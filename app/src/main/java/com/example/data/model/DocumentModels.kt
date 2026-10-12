package com.example.data.model

import java.util.UUID

/**
 * Represents a document in TruthTrace.
 */
data class ResearchDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val source: String, // "pdf", "txt", "scan"
    val rawText: String,
    val pageCount: Int = 1,
    val chunks: List<DocumentChunk> = emptyList(),
    val fileSizeBytes: Long = rawText.length.toLong(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * An atomic chunk extracted from a document with page and position metadata.
 */
data class DocumentChunk(
    val id: String = UUID.randomUUID().toString(),
    val documentId: String,
    val documentTitle: String,
    val pageNumber: Int = 1,
    val chunkIndex: Int,
    val text: String,
    val startCharOffset: Int = 0,
    val endCharOffset: Int = text.length
)

/**
 * Retrieval strategy configuration for evaluation and search.
 */
enum class RetrievalStrategy(val displayName: String, val description: String) {
    BASELINE("No Context (Baseline)", "Generates answers without retrieved context to measure baseline hallucination"),
    DENSE("Dense Semantic", "Vector similarity retrieval based on semantic embeddings"),
    BM25("BM25 Keyword", "Probabilistic lexical matching normalized by term frequency and doc length"),
    HYBRID("Hybrid (Dense + BM25)", "Reciprocal Rank Fusion (RRF) combining semantic and lexical signals")
}

/**
 * A retrieved chunk with score and provenance.
 */
data class RetrievedChunk(
    val chunk: DocumentChunk,
    val similarityScore: Float, // [0.0, 1.0] - Strictly labelled as Retrieval Similarity
    val lexicalScore: Float = 0f,
    val denseScore: Float = 0f,
    val rank: Int = 1
)

/**
 * Claim verification status.
 */
enum class ClaimStatus(val label: String) {
    SUPPORTED("Supported"),
    CONTRADICTED("Contradicted"),
    INSUFFICIENT("Insufficient Evidence"),
    UNVERIFIED("Unverified")
}

/**
 * Atomic factual claim extracted from an AI-generated answer.
 */
data class AtomicClaim(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val claimType: String = "factual",
    val status: ClaimStatus = ClaimStatus.UNVERIFIED,
    val explanation: String = "",
    val supportedByChunkIds: List<String> = emptyList(),
    val contradictedByChunkIds: List<String> = emptyList(),
    val confidenceScore: Float = 0f
)

/**
 * A verifiable citation linked directly to a real source chunk.
 */
data class Citation(
    val id: String, // e.g. "S1", "S2"
    val chunkId: String,
    val documentId: String,
    val documentTitle: String,
    val pageNumber: Int,
    val excerpt: String,
    val surroundingContext: String = excerpt
)

/**
 * Complete result of an investigated question in TruthTrace.
 */
data class InvestigationResult(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val strategyUsed: RetrievalStrategy,
    val modelUsed: String,
    val isThinkingMode: Boolean = false,
    val retrievedChunks: List<RetrievedChunk> = emptyList(),
    val claims: List<AtomicClaim> = emptyList(),
    val citations: List<Citation> = emptyList(),
    val latencyMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis()
)
