package com.example.data.model

import java.util.UUID

/**
 * A benchmark evaluation query with ground-truth relevant chunk IDs and expected answer status.
 */
data class BenchmarkSample(
    val id: String,
    val question: String,
    val category: String, // "answerable", "unanswerable", "conflicting", "paraphrased"
    val groundTruthRelevantChunkIds: List<String>,
    val expectedAbstention: Boolean = false, // true if the question is unanswerable from the document
    val referenceAnswer: String = ""
)

/**
 * Evaluation dataset containing curated research questions and documents.
 */
data class EvaluationDataset(
    val id: String,
    val name: String,
    val description: String,
    val targetDocumentId: String,
    val samples: List<BenchmarkSample>
)

/**
 * Evaluated metric results for a retrieval strategy on a benchmark dataset.
 */
data class EvaluationMetricResult(
    val strategy: RetrievalStrategy,
    val sampleCount: Int,
    val topK: Int,
    val recallAtK: Float,
    val precisionAtK: Float,
    val mrr: Float, // Mean Reciprocal Rank
    val ndcgAtK: Float, // Normalized Discounted Cumulative Gain
    val claimFaithfulness: Float, // % of claims that are supported vs total claims
    val citationSupportAccuracy: Float, // % of citations that are grounded
    val abstentionPerformance: Float, // % of unanswerable questions accurately abstained
    val averageLatencyMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * An experiment run tracking detailed per-sample outputs.
 */
data class ExperimentRun(
    val id: String = UUID.randomUUID().toString(),
    val datasetName: String,
    val configSummary: String,
    val resultsByStrategy: Map<RetrievalStrategy, EvaluationMetricResult>,
    val timestamp: Long = System.currentTimeMillis()
)
