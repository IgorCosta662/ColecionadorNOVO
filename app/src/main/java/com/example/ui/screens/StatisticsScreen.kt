package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.CollectorViewModel
import com.example.ui.components.RarityDistributionChart
import com.example.ui.components.ValueDistributionBarChart
import com.example.ui.navigation.Routes
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: CollectorViewModel,
    navController: NavController,
    onOpenDrawer: (() -> Unit)? = null
) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    val totalValue = items.sumOf { it.totalEstimatedValue }
    val totalCost = items.sumOf { it.totalPurchasePrice }
    val totalProfit = totalValue - totalCost
    val unlockedAchievements = achievements.count { it.isUnlocked }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Lateral")
                        }
                    }
                },
                title = { Text("Estatísticas e Patrimônio", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Portfolio Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Resumo Geral do Portfólio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Catalogado:")
                            Text("${items.size} itens", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Valor de Mercado:")
                            Text(currency.formatValue(totalValue), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Custo de Aquisição:")
                            Text(currency.formatValue(totalCost), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lucro Não Realizado:")
                            Text(
                                currency.formatValue(totalProfit),
                                fontWeight = FontWeight.Bold,
                                color = if (totalProfit >= 0) NeonEmerald else NeonRose
                            )
                        }
                    }
                }
            }

            // Interactive Donut Chart: Rarity Distribution
            item {
                RarityDistributionChart(items = items)
            }

            // Value Distribution Buckets Chart
            item {
                ValueDistributionBarChart(items = items, currencySymbol = currency.symbol)
            }

            // Achievements Quick Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { navController.navigate(Routes.ACHIEVEMENTS) }
                        .testTag("stats_achievements_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NeonEmerald)
                            Column {
                                Text("Conquistas e Gamificação", fontWeight = FontWeight.Bold)
                                Text("$unlockedAchievements de ${achievements.size} desbloqueadas", fontSize = 12.sp)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // Top Valued Items
            item {
                Text("Itens Mais Valiosos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            val topItems = items.sortedByDescending { it.estimatedValue }.take(5)
            items(topItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("${item.collection} • ${item.condition}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(currency.formatValue(item.estimatedValue), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
