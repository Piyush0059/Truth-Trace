package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AtomicClaim
import com.example.data.model.Citation
import com.example.data.model.ClaimStatus
import com.example.ui.theme.AccessibleTeal
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.StatusContradicted
import com.example.ui.theme.StatusInsufficient
import com.example.ui.theme.StatusSupported
import com.example.ui.theme.StatusUnverified
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Clickable citation chip embedded in answers and claim cards.
 */
@Composable
fun CitationChip(
    citationTag: String, // e.g. "S1", "[S1]"
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanTag = citationTag.removePrefix("[").removeSuffix("]")
    Surface(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .clickable(onClick = onClick)
            .testTag("citation_chip_$cleanTag"),
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF14303B),
        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(ElectricCyan, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "[$cleanTag]",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            )
        }
    }
}

/**
 * Detailed modal inspector showing the exact source chunk and provenance.
 */
@Composable
fun EvidenceDialog(
    citation: Citation,
    similarityScore: Float? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("evidence_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SOURCE PASSAGE [${citation.id}]",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.5.sp,
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = citation.documentTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Provenance metadata bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Page ${citation.pageNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Text(
                        text = "Chunk ID: ${citation.chunkId}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = AccessibleTeal
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verified passage text
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceCard,
                    border = BorderStroke(1.dp, Color(0xFF263854))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "VERIFIED PASSAGE CONTENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = citation.surroundingContext,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory scientific disclaimer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131E2E), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Citation Information",
                        tint = ElectricCyan.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Citation integrity check: Verifies that this source chunk exists in the ingested corpus. A valid citation proves passage existence, not absolute real-world truth.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_evidence_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Close Inspector",
                        style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF042622))
                    )
                }
            }
        }
    }
}

/**
 * Atomic Claim Card displaying statement, status badge, reasoning, and citations.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClaimCard(
    claim: AtomicClaim,
    citations: List<Citation>,
    onCitationClicked: (Citation) -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusIcon) = when (claim.status) {
        ClaimStatus.SUPPORTED -> Pair(StatusSupported, Icons.Default.CheckCircle)
        ClaimStatus.CONTRADICTED -> Pair(StatusContradicted, Icons.Default.Error)
        ClaimStatus.INSUFFICIENT -> Pair(StatusInsufficient, Icons.Default.Warning)
        ClaimStatus.UNVERIFIED -> Pair(StatusUnverified, Icons.AutoMirrored.Filled.Help)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("claim_card_${claim.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLAIM #${claim.id.takeLast(4).uppercase()}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                )

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = claim.status.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Claim Text
            Text(
                text = claim.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            )

            if (claim.explanation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = claim.explanation,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                )
            }

            // Linked citations
            val linkedCitations = citations.filter { it.chunkId in claim.supportedByChunkIds || it.chunkId in claim.contradictedByChunkIds }
            if (linkedCitations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    linkedCitations.forEach { cit ->
                        CitationChip(
                            citationTag = cit.id,
                            onClick = { onCitationClicked(cit) }
                        )
                    }
                }
            }
        }
    }
}
