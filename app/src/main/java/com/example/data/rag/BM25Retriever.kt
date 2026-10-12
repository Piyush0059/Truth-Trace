package com.example.data.rag

import com.example.data.model.DocumentChunk
import com.example.data.model.RetrievedChunk
import kotlin.math.ln

/**
 * Probabilistic Lexical Retrieval using the Okapi BM25 ranking function.
 */
class BM25Retriever(
    private val chunks: List<DocumentChunk>,
    private val k1: Float = 1.5f,
    private val b: Float = 0.75f
) {
    private val stopWords = setOf(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
        "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but",
        "by", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
        "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him",
        "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just",
        "me", "more", "most", "my", "myself", "no", "nor", "not", "now", "of", "off", "on", "once",
        "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same",
        "she", "should", "so", "some", "such", "than", "that", "the", "their", "theirs", "them",
        "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too",
        "under", "until", "up", "very", "was", "we", "were", "what", "when", "where", "which",
        "while", "who", "whom", "why", "with", "would", "you", "your", "yours", "yourself"
    )

    private val docTokenLists: List<List<String>> = chunks.map { tokenize(it.text) }
    private val docLengths: List<Int> = docTokenLists.map { it.size }
    private val avgDocLength: Float = if (docLengths.isNotEmpty()) docLengths.average().toFloat() else 1f

    // Document frequencies: number of chunks containing term t
    private val docFrequencies: Map<String, Int> = buildMap {
        docTokenLists.forEach { tokens ->
            tokens.toSet().forEach { term ->
                put(term, getOrDefault(term, 0) + 1)
            }
        }
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase()
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length > 1 && it !in stopWords }
    }

    private fun computeIdf(term: String): Float {
        val df = docFrequencies[term] ?: 0
        val totalDocs = chunks.size
        // Standard Lucene/BM25 Robertson-Spärck Jones IDF
        return ln(1.0 + (totalDocs - df + 0.5) / (df + 0.5)).toFloat().coerceAtLeast(0.01f)
    }

    fun retrieve(query: String, topK: Int = 4): List<RetrievedChunk> {
        if (chunks.isEmpty()) return emptyList()

        val queryTerms = tokenize(query)
        if (queryTerms.isEmpty()) return emptyList()

        val scored = chunks.mapIndexed { index, chunk ->
            val docTokens = docTokenLists[index]
            val docLen = docLengths[index]
            val termCounts = docTokens.groupingBy { it }.eachCount()

            var score = 0f
            for (term in queryTerms) {
                val tf = termCounts[term] ?: 0
                if (tf > 0) {
                    val idf = computeIdf(term)
                    val numerator = tf * (k1 + 1f)
                    val denominator = tf + k1 * (1f - b + b * (docLen / avgDocLength.coerceAtLeast(1f)))
                    score += idf * (numerator / denominator)
                }
            }
            Pair(chunk, score)
        }

        val maxScore = scored.maxOfOrNull { it.second } ?: 1f
        val sorted = scored.filter { it.second > 0f }
            .sortedByDescending { it.second }
            .take(topK)

        return sorted.mapIndexed { rankIndex, pair ->
            val normalizedSimilarity = if (maxScore > 0f) (pair.second / maxScore).coerceIn(0f, 1f) else 0f
            RetrievedChunk(
                chunk = pair.first,
                similarityScore = normalizedSimilarity,
                lexicalScore = pair.second,
                rank = rankIndex + 1
            )
        }
    }
}
