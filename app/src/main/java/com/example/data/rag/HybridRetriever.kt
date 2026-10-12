package com.example.data.rag

import com.example.data.model.DocumentChunk
import com.example.data.model.RetrievedChunk

/**
 * Hybrid Retriever combining Dense Semantic and Lexical BM25 rankings
 * via Reciprocal Rank Fusion (RRF).
 */
class HybridRetriever(
    private val chunks: List<DocumentChunk>,
    private val rrfConstantK: Int = 60
) {
    private val bm25Retriever = BM25Retriever(chunks)
    private val denseRetriever = DenseRetriever(chunks)

    fun retrieve(query: String, topK: Int = 4): List<RetrievedChunk> {
        if (chunks.isEmpty() || query.isBlank()) return emptyList()

        val denseResults = denseRetriever.retrieve(query, topK = chunks.size)
        val bm25Results = bm25Retriever.retrieve(query, topK = chunks.size)

        val denseRanks = denseResults.mapIndexed { index, r -> r.chunk.id to (index + 1) }.toMap()
        val bm25Ranks = bm25Results.mapIndexed { index, r -> r.chunk.id to (index + 1) }.toMap()

        val chunkMap = chunks.associateBy { it.id }

        // Reciprocal Rank Fusion: RRF = 1 / (k + rank_dense) + 1 / (k + rank_bm25)
        val fusedScores = chunks.map { chunk ->
            val denseRank = denseRanks[chunk.id] ?: (chunks.size + 1)
            val bm25Rank = bm25Ranks[chunk.id] ?: (chunks.size + 1)

            val rrfDense = 1.0f / (rrfConstantK + denseRank)
            val rrfBm25 = 1.0f / (rrfConstantK + bm25Rank)
            val totalRrf = rrfDense + rrfBm25

            val denseScore = denseResults.find { it.chunk.id == chunk.id }?.similarityScore ?: 0f
            val bm25Score = bm25Results.find { it.chunk.id == chunk.id }?.lexicalScore ?: 0f

            Triple(chunk, totalRrf, Pair(denseScore, bm25Score))
        }

        val maxRrf = fusedScores.maxOfOrNull { it.second } ?: 1f
        val sorted = fusedScores.sortedByDescending { it.second }.take(topK)

        return sorted.mapIndexed { index, triple ->
            val normScore = if (maxRrf > 0f) (triple.second / maxRrf).coerceIn(0f, 1f) else 0f
            RetrievedChunk(
                chunk = triple.first,
                similarityScore = normScore,
                denseScore = triple.third.first,
                lexicalScore = triple.third.second,
                rank = index + 1
            )
        }
    }
}
