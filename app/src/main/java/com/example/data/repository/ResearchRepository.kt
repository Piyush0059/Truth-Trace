package com.example.data.repository
import com.example.data.gemini.GeminiService
import com.example.data.model.Citation
import com.example.data.model.InvestigationResult
import com.example.data.model.RetrievedChunk
import com.example.data.model.RetrievalStrategy
import com.example.data.rag.BM25Retriever
import com.example.data.rag.DenseRetriever
import com.example.data.rag.HybridRetriever
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResearchRepository(
    private val documentRepository: DocumentRepository,
    private val firestoreRepository: FirestoreRepository? = null
) {
    private val _history = MutableStateFlow<List<InvestigationResult>>(emptyList())
    val history: StateFlow<List<InvestigationResult>> = _history.asStateFlow()

    suspend fun investigateQuestion(
        question: String,
        selectedDocumentId: String? = null,
        strategy: RetrievalStrategy = RetrievalStrategy.HYBRID,
        model: String = GeminiService.MODEL_PRO,
        enableHighThinking: Boolean = true,
        useGoogleSearch: Boolean = false,
        topK: Int = 4
    ): InvestigationResult {
        val startTime = System.currentTimeMillis()

        val chunks = if (selectedDocumentId != null) {
            documentRepository.getChunksForDocument(selectedDocumentId)
        } else {
            documentRepository.getAllChunks()
        }

        // 1. Retrieve candidates based on configured strategy
        val retrievedChunks: List<RetrievedChunk> = when (strategy) {
            RetrievalStrategy.BASELINE -> emptyList() // No context for baseline
            RetrievalStrategy.DENSE -> DenseRetriever(chunks).retrieve(question, topK)
            RetrievalStrategy.BM25 -> BM25Retriever(chunks).retrieve(question, topK)
            RetrievalStrategy.HYBRID -> HybridRetriever(chunks).retrieve(question, topK)
        }

        // 2. Generate grounded answer and extract atomic claims
        val (answerText, claims) = GeminiService.generateGroundedAnswer(
            question = question,
            retrievedChunks = retrievedChunks,
            model = model,
            enableHighThinking = enableHighThinking,
            useGoogleSearch = useGoogleSearch
        )

        // 3. Formulate verifiable Citations linking directly to retrieved passages
        val citations = retrievedChunks.mapIndexed { index, rChunk ->
            Citation(
                id = "S${index + 1}",
                chunkId = rChunk.chunk.id,
                documentId = rChunk.chunk.documentId,
                documentTitle = rChunk.chunk.documentTitle,
                pageNumber = rChunk.chunk.pageNumber,
                excerpt = rChunk.chunk.text.take(240),
                surroundingContext = rChunk.chunk.text
            )
        }

        val latencyMs = System.currentTimeMillis() - startTime

        val result = InvestigationResult(
            question = question,
            answer = answerText,
            strategyUsed = strategy,
            modelUsed = if (useGoogleSearch) GeminiService.MODEL_FLASH else model,
            isThinkingMode = enableHighThinking && !useGoogleSearch,
            retrievedChunks = retrievedChunks,
            claims = claims,
            citations = citations,
            latencyMs = latencyMs
        )

        _history.value = listOf(result) + _history.value

        // Optionally persist query in Firestore if user is authenticated
        try {
            firestoreRepository?.saveQuery(result)
        } catch (_: Exception) {}

        return result
    }
}
