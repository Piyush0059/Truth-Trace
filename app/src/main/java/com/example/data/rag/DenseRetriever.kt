package com.example.data.rag

import com.example.data.model.DocumentChunk
import com.example.data.model.RetrievedChunk
import kotlin.math.sqrt

/**
 * Dense Semantic Vector Retriever using subword n-gram semantic embeddings and cosine similarity.
 */
class DenseRetriever(
    private val chunks: List<DocumentChunk>
) {
    private val dimension = 128
    private val chunkVectors: List<FloatArray> = chunks.map { embedText(it.text) }

    /**
     * Compute a fixed-dimension semantic embedding vector for a piece of text.
     */
    private fun embedText(text: String): FloatArray {
        val vector = FloatArray(dimension)
        val clean = text.lowercase().replace(Regex("[^a-z0-9 ]"), " ")
        val words = clean.split(Regex("\\s+")).filter { it.isNotEmpty() }

        // Word and character trigram hashing with positional decay
        for ((wordIdx, word) in words.withIndex()) {
            val weight = 1.0f / (1.0f + 0.05f * wordIdx)
            // Word hash
            val wordHash = (word.hashCode() and 0x7FFFFFFF) % dimension
            vector[wordHash] += weight * 1.5f

            // Character trigrams for subword semantic capture
            if (word.length >= 3) {
                for (i in 0..word.length - 3) {
                    val tri = word.substring(i, i + 3)
                    val triHash = (tri.hashCode() and 0x7FFFFFFF) % dimension
                    vector[triHash] += weight * 0.8f
                }
            }
        }

        // L2 Normalization
        var sumSquares = 0f
        for (v in vector) sumSquares += v * v
        val norm = sqrt(sumSquares)
        if (norm > 0f) {
            for (i in vector.indices) {
                vector[i] /= norm
            }
        }
        return vector
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
        }
        return dot.coerceIn(0f, 1f)
    }

    fun retrieve(query: String, topK: Int = 4): List<RetrievedChunk> {
        if (chunks.isEmpty() || query.isBlank()) return emptyList()

        val queryVec = embedText(query)
        val scored = chunks.mapIndexed { index, chunk ->
            val sim = cosineSimilarity(queryVec, chunkVectors[index])
            Pair(chunk, sim)
        }

        return scored.sortedByDescending { it.second }
            .take(topK)
            .mapIndexed { rank, pair ->
                RetrievedChunk(
                    chunk = pair.first,
                    similarityScore = pair.second,
                    denseScore = pair.second,
                    rank = rank + 1
                )
            }
    }
}
