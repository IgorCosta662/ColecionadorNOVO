package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Style
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.api.ScryfallApiClient
import com.example.data.CardImageResolver
import com.example.data.DeckCard
import com.example.ui.CollectorViewModel
import com.example.ui.components.CardDetailModalSheet
import com.example.ui.components.ImportDecklistDialog
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    viewModel: CollectorViewModel,
    navController: NavController,
    deckId: Int
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val decksWithCards by viewModel.allDecksWithCards.collectAsStateWithLifecycle()
    val deckWithCards = decksWithCards.find { it.deck.id == deckId }

    var showAddCardDialog by remember { mutableStateOf(false) }
    var showPasteDecklistDialog by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<DeckCard?>(null) }
    var showStrategyDetails by remember { mutableStateOf(true) }

    var cardName by remember { mutableStateOf("") }
    var cardQuantityStr by remember { mutableStateOf("1") }
    var cardType by remember { mutableStateOf("Criatura / Mágica") }
    var cardPriceStr by remember { mutableStateOf("5.0") }
    var cardImageUrl by remember { mutableStateOf("") }
    var isSearchingScryfall by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deckWithCards?.deck?.name ?: "Baralho", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showPasteDecklistDialog = true }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Colar Lista")
                    }
                    IconButton(onClick = { showAddCardDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Carta")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCardDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Carta")
            }
        }
    ) { padding ->
        if (deckWithCards == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Baralho não encontrado.")
            }
        } else {
            val totalCards = deckWithCards.cards.sumOf { it.quantity }
            val totalCost = deckWithCards.cards.sumOf { it.estimatedPriceBrl * it.quantity }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Deck Header Card
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    deckWithCards.deck.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        deckWithCards.deck.format,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                deckWithCards.deck.game,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )

                            if (deckWithCards.deck.description.isNotEmpty()) {
                                Text(deckWithCards.deck.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total de Cartas: $totalCards",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Custo: R$ ${String.format(Locale.US, "%.2f", totalCost)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                            }

                            // Quick bulk import button row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showPasteDecklistDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Colar Lista Completa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showAddCardDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Adicionar Carta", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Deck Strategy & How to Play
                item {
                    val rawDesc = deckWithCards.deck.description
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        "Estratégia do Deck & Como Jogar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                TextButton(onClick = { showStrategyDetails = !showStrategyDetails }) {
                                    Text(if (showStrategyDetails) "Ocultar" else "Ver Detalhes", fontSize = 12.sp)
                                }
                            }

                            if (showStrategyDetails) {
                                Text(
                                    text = if (rawDesc.isNotBlank()) rawDesc else "Construa seu campo combinando aceleração de recursos e criaturas de combate para pressionar os oponentes e alcançar a condição de vitória.",
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Text(
                                            "Dica: Toque em qualquer carta da lista abaixo para ver o que ela faz em português, custo/stats e seu papel no deck!",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Cartas no Deck (${deckWithCards.cards.size} tipos • $totalCards cópias)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        FilledTonalButton(
                            onClick = {
                                viewModel.enrichDeckCardPhotos(deckId) { count ->
                                    Toast.makeText(
                                        context,
                                        "✓ Fotos atualizadas para $count carta(s)!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atualizar Fotos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (deckWithCards.cards.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("Nenhuma carta no deck ainda", fontWeight = FontWeight.Bold)
                                Text("Toque em 'Colar Lista Completa' para adicionar todas as cartas de uma vez, ou adicione carta por carta.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                items(deckWithCards.cards) { card ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCardForDetail = card },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Card Image Thumbnail
                            val resolvedImg = card.imageUrl.ifBlank {
                                CardImageResolver.resolveImageUrl(card.name, deckWithCards.deck.game)
                            }
                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 66.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (resolvedImg.isNotBlank()) {
                                    AsyncImage(
                                        model = resolvedImg,
                                        contentDescription = card.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Style,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("${card.quantity}x ${card.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (card.manaCost.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                                        ) {
                                            Text(
                                                card.manaCost,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    "${card.cardType} • Cotação: R$ ${String.format(Locale.US, "%.2f", card.estimatedPriceBrl)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    "👆 Toque para ver efeito completo & regras",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { selectedCardForDetail = card }) {
                                    Icon(Icons.Default.Info, contentDescription = "Ver Efeito", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.removeCardFromDeck(card) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Card Detail & Rules
    if (selectedCardForDetail != null && deckWithCards != null) {
        CardDetailModalSheet(
            card = selectedCardForDetail!!,
            deckGame = deckWithCards.deck.game,
            onDismiss = { selectedCardForDetail = null }
        )
    }

    // Dialog: Bulk Paste Decklist
    if (showPasteDecklistDialog && deckWithCards != null) {
        ImportDecklistDialog(
            initialGame = deckWithCards.deck.game,
            onDismiss = { showPasteDecklistDialog = false },
            onImportCards = { parsedCards ->
                val cardEntities = parsedCards.map { c ->
                    val img = c.imageUrl.ifBlank {
                        CardImageResolver.resolveImageUrl(c.name, deckWithCards.deck.game)
                    }
                    DeckCard(
                        deckId = deckId,
                        name = c.name,
                        quantity = c.quantity,
                        cardType = c.cardType,
                        estimatedPriceBrl = c.estimatedPriceBrl,
                        imageUrl = img
                    )
                }
                viewModel.addCardsToDeck(cardEntities) {
                    Toast.makeText(
                        context,
                        "✓ ${cardEntities.sumOf { it.quantity }} cartas adicionadas ao deck com fotos!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                showPasteDecklistDialog = false
            }
        )
    }

    // Dialog: Add Single Card (with Scryfall Auto-Search)
    if (showAddCardDialog && deckWithCards != null) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Adicionar Carta ao Deck", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = cardName,
                            onValueChange = { cardName = it },
                            label = { Text("Nome da Carta") },
                            placeholder = { Text("Ex: Sol Ring, Lightning Bolt...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        if (deckWithCards.deck.game.contains("Magic", ignoreCase = true)) {
                            FilledTonalButton(
                                onClick = {
                                    if (cardName.isNotBlank() && !isSearchingScryfall) {
                                        isSearchingScryfall = true
                                        coroutineScope.launch {
                                            try {
                                                val match = ScryfallApiClient.getBestCardMatch(cardName)
                                                if (match != null) {
                                                    cardName = match.name
                                                    cardType = match.typeLine ?: "Mágica"
                                                    cardImageUrl = match.imageUrlLarge.ifBlank { match.imageUrlNormal }
                                                    if (match.priceUsd != null && match.priceUsd > 0.0) {
                                                        cardPriceStr = String.format(Locale.US, "%.2f", match.priceUsd * 5.75)
                                                    }
                                                    Toast.makeText(context, "✓ '${match.name}' encontrada no Scryfall!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Carta não encontrada no Scryfall.", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Erro Scryfall: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isSearchingScryfall = false
                                            }
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                if (isSearchingScryfall) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Scryfall", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cardQuantityStr,
                            onValueChange = { cardQuantityStr = it },
                            label = { Text("Quantidade") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cardPriceStr,
                            onValueChange = { cardPriceStr = it },
                            label = { Text("Preço (R$)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = cardType,
                        onValueChange = { cardType = it },
                        label = { Text("Tipo (ex: Criatura, Terreno, Feitiço, Pokémon)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cardName.isNotBlank()) {
                            val resolvedImg = cardImageUrl.ifBlank {
                                CardImageResolver.resolveImageUrl(cardName, deckWithCards.deck.game)
                            }
                            viewModel.addCardToDeck(
                                DeckCard(
                                    deckId = deckId,
                                    name = cardName,
                                    quantity = cardQuantityStr.toIntOrNull() ?: 1,
                                    cardType = cardType,
                                    estimatedPriceBrl = cardPriceStr.toDoubleOrNull() ?: 0.0,
                                    imageUrl = resolvedImg
                                )
                            )
                            showAddCardDialog = false
                            cardName = ""
                            cardQuantityStr = "1"
                            cardImageUrl = ""
                        }
                    }
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
