package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.Deck
import com.example.data.DeckCard
import com.example.ui.CollectorViewModel
import com.example.ui.components.ImportDecklistDialog
import com.example.ui.components.PreconPickerSheet
import com.example.ui.navigation.Routes
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecksListScreen(
    viewModel: CollectorViewModel,
    navController: NavController,
    onOpenDrawer: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val decksWithCards by viewModel.allDecksWithCards.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showPreconPicker by remember { mutableStateOf(false) }
    var showPasteDecklistDialog by remember { mutableStateOf(false) }

    var newDeckName by remember { mutableStateOf("") }
    var newDeckGame by remember { mutableStateOf("Magic: The Gathering") }
    var newDeckFormat by remember { mutableStateOf("Commander") }

    var selectedDeckGameFilter by remember { mutableStateOf("Todos") }
    var deckSearchQuery by remember { mutableStateOf("") }

    val filteredDecksWithCards = remember(decksWithCards, selectedDeckGameFilter, deckSearchQuery) {
        val q = deckSearchQuery.trim().lowercase()
        decksWithCards.filter { deckWithCards ->
            val deck = deckWithCards.deck
            val matchesGame = when (selectedDeckGameFilter) {
                "Magic: The Gathering" -> deck.game.contains("Magic", ignoreCase = true)
                "Pokémon TCG" -> deck.game.contains("Pokémon", ignoreCase = true)
                "Yu-Gi-Oh!" -> deck.game.contains("Yu-Gi-Oh", ignoreCase = true)
                "One Piece" -> deck.game.contains("One Piece", ignoreCase = true)
                else -> true
            }
            val matchesSearch = q.isBlank() ||
                deck.name.lowercase().contains(q) ||
                deck.game.lowercase().contains(q) ||
                deck.format.lowercase().contains(q) ||
                deck.commanderOrLeader.lowercase().contains(q) ||
                deck.description.lowercase().contains(q)
            matchesGame && matchesSearch
        }
    }

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
                title = { Text("Gerenciador de Decks", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showPreconPicker = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Importar Precon")
                    }
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Criar Deck")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Deck")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header / Quick actions banner for Precons and Manual cardlist
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Baralhos, Precons & Listas Prontas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            "Adicione facilmente seus Commander Precons de Magic, Baralhos de Batalha Pokémon ou cole a lista de cartas completa de qualquer site.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showPreconPicker = true },
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Importar Precon", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { showPasteDecklistDialog = true },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Colar Lista", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Decks list
            if (decksWithCards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Style,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Nenhum baralho cadastrado ainda", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "Toque em 'Importar Precon' para adicionar um deck pronto em 1 segundo, ou crie seu próprio baralho.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 32.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { showPreconPicker = true }) {
                                    Text("Escolher Precon Pronto")
                                }
                                OutlinedButton(onClick = { showCreateDialog = true }) {
                                    Text("Criar Manualmente")
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (selectedDeckGameFilter == "Todos" && deckSearchQuery.isBlank())
                                    "Meus Decks (${decksWithCards.size})"
                                else
                                    "Meus Decks (${filteredDecksWithCards.size} de ${decksWithCards.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                "Total: ${decksWithCards.sumOf { it.cards.sumOf { c -> c.quantity } }} cartas",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Search in created decks
                        OutlinedTextField(
                            value = deckSearchQuery,
                            onValueChange = { deckSearchQuery = it },
                            placeholder = { Text("Buscar nos meus decks...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (deckSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { deckSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                    }
                                }
                            }
                        )

                        // TCG Filter Chips (Magic, Pokémon, Yu-Gi-Oh!, One Piece)
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(listOf("Todos", "Magic: The Gathering", "Pokémon TCG", "Yu-Gi-Oh!", "One Piece")) { filter ->
                                FilterChip(
                                    selected = selectedDeckGameFilter == filter,
                                    onClick = { selectedDeckGameFilter = filter },
                                    label = { Text(filter, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                if (filteredDecksWithCards.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Nenhum deck encontrado neste filtro", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                TextButton(onClick = {
                                    selectedDeckGameFilter = "Todos"
                                    deckSearchQuery = ""
                                }) {
                                    Text("Limpar Filtros")
                                }
                            }
                        }
                    }
                }

                items(filteredDecksWithCards, key = { it.deck.id }) { deckWithCards ->
                    val deck = deckWithCards.deck
                    val totalCards = deckWithCards.cards.sumOf { it.quantity }
                    val totalCost = deckWithCards.cards.sumOf { it.estimatedPriceBrl * it.quantity }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { navController.navigate(Routes.deckDetail(deck.id)) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val coverImg = deck.coverImageUrl.ifBlank {
                                deckWithCards.cards.firstOrNull { it.imageUrl.isNotBlank() }?.imageUrl ?: ""
                            }
                            if (coverImg.isNotBlank()) {
                                AsyncImage(
                                    model = coverImg,
                                    contentDescription = deck.name,
                                    modifier = Modifier
                                        .size(width = 46.dp, height = 64.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(deck.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            deck.format,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    deck.game,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    "$totalCards cartas • Estimado: R$ ${String.format(Locale.US, "%.2f", totalCost)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = { viewModel.deleteDeck(deck) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir Deck", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet: Precon Picker
    if (showPreconPicker) {
        val importSinglePrecon: (com.example.data.PreconDeckTemplate) -> Unit = { precon ->
            val desc = buildString {
                append(precon.description)
                if (precon.commanderOrLead.isNotBlank()) {
                    append("\n\n👑 Comandante / Líder: ${precon.commanderOrLead}")
                }
                if (precon.strategyGuide.isNotBlank()) {
                    append("\n\n🎯 ESTRATÉGIA DO DECK:\n${precon.strategyGuide}")
                }
                if (precon.winCondition.isNotBlank()) {
                    append("\n\n🏆 CONDIÇÃO DE VITÓRIA:\n${precon.winCondition}")
                }
                if (precon.keyCombos.isNotEmpty()) {
                    append("\n\n⚡ COMBOS E SINERGIAS:\n")
                    precon.keyCombos.forEach { combo ->
                        append("• $combo\n")
                    }
                }
            }
            val cardEntities = precon.cardList.map { cardTemplate ->
                val ruleInfo = com.example.data.CardRulesEncyclopedia.getCardInfo(cardTemplate.name, precon.game)
                val img = cardTemplate.imageUrl.ifBlank {
                    ruleInfo.imageUrl.ifBlank {
                        com.example.data.CardImageResolver.resolveImageUrl(cardTemplate.name, precon.game)
                    }
                }
                DeckCard(
                    deckId = 0,
                    name = cardTemplate.name,
                    quantity = cardTemplate.quantity,
                    cardType = if (cardTemplate.cardType != "Mágica / Pokémon") cardTemplate.cardType else ruleInfo.cardType,
                    manaCost = cardTemplate.manaCostOrStats.ifBlank { ruleInfo.manaCostOrStats },
                    oracleText = cardTemplate.oracleText.ifBlank { ruleInfo.oracleEn },
                    translatedEffectPt = cardTemplate.effectPt.ifBlank { ruleInfo.effectPt },
                    estimatedPriceBrl = cardTemplate.estimatedPriceBrl,
                    imageUrl = img
                )
            }
            val newDeck = Deck(
                name = precon.name,
                game = precon.game,
                format = precon.format,
                commanderOrLeader = precon.commanderOrLead,
                description = desc,
                coverImageUrl = precon.coverImageUrl.ifBlank { cardEntities.firstOrNull { it.imageUrl.isNotBlank() }?.imageUrl ?: "" }
            )
            viewModel.importPreconDeck(newDeck, cardEntities) { createdDeckId ->
                Toast.makeText(
                    context,
                    "✓ Deck '${precon.name}' importado com fotos oficiais!",
                    Toast.LENGTH_LONG
                ).show()
                navController.navigate(Routes.deckDetail(createdDeckId.toInt()))
            }
        }

        PreconPickerSheet(
            onDismiss = { showPreconPicker = false },
            onPreconSelected = { precon ->
                showPreconPicker = false
                if (precon.isMultiDeckBox && precon.boxDecks.isNotEmpty()) {
                    // Import both separated decks directly
                    val prepared = precon.boxDecks.map { subDeck ->
                        val desc = buildString {
                            append(subDeck.description)
                            if (subDeck.commanderOrLead.isNotBlank()) append("\n\n👑 Líder: ${subDeck.commanderOrLead}")
                            if (subDeck.strategyGuide.isNotBlank()) append("\n\n🎯 ESTRATÉGIA:\n${subDeck.strategyGuide}")
                            if (subDeck.winCondition.isNotBlank()) append("\n\n🏆 VITÓRIA:\n${subDeck.winCondition}")
                        }
                        val cards = subDeck.cardList.map { c ->
                            val ruleInfo = com.example.data.CardRulesEncyclopedia.getCardInfo(c.name, subDeck.game)
                            val img = c.imageUrl.ifBlank {
                                ruleInfo.imageUrl.ifBlank {
                                    com.example.data.CardImageResolver.resolveImageUrl(c.name, subDeck.game)
                                }
                            }
                            DeckCard(
                                deckId = 0,
                                name = c.name,
                                quantity = c.quantity,
                                cardType = if (c.cardType != "Mágica / Pokémon") c.cardType else ruleInfo.cardType,
                                manaCost = c.manaCostOrStats.ifBlank { ruleInfo.manaCostOrStats },
                                oracleText = c.oracleText.ifBlank { ruleInfo.oracleEn },
                                translatedEffectPt = c.effectPt.ifBlank { ruleInfo.effectPt },
                                estimatedPriceBrl = c.estimatedPriceBrl,
                                imageUrl = img
                            )
                        }
                        val deck = Deck(
                            name = subDeck.name,
                            game = subDeck.game,
                            format = subDeck.format,
                            commanderOrLeader = subDeck.commanderOrLead,
                            description = desc,
                            coverImageUrl = subDeck.coverImageUrl.ifBlank { cards.firstOrNull { it.imageUrl.isNotBlank() }?.imageUrl ?: "" }
                        )
                        Pair(deck, cards)
                    }
                    viewModel.importMultiplePreconDecks(prepared) {
                        Toast.makeText(
                            context,
                            "✓ Os 2 baralhos (Cloud e Sephiroth) foram criados SEPARADAMENTE na sua coleção com fotos!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    importSinglePrecon(precon)
                }
            },
            onImportMultiplePrecons = { boxDecks ->
                showPreconPicker = false
                val prepared = boxDecks.map { subDeck ->
                    val desc = buildString {
                        append(subDeck.description)
                        if (subDeck.commanderOrLead.isNotBlank()) append("\n\n👑 Líder: ${subDeck.commanderOrLead}")
                        if (subDeck.strategyGuide.isNotBlank()) append("\n\n🎯 ESTRATÉGIA:\n${subDeck.strategyGuide}")
                        if (subDeck.winCondition.isNotBlank()) append("\n\n🏆 VITÓRIA:\n${subDeck.winCondition}")
                    }
                    val cards = subDeck.cardList.map { c ->
                        val ruleInfo = com.example.data.CardRulesEncyclopedia.getCardInfo(c.name, subDeck.game)
                        val img = c.imageUrl.ifBlank {
                            ruleInfo.imageUrl.ifBlank {
                                com.example.data.CardImageResolver.resolveImageUrl(c.name, subDeck.game)
                            }
                        }
                        DeckCard(
                            deckId = 0,
                            name = c.name,
                            quantity = c.quantity,
                            cardType = if (c.cardType != "Mágica / Pokémon") c.cardType else ruleInfo.cardType,
                            manaCost = c.manaCostOrStats.ifBlank { ruleInfo.manaCostOrStats },
                            oracleText = c.oracleText.ifBlank { ruleInfo.oracleEn },
                            translatedEffectPt = c.effectPt.ifBlank { ruleInfo.effectPt },
                            estimatedPriceBrl = c.estimatedPriceBrl,
                            imageUrl = img
                        )
                    }
                    val deck = Deck(
                        name = subDeck.name,
                        game = subDeck.game,
                        format = subDeck.format,
                        commanderOrLeader = subDeck.commanderOrLead,
                        description = desc,
                        coverImageUrl = subDeck.coverImageUrl.ifBlank { cards.firstOrNull { it.imageUrl.isNotBlank() }?.imageUrl ?: "" }
                    )
                    Pair(deck, cards)
                }
                viewModel.importMultiplePreconDecks(prepared) {
                    Toast.makeText(
                        context,
                        "✓ ${boxDecks.size} baralhos independentes adicionados à sua coleção com fotos!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    // Dialog: Paste Decklist
    if (showPasteDecklistDialog) {
        var deckNameInput by remember { mutableStateOf("Meu Deck Importado") }
        var deckGameInput by remember { mutableStateOf("Magic: The Gathering") }
        var deckFormatInput by remember { mutableStateOf("Commander") }

        AlertDialog(
            onDismissRequest = { showPasteDecklistDialog = false },
            title = { Text("Criar Deck Colando Lista de Cartas", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = deckNameInput,
                        onValueChange = { deckNameInput = it },
                        label = { Text("Nome do Deck") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = deckGameInput,
                            onValueChange = { deckGameInput = it },
                            label = { Text("Jogo") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = deckFormatInput,
                            onValueChange = { deckFormatInput = it },
                            label = { Text("Formato") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    ImportDecklistDialog(
                        initialGame = deckGameInput,
                        onDismiss = { showPasteDecklistDialog = false },
                        onImportCards = { parsedCards ->
                            showPasteDecklistDialog = false
                            val newDeck = Deck(
                                name = deckNameInput.ifBlank { "Deck Importado" },
                                game = deckGameInput,
                                format = deckFormatInput
                            )
                            val cardEntities = parsedCards.map { c ->
                                DeckCard(
                                    deckId = 0,
                                    name = c.name,
                                    quantity = c.quantity,
                                    cardType = c.cardType,
                                    estimatedPriceBrl = c.estimatedPriceBrl
                                )
                            }
                            viewModel.importPreconDeck(newDeck, cardEntities) { newId ->
                                Toast.makeText(
                                    context,
                                    "✓ Deck criado com ${cardEntities.sumOf { it.quantity }} cartas importadas!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(Routes.deckDetail(newId.toInt()))
                            }
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPasteDecklistDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Modal Dialog: New Empty Deck
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Novo Deck Personalizado", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newDeckName,
                        onValueChange = { newDeckName = it },
                        label = { Text("Nome do Baralho") },
                        placeholder = { Text("Ex: Dinossauros Naya, Charizard ex...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Escolha rápida do TCG:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(listOf(
                            "Magic: The Gathering" to "Commander",
                            "Pokémon TCG" to "Standard",
                            "Yu-Gi-Oh!" to "Avançado (TCG)",
                            "One Piece Card Game" to "Standard (50 cartas + Líder)"
                        )) { (game, defaultFormat) ->
                            AssistChip(
                                onClick = {
                                    newDeckGame = game
                                    newDeckFormat = defaultFormat
                                },
                                label = {
                                    Text(
                                        when (game) {
                                            "Magic: The Gathering" -> "Magic"
                                            "Pokémon TCG" -> "Pokémon"
                                            "Yu-Gi-Oh!" -> "Yu-Gi-Oh"
                                            else -> "One Piece"
                                        },
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = newDeckGame,
                        onValueChange = { newDeckGame = it },
                        label = { Text("Jogo / TCG") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDeckFormat,
                        onValueChange = { newDeckFormat = it },
                        label = { Text("Formato") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDeckName.isNotBlank()) {
                            viewModel.saveDeck(
                                Deck(name = newDeckName, game = newDeckGame, format = newDeckFormat)
                            ) { createdId ->
                                showCreateDialog = false
                                newDeckName = ""
                                if (createdId > 0) {
                                    navController.navigate(Routes.deckDetail(createdId.toInt()))
                                }
                            }
                        }
                    }
                ) {
                    Text("Criar e Abrir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
