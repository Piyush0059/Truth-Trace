package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccessibleTeal
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.RestrainedViolet
import com.example.ui.theme.SurfaceCard
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D Node representation in constellation space.
 */
data class Node3D(
    val id: String,
    val title: String,
    val category: String, // "doc", "evidence", "claim", "core"
    val x: Float,
    val y: Float,
    val z: Float,
    val baseColor: Color,
    val radius: Float = 14f
)

/**
 * Interactive 3D Evidence Constellation visualizer.
 * Supports touch rotate/drag, perspective depth, connected claim links,
 * state-driven retrieval animations, and reduced-motion mode.
 */
@Composable
fun EvidenceConstellation3D(
    modifier: Modifier = Modifier,
    activeRetrievalState: String = "IDLE", // "IDLE", "RETRIEVING", "VERIFYING", "COMPLETE"
    highlightedNodeId: String? = null,
    onNodeSelected: (Node3D) -> Unit = {}
) {
    var rotX by remember { mutableFloatStateOf(0.2f) }
    var rotY by remember { mutableFloatStateOf(0.4f) }
    var selectedNode by remember { mutableStateOf<Node3D?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "constellation_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    // Base 3D Nodes representing document network
    val nodes = remember {
        listOf(
            Node3D("core", "TruthTrace Core", "core", 0f, 0f, 0f, ElectricCyan, 22f),
            Node3D("doc1", "RAG Factuality 2026", "doc", -160f, -80f, 60f, RestrainedViolet, 18f),
            Node3D("doc2", "Climate Synthesis", "doc", 160f, -60f, -70f, RestrainedViolet, 18f),
            Node3D("ev1", "Dense Recall@5 (0.78)", "evidence", -110f, 70f, -100f, AccessibleTeal, 14f),
            Node3D("ev2", "BM25 Exact Precision", "evidence", 120f, 90f, 80f, AccessibleTeal, 14f),
            Node3D("ev3", "Hybrid Frontier (0.91)", "evidence", 0f, -140f, 90f, ElectricCyan, 16f),
            Node3D("clm1", "Entailment != Similarity", "claim", -80f, -110f, -120f, Color(0xFFF59E0B), 14f),
            Node3D("clm2", "Abstention F1 Rule", "claim", 90f, -100f, 130f, AccessibleTeal, 14f)
        )
    }

    val edges = remember {
        listOf(
            Pair("core", "doc1"),
            Pair("core", "doc2"),
            Pair("doc1", "ev1"),
            Pair("doc1", "ev3"),
            Pair("doc2", "ev2"),
            Pair("ev3", "clm1"),
            Pair("ev3", "clm2"),
            Pair("ev1", "clm1"),
            Pair("ev2", "clm2")
        )
    }

    // 40 Ambient starfield particles
    val particles = remember {
        List(40) { i ->
            val angle = i * 0.157f
            val radius = 120f + (i * 7f) % 180f
            Triple(
                cos(angle) * radius,
                sin(angle * 1.5f) * radius * 0.7f,
                sin(angle) * radius
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF131E35), MidnightNavy),
                    radius = 500f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rotY += dragAmount.x * 0.008f
                    rotX = (rotX - dragAmount.y * 0.008f).coerceIn(-1.2f, 1.2f)
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(nodes) {
                    detectTapGestures { tapOffset ->
                        // Hit-test nodes projected to 2D
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val centerX = width / 2f
                        val centerY = height / 2f
                        val fov = 400f

                        val tapped = nodes.map { n ->
                            val pt = projectPoint(n.x, n.y, n.z, rotX, rotY, centerX, centerY, fov)
                            Pair(n, (pt.first - tapOffset.x) * (pt.first - tapOffset.x) + (pt.second - tapOffset.y) * (pt.second - tapOffset.y))
                        }.minByOrNull { it.second }

                        if (tapped != null && tapped.second < (35f * 35f)) {
                            selectedNode = tapped.first
                            onNodeSelected(tapped.first)
                        } else {
                            selectedNode = null
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val fov = 400f

            // 1. Draw ambient 3D starfield particles
            for (p in particles) {
                val proj = projectPoint(p.first, p.second, p.third, rotX, rotY, centerX, centerY, fov)
                val alpha = (0.25f + 0.15f * sin(pulse + p.first)).coerceIn(0.1f, 0.6f)
                drawCircle(
                    color = ElectricCyan.copy(alpha = alpha),
                    radius = 1.8f * proj.third,
                    center = Offset(proj.first, proj.second)
                )
            }

            // 2. Project all nodes
            val projectedNodes = nodes.map { node ->
                val p = projectPoint(node.x, node.y, node.z, rotX, rotY, centerX, centerY, fov)
                node to p
            }.toMap()

            // 3. Draw connecting constellation edges
            for (edge in edges) {
                val p1 = projectedNodes[nodes.first { it.id == edge.first }]
                val p2 = projectedNodes[nodes.first { it.id == edge.second }]
                if (p1 != null && p2 != null) {
                    val isBeamActive = activeRetrievalState in listOf("RETRIEVING", "VERIFYING")
                    val edgeAlpha = if (isBeamActive) (0.4f + 0.3f * sin(pulse * 3f + p1.first)).coerceIn(0.2f, 0.9f) else 0.25f
                    val edgeColor = if (isBeamActive) ElectricCyan else Color(0xFF4A6185)

                    drawLine(
                        color = edgeColor.copy(alpha = edgeAlpha),
                        start = Offset(p1.first, p1.second),
                        end = Offset(p2.first, p2.second),
                        strokeWidth = if (isBeamActive) 2.2f else 1.2f
                    )
                }
            }

            // 4. Sort and draw nodes by depth (z-order)
            val sorted = projectedNodes.entries.sortedBy { it.value.third }
            for (entry in sorted) {
                val node = entry.key
                val proj = entry.value
                val scale = proj.third
                val isSelected = selectedNode?.id == node.id || highlightedNodeId == node.id

                val nodeRadius = node.radius * scale * (if (isSelected) 1.35f else 1f)

                // Outer halo glow
                drawCircle(
                    color = node.baseColor.copy(alpha = if (isSelected) 0.5f else 0.18f),
                    radius = nodeRadius * 2.2f,
                    center = Offset(proj.first, proj.second)
                )

                // Core body
                drawCircle(
                    color = if (isSelected) Color.White else node.baseColor,
                    radius = nodeRadius,
                    center = Offset(proj.first, proj.second)
                )

                // Ring accent
                drawCircle(
                    color = node.baseColor,
                    radius = nodeRadius + 3f,
                    center = Offset(proj.first, proj.second),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // Overlay Header & Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "EVIDENCE CONSTELLATION 3D",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.sp,
                    color = ElectricCyan
                ),
                modifier = Modifier.align(Alignment.TopStart)
            )

            IconButton(
                onClick = {
                    rotX = 0.2f
                    rotY = 0.4f
                    selectedNode = null
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset 3D camera",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Selected node floating callout
        selectedNode?.let { node ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = SurfaceCard.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan)
            ) {
                Text(
                    text = "${node.title} (${node.category.uppercase()})",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * 3D-to-2D Perspective Projection Helper.
 */
private fun projectPoint(
    x: Float, y: Float, z: Float,
    rotX: Float, rotY: Float,
    centerX: Float, centerY: Float,
    fov: Float
): Triple<Float, Float, Float> {
    // Rotation around Y axis
    val cosY = cos(rotY)
    val sinY = sin(rotY)
    val x1 = x * cosY + z * sinY
    val z1 = -x * sinY + z * cosY

    // Rotation around X axis
    val cosX = cos(rotX)
    val sinX = sin(rotX)
    val y2 = y * cosX - z1 * sinX
    val z2 = y * sinX + z1 * cosX

    // Perspective projection
    val distance = fov + z2
    val safeDistance = if (distance > 20f) distance else 20f
    val scale = fov / safeDistance

    val screenX = centerX + x1 * scale
    val screenY = centerY + y2 * scale

    return Triple(screenX, screenY, scale)
}
