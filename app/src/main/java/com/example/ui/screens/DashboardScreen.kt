package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.CollectorViewModel
import com.example.ui.components.AppModeToggleHeader
import com.example.ui.components.CollectionCategoriesSection
import com.example.ui.components.CollectorToolsSection
import com.example.ui.components.ExactCollectorToolsGrid
import com.example.ui.components.ExactHeroPortfolioGrid
import com.example.ui.components.ExactQuickActionsGrid
import com.example.ui.navigation.Routes
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: CollectorViewModel,
    navController: NavController,
    onOpenDrawer: (() -> Unit)? = null
) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val appMode by viewModel.appMode.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val achievements by viewModel.achievements.collectAsStateWithLifecycle()

    val totalValueBrl = items.sumOf { it.totalEstimatedValue }
    val totalCostBrl = items.sumOf { it.totalPurchasePrice }
    val totalProfitBrl = totalValueBrl - totalCostBrl
    val profitPercent = if (totalCostBrl > 0) ((totalProfitBrl) / totalCostBrl) * 100.0 else 0.0

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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Style,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "Colecionador",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (appMode == AppMode.INVESTOR) "Mercado LigaMagic & LigaPokémon" else "Gestão de Coleções & Sets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Routes.ACHIEVEMENTS) },
                        modifier = Modifier.testTag("achievements_top_button")
                    ) {
                        Badge(
                            containerColor = NeonEmerald
                        ) {
                            val count = achievements.count { it.isUnlocked }
                            Text("$count", color = Color.White, fontSize = 10.sp)
                        }
                    }
                    IconButton(
                        onClick = {
                            val nextCurr = when (currency) {
                                AppCurrency.BRL -> AppCurrency.USD
                                AppCurrency.USD -> AppCurrency.EUR
                                else -> AppCurrency.BRL
                            }
                            viewModel.setCurrency(nextCurr)
                        }
                    ) {
                        Text(currency.symbol, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações & Chave de IA")
                    }
                }
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
            // App Mode Switcher (Colecionador vs Investidor)
            item {
                AppModeToggleHeader(
                    appMode = appMode,
                    onToggle = { viewModel.toggleAppMode() }
                )
            }

            // 4 Hero Metric Cards from Screenshot 2 (Valor total, Valorização, Itens, Mais valioso)
            item {
                val mostValuable = items.maxByOrNull { it.estimatedValue }
                ExactHeroPortfolioGrid(
                    totalValueFormatted = currency.formatValue(totalValueBrl),
                    profitFormatted = "${if (totalProfitBrl >= 0) "+" else ""}${currency.formatValue(totalProfitBrl)}",
                    totalItems = items.size,
                    mostValuableFormatted = if (mostValuable != null) currency.formatValue(mostValuable.estimatedValue) else "R$ 0,00"
                )
            }

            // Ações Rápidas (Screenshot 1: Escanear, Adicionar, Pesquisar, Ver Coleção, Catálogo, Checklists, Mercado, Estatísticas)
            item {
                ExactQuickActionsGrid(itemCount = items.size, navController = navController)
            }

            // Ferramentas do Colecionador (Screenshot 2: Gestão Avançada com os 10 cartões coloridos + Importar)
            item {
                ExactCollectorToolsGrid(navController = navController)
            }

            // Categorias da Coleção (Screenshot 1: Visualização Isolada - TCG, Diecast, Action Figures, Moedas, Colecionáveis, Outros)
            item {
                CollectionCategoriesSection(items = items, navController = navController)
            }

            // Investor Market Quotation Direct Banner (when in investor mode)
            if (appMode == AppMode.INVESTOR) {
                item {
                    MarketSourcesBanner()
                }
            }

            // Recent or Top Highlights
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (appMode == AppMode.INVESTOR) "Maiores Cotações do Portfólio" else "Destaques da Coleção",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { navController.navigate(Routes.COLLECTION) }) {
                        Text("Ver Todos")
                    }
                }
            }

            val highlightItems = if (appMode == AppMode.INVESTOR) {
                items.sortedByDescending { it.estimatedValue }.take(5)
            } else {
                items.sortedByDescending { it.dateAdded }.take(5)
            }

            if (highlightItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp))
                            Text("Sua coleção está vazia", fontWeight = FontWeight.Bold)
                            Text("Adicione cartas ou carrinhos para ver a análise completa.")
                            Button(
                                onClick = { navController.navigate(Routes.ADD_ITEM) },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Adicionar Primeiro Item")
                            }
                        }
                    }
                }
            } else {
                items(highlightItems) { item ->
                    ItemSummaryCard(
                        item = item,
                        currency = currency,
                        appMode = appMode,
                        onClick = { navController.navigate(Routes.itemDetail(item.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun InvestorHeroCard(
    totalValue: String,
    totalCost: String,
    profit: String,
    profitPercent: Double,
    itemCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "VALOR TOTAL ESTIMADO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF312E81)
                ) {
                    Text(
                        "$itemCount Ativos",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                totalValue,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Custo de Aquisição", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(totalCost, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Lucro / Variação", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$profit (${String.format(java.util.Locale.US, "%.1f", profitPercent)}%)",
                            fontWeight = FontWeight.Bold,
                            color = if (profitPercent >= 0) NeonEmerald else NeonRose
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollectorHeroCard(
    totalItems: Int,
    cardsCount: Int,
    diecastCount: Int,
    gradedCount: Int,
    totalEstimated: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SUA COLEÇÃO PESSOAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF047857)
                ) {
                    Text(
                        "$totalItems Peças",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "$totalItems Itens Catalogados",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Avaliação total: $totalEstimated",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$cardsCount", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text("Cartas TCG", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$diecastCount", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text("Carrinhos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$gradedCount", fontWeight = FontWeight.Bold, color = SlabGold, fontSize = 16.sp)
                    Text("Slabs / Graded", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun MarketSourcesBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E40AF)),
                contentAlignment = Alignment.Center
            ) {
                Text("BR", color = Color.White, fontWeight = FontWeight.Black)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Cotações Oficiais Integradas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "LigaMagic e LigaPokémon com conversão em BRL, preços mínimos, médios e máximos de lojas brasileiras.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ItemSummaryCard(
    item: Item,
    currency: AppCurrency,
    appMode: AppMode,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image / Thumbnail
            val displayImage = item.officialImageUrl ?: item.imageUri
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (displayImage.isNotEmpty()) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        if (item.isCard) Icons.Default.Style else Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Info Column
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isGraded) {
                        Surface(shape = RoundedCornerShape(4.dp), color = SlabGold) {
                            Text(
                                item.gradeScore ?: "GRADED",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    "${item.collection} • ${item.itemNumber}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    item.condition,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Price / Financial Column
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    currency.formatValue(item.estimatedValue),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (appMode == AppMode.INVESTOR && item.purchasePrice > 0) {
                    val profitVal = item.profit
                    Text(
                        "${if (profitVal >= 0) "+" else ""}${currency.formatValue(profitVal)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (profitVal >= 0) NeonEmerald else NeonRose
                    )
                }
            }
        }
    }
}
