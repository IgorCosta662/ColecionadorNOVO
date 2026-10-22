package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.RealCatalogEntry
import com.example.data.RealMarketCatalog
import com.example.ui.CollectorViewModel
import com.example.ui.theme.LigaMagicBlue
import com.example.ui.theme.LigaPokemonOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: CollectorViewModel,
    navController: NavController? = null
) {
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("Todos") }

    val entries = when (selectedFilter) {
        "LigaMagic" -> RealMarketCatalog.allEntries.filter { it.sourceMarket == "LigaMagic" }
        "LigaPokémon" -> RealMarketCatalog.allEntries.filter { it.sourceMarket == "LigaPokemon" }
        else -> RealMarketCatalog.allEntries
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cotações e Mercado Oficial Brasil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (navController?.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Source Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "Todos",
                    onClick = { selectedFilter = "Todos" },
                    label = { Text("Todos os Mercados") }
                )
                FilterChip(
                    selected = selectedFilter == "LigaMagic",
                    onClick = { selectedFilter = "LigaMagic" },
                    label = { Text("LigaMagic (MTG)") }
                )
                FilterChip(
                    selected = selectedFilter == "LigaPokémon",
                    onClick = { selectedFilter = "LigaPokémon" },
                    label = { Text("LigaPokémon") }
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(entries) { entry ->
                    MarketEntryCard(entry = entry, formattedValue = currency.formatValue(entry.realMarketPriceBrl))
                }
            }
        }
    }
}

@Composable
fun MarketEntryCard(
    entry: RealCatalogEntry,
    formattedValue: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
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
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = entry.imageUrl,
                    contentDescription = entry.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (entry.sourceMarket == "LigaMagic") LigaMagicBlue else LigaPokemonOrange
                    ) {
                        Text(
                            entry.sourceMarket,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Text("${entry.collection} • ${entry.itemNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Menor: R$ ${String.format(java.util.Locale.US, "%.2f", entry.lowPriceBrl)} | Maior: R$ ${String.format(java.util.Locale.US, "%.2f", entry.highPriceBrl)}", fontSize = 11.sp)
            }

            Text(formattedValue, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
        }
    }
}
