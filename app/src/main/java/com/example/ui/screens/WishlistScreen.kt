package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.WishlistItem
import com.example.ui.CollectorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    viewModel: CollectorViewModel,
    navController: NavController
) {
    val wishlist by viewModel.allWishlist.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var targetMaxPriceStr by remember { mutableStateOf("") }
    var currentMarketStr by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Alta") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Desejos (Wishlist)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Desejo")
            }
        }
    ) { padding ->
        if (wishlist.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Bookmarks, contentDescription = null, modifier = Modifier.size(48.dp))
                    Text("Nenhum item na sua lista de desejos", fontWeight = FontWeight.Bold)
                    Text("Salve cartas e miniaturas que você planeja comprar.")
                    Button(onClick = { showAddDialog = true }) {
                        Text("Adicionar Desejo")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wishlist) { wish ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(wish.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Meta de Preço: ${currency.formatValue(wish.targetMaxPrice)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Mercado Atual: ${currency.formatValue(wish.currentMarketEstimate)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.deleteWishlist(wish) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Novo Item Desejado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Nome da Carta / Carrinho") }, singleLine = true)
                    OutlinedTextField(value = targetMaxPriceStr, onValueChange = { targetMaxPriceStr = it }, label = { Text("Preço Máximo Alvo (R$)") }, singleLine = true)
                    OutlinedTextField(value = currentMarketStr, onValueChange = { currentMarketStr = it }, label = { Text("Preço de Mercado Atual (R$)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (itemName.isNotBlank()) {
                            viewModel.saveWishlist(
                                WishlistItem(
                                    name = itemName,
                                    category = "Trading Cards",
                                    targetMaxPrice = targetMaxPriceStr.toDoubleOrNull() ?: 0.0,
                                    currentMarketEstimate = currentMarketStr.toDoubleOrNull() ?: 0.0,
                                    priority = priority
                                )
                            )
                            showAddDialog = false
                            itemName = ""
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
