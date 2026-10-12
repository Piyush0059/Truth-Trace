package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EvaluationMetricResult
import com.example.data.model.RetrievalStrategy
import com.example.ui.theme.AccessibleTeal
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.RestrainedViolet
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Metric card displaying single evaluation score with TruthTrace scientific dark theme.
 */
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color = ElectricCyan,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}

/**
 * Comparative bar chart comparing strategies with TruthTrace scientific aesthetic.
 */
@Composable
fun EvaluationComparisonChart(
    results: Map<RetrievalStrategy, EvaluationMetricResult>,
    metricSelector: String = "Faithfulness",
    modifier: Modifier = Modifier
) {
    val strategies = listOf(
        RetrievalStrategy.BASELINE,
        RetrievalStrategy.DENSE,
        RetrievalStrategy.BM25,
        RetrievalStrategy.HYBRID
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("evaluation_chart"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RETRIEVAL COMPARISON: $metricSelector",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                strategies.forEach { strat ->
                    val metric = results[strat]
                    val score = when (metricSelector) {
                        "Recall@k" -> metric?.recallAtK ?: 0f
                        "MRR" -> metric?.mrr ?: 0f
                        "Citations" -> metric?.citationSupportAccuracy ?: 0f
                        else -> metric?.claimFaithfulness ?: 0f
                    }

                    val color = when (strat) {
                        RetrievalStrategy.BASELINE -> Color(0xFF64748B)
                        RetrievalStrategy.DENSE -> RestrainedViolet
                        RetrievalStrategy.BM25 -> AccessibleTeal
                        RetrievalStrategy.HYBRID -> ElectricCyan
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strat.displayName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = String.format("%.2f", score),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(SurfaceElevated, RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = score.coerceIn(0.02f, 1f))
                                    .height(8.dp)
                                    .background(color, RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
