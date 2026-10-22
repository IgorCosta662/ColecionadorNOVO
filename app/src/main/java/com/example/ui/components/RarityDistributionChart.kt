package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Item

data class RaritySlice(
    val rarityName: String,
    val count: Int,
    val percentage: Float,
    val color: Color
)

@Composable
fun RarityDistributionChart(
    items: List<Item>,
    modifier: Modifier = Modifier
) {
    val rarityPalette = listOf(
        Color(0xFF6366F1), // Indigo - Comum
        Color(0xFF38BDF8), // Cyan - Incomum
        Color(0xFFA855F7), // Purple - Rara
        Color(0xFFF59E0B), // Amber - Ultra Rara / Holo
        Color(0xFFEC4899), // Pink - Ilustração Especial / Secret
        Color(0xFF10B981)  // Emerald - Mítica / STH
    )

    val rarityGroups = remember(items) {
        val total = items.size.coerceAtLeast(1)
        val grouped = items.groupBy {
            val r = it.rarity.trim()
            if (r.isEmpty()) "Comum" else r
        }
        grouped.entries.mapIndexed { index, entry ->
            val color = rarityPalette[index % rarityPalette.size]
            val pct = entry.value.size.toFloat() / total.toFloat()
            RaritySlice(entry.key, entry.value.size, pct, color)
        }.sortedByDescending { it.count }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Distribuição por Raridade",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${items.size} cartas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma carta para gerar gráfico.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Donut Chart Canvas
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(120.dp)) {
                            var startAngle = -90f
                            val strokeWidth = 24.dp.toPx()

                            rarityGroups.forEach { slice ->
                                val sweepAngle = slice.percentage * 360f
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                    size = Size(size.width, size.height)
                                )
                                startAngle += sweepAngle
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${rarityGroups.size}",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text("Raridades", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Legend Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rarityGroups.take(5).forEach { slice ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(slice.color)
                                    )
                                    Text(
                                        slice.rarityName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    "${slice.count} (${(slice.percentage * 100).toInt()}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ValueDistributionBarChart(
    items: List<Item>,
    currencySymbol: String = "R$",
    modifier: Modifier = Modifier
) {
    val tierBuckets = remember(items) {
        val tier1 = items.count { it.estimatedValue <= 10.0 }
        val tier2 = items.count { it.estimatedValue in 10.01..50.0 }
        val tier3 = items.count { it.estimatedValue in 50.01..150.0 }
        val tier4 = items.count { it.estimatedValue > 150.0 }
        val maxCount = maxOf(tier1, tier2, tier3, tier4, 1)

        listOf(
            "Até R$ 10" to (tier1 to (tier1.toFloat() / maxCount)),
            "R$ 10 - 50" to (tier2 to (tier2.toFloat() / maxCount)),
            "R$ 50 - 150" to (tier3 to (tier3.toFloat() / maxCount)),
            "R$ 150+" to (tier4 to (tier4.toFloat() / maxCount))
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Faixas de Valor da Coleção",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            tierBuckets.forEach { (label, data) ->
                val (count, fraction) = data
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("$count cartas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
