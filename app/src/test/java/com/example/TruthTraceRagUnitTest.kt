package com.example

import com.example.data.model.RetrievalStrategy
import com.example.data.rag.BM25Retriever
import com.example.data.rag.BenchmarkDatasets
import com.example.data.rag.DenseRetriever
import com.example.data.rag.DocumentChunker
import com.example.data.rag.HybridRetriever
import com.example.data.repository.DocumentRepository
import com.example.data.repository.EvaluationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TruthTraceRagUnitTest {

    @Test
    fun testDocumentChunkingPreservesMetadata() {
        val sampleText = "Sentence one. Sentence two is longer and contains more details. Sentence three discusses retrieval."
        val chunks = DocumentChunker.chunkText(
            documentId = "test_doc",
            documentTitle = "Test Document",
            text = sampleText,
            chunkSizeChars = 50,
            chunkOverlapChars = 10
        )

        assertFalse("Chunks should not be empty", chunks.isEmpty())
        assertTrue("Every chunk should have a non-blank text", chunks.all { it.text.isNotBlank() })
        assertEquals("Document ID should be preserved", "test_doc", chunks.first().documentId)
        assertEquals("Document title should be preserved", "Test Document", chunks.first().documentTitle)
        assertTrue("Character offsets should be sequential", chunks.first().endCharOffset > chunks.first().startCharOffset)
    }

    @Test
    fun testBm25RetrievalRanking() {
        val doc = BenchmarkDatasets.sampleDocument1
        val chunks = DocumentChunker.chunkText(doc.id, doc.title, doc.rawText)
        val retriever = BM25Retriever(chunks)

        val results = retriever.retrieve("Pareto frontier Recall@5", topK = 3)
        assertFalse("BM25 should return matching chunks", results.isEmpty())
        assertTrue("Results should be sorted by rank", results[0].rank == 1)
        assertTrue("Top chunk should have highest or equal score", results[0].similarityScore >= (results.getOrNull(1)?.similarityScore ?: 0f))
    }

    @Test
    fun testDenseRetrievalEmbeddings() {
        val doc = BenchmarkDatasets.sampleDocument1
        val chunks = DocumentChunker.chunkText(doc.id, doc.title, doc.rawText)
        val retriever = DenseRetriever(chunks)

        val results = retriever.retrieve("semantic embeddings and neural bi-encoders", topK = 3)
        assertFalse("Dense retriever should return matching candidates", results.isEmpty())
        assertTrue("Similarity score should be within [0, 1]", results[0].similarityScore in 0f..1f)
    }

    @Test
    fun testHybridRetrieverReciprocalRankFusion() {
        val doc = BenchmarkDatasets.sampleDocument1
        val chunks = DocumentChunker.chunkText(doc.id, doc.title, doc.rawText)
        val hybrid = HybridRetriever(chunks)

        val results = hybrid.retrieve("quantum error suppression and entailment", topK = 4)
        assertFalse("Hybrid retriever should produce results", results.isEmpty())
        assertEquals(1, results.first().rank)
        assertTrue(results.first().similarityScore in 0f..1f)
    }

    @Test
    fun testComparativeEvaluationMetrics() = runBlocking {
        val docRepo = DocumentRepository()
        val evalRepo = EvaluationRepository(docRepo)

        val run = evalRepo.runComparativeEvaluation(
            dataset = BenchmarkDatasets.benchmarkDataset1,
            topK = 4
        )

        assertNotNull(run)
        assertEquals("RAG Factuality & Verification Benchmark", run.datasetName)

        val hybridResult = run.resultsByStrategy[RetrievalStrategy.HYBRID]
        assertNotNull(hybridResult)
        assertTrue("Recall@k must be in range [0, 1]", hybridResult!!.recallAtK in 0f..1f)
        assertTrue("Precision@k must be in range [0, 1]", hybridResult.precisionAtK in 0f..1f)
        assertTrue("MRR must be in range [0, 1]", hybridResult.mrr in 0f..1f)
        assertTrue("nDCG must be in range [0, 1]", hybridResult.ndcgAtK in 0f..1f)
        assertTrue("Faithfulness must be in range [0, 1]", hybridResult.claimFaithfulness in 0f..1f)
        assertTrue("Citation accuracy must be in range [0, 1]", hybridResult.citationSupportAccuracy in 0f..1f)
        assertTrue("Abstention score must be in range [0, 1]", hybridResult.abstentionPerformance in 0f..1f)

        val json = evalRepo.exportRunAsJson(run)
        assertTrue("JSON export should contain strategy information", json.contains("Hybrid (Dense + BM25)"))
    }
}
