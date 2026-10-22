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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.CollectorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CollectorViewModel,
    navController: NavController
) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedComparisonIds.collectAsStateWithLifecycle()

    val comparedItems = items.filter { selectedIds.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparador de Cartas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (selectedIds.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearComparison() }) {
                            Text("Limpar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (comparedItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(56.dp))
                    Text("Nenhuma carta selecionada", fontWeight = FontWeight.Bold)
                    Text("Abra uma carta na coleção e clique no ícone de comparação.")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Comparando ${comparedItems.size} itens lado a lado", fontWeight = FontWeight.Bold)
                }

                items(comparedItems) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val img = item.officialImageUrl ?: item.imageUri
                            Box(
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (img.isNotEmpty()) {
                                    AsyncImage(model = img, contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("${item.collection} • #${item.itemNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Condição: ${item.condition} | Raridade: ${item.rarity}", fontSize = 12.sp)
                                Text("Cotação Brasil: ${currency.formatValue(item.estimatedValue)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                if (item.purchasePrice > 0) {
                                    Text("Preço Pago: ${currency.formatValue(item.purchasePrice)}", fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { viewModel.toggleComparisonItem(item.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remover")
                            }
                        }
                    }
                }
            }
        }
    }
}
