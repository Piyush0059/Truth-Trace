package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.EvaluationMetricResult
import com.example.data.model.InvestigationResult
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    context: Context,
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
) {
    private val TAG = "FirestoreRepo"
    private val auth = Firebase.auth

    fun getCurrentUser() = auth.currentUser

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun saveUserStats(docsCount: Int, queriesCount: Int, evaluationsCount: Int) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val userRef = db.collection("users").document(uid)
            val data = mapOf(
                "userId" to uid,
                "email" to (auth.currentUser?.email ?: ""),
                "displayName" to (auth.currentUser?.displayName ?: "Researcher"),
                "documentsCount" to docsCount,
                "queriesCount" to queriesCount,
                "evaluationsRunCount" to evaluationsCount,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save user stats: ${e.message}")
        }
    }

    suspend fun saveQuery(result: InvestigationResult) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val queryRef = db.collection("users").document(uid).collection("queries").document(result.id)
            val data = mapOf(
                "userId" to uid,
                "question" to result.question,
                "answer" to result.answer,
                "modelUsed" to result.modelUsed,
                "retrievalStrategy" to result.strategyUsed.name.lowercase(),
                "claimCount" to result.claims.size,
                "supportedClaims" to result.claims.count { it.status.name == "SUPPORTED" },
                "contradictedClaims" to result.claims.count { it.status.name == "CONTRADICTED" },
                "insufficientClaims" to result.claims.count { it.status.name == "INSUFFICIENT" },
                "latencyMs" to result.latencyMs,
                "createdAt" to FieldValue.serverTimestamp()
            )
            queryRef.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save query to Firestore: ${e.message}")
        }
    }

    suspend fun saveEvaluation(metric: EvaluationMetricResult) {
        val uid = auth.currentUser?.uid ?: return
        try {
            val evalRef = db.collection("users").document(uid).collection("evaluations").document()
            val data = mapOf(
                "userId" to uid,
                "strategy" to metric.strategy.name.lowercase(),
                "sampleCount" to metric.sampleCount,
                "recallAtK" to metric.recallAtK.toDouble(),
                "precisionAtK" to metric.precisionAtK.toDouble(),
                "mrr" to metric.mrr.toDouble(),
                "ndcg" to metric.ndcgAtK.toDouble(),
                "faithfulness" to metric.claimFaithfulness.toDouble(),
                "citationCorrectness" to metric.citationSupportAccuracy.toDouble(),
                "abstentionScore" to metric.abstentionPerformance.toDouble(),
                "averageLatencyMs" to metric.averageLatencyMs,
                "createdAt" to FieldValue.serverTimestamp()
            )
            evalRef.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save evaluation to Firestore: ${e.message}")
        }
    }
}
