package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.Item
import com.example.data.RealCatalogEntry
import com.example.data.RealMarketCatalog
import com.example.ui.CollectorViewModel
import com.example.ui.components.ScryfallArtPickerSheet
import com.example.ui.theme.LigaMagicBlue
import com.example.ui.theme.LigaPokemonOrange
import com.example.ui.theme.NeonEmerald
import com.example.util.CardKnowledgeBase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    viewModel: CollectorViewModel,
    navController: NavController,
    itemId: Int? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Active Screen Tab: 0 = Buscar no Catálogo (Rápido), 1 = Formulário Manual
    var selectedTab by remember { mutableIntStateOf(0) }

    // Form fields state
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Trading Cards") }
    var subCategory by remember { mutableStateOf("Pokémon TCG") }
    var collection by remember { mutableStateOf("") }
    var itemNumber by remember { mutableStateOf("") }
    var rarity by remember { mutableStateOf("") }
    var variant by remember { mutableStateOf("Normal") }
    var condition by remember { mutableStateOf("Near Mint") }
    var purchasePriceStr by remember { mutableStateOf("") }
    var estimatedValueStr by remember { mutableStateOf("") }
    var officialImageUrl by remember { mutableStateOf<String?>(null) }
    var showScryfallPickerSheet by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var storageLocation by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("1") }

    // Card Effects, HP, Artist & Translation fields
    var cardAttacks by remember { mutableStateOf("") }
    var cardTranslatedEffect by remember { mutableStateOf("") }
    var cardOracleText by remember { mutableStateOf("") }
    var cardHp by remember { mutableStateOf("") }
    var cardArtist by remember { mutableStateOf("") }
    var energyType by remember { mutableStateOf("") }
    var releaseYearStr by remember { mutableStateOf("") }

    // Catalog Search & Browse State
    var catalogSearchQuery by remember { mutableStateOf("") }
    var selectedCatalogCategory by remember { mutableStateOf("Todas as Cartas") }

    // Bottom Sheet Preview for clicked card
    var previewCardEntry by remember { mutableStateOf<RealCatalogEntry?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories = listOf("Trading Cards", "Carrinhos / Diecast", "Action Figures", "Moedas", "Outros")
    val subCategories = listOf("Pokémon TCG", "Magic: The Gathering", "Yu-Gi-Oh!", "Hot Wheels", "Mini GT")

    fun applyCardEffects(cardName: String, cat: String, subCat: String) {
        val effect = CardKnowledgeBase.findOrGenerateCardEffect(cardName, cat, subCat)
        cardTranslatedEffect = effect.translatedEffectPt
        cardOracleText = effect.originalOracle
        cardAttacks = effect.cardAttacks ?: ""
        cardHp = effect.cardHp ?: ""
        cardArtist = effect.cardArtist ?: ""

        val combined = "$cardName $cat $subCat".lowercase()
        if (energyType.isBlank()) {
            energyType = effect.manaColor ?: effect.cardType ?: when {
                combined.contains("preto") || combined.contains("black") || combined.contains("sheoldred") -> "Preto"
                combined.contains("azul") || combined.contains("blue") || combined.contains("counterspell") -> "Azul"
                combined.contains("vermelho") || combined.contains("red") || combined.contains("the thing") || combined.contains("lightning bolt") -> "Vermelho"
                combined.contains("branco") || combined.contains("white") -> "Branco"
                combined.contains("verde") || combined.contains("green") -> "Verde"
                combined.contains("fogo") || combined.contains("fire") || combined.contains("charizard") -> "Fogo"
                combined.contains("água") || combined.contains("water") || combined.contains("blastoise") -> "Água"
                combined.contains("planta") || combined.contains("grass") || combined.contains("venusaur") -> "Planta"
                combined.contains("elétrico") || combined.contains("raio") || combined.contains("lightning") || combined.contains("pikachu") -> "Elétrico / Raios"
                combined.contains("psíquic") || combined.contains("psychic") || combined.contains("mew") -> "Psíquico"
                combined.contains("escuridão") || combined.contains("dark") || combined.contains("umbreon") -> "Escuridão"
                combined.contains("sol ring") || combined.contains("one ring") || combined.contains("black lotus") -> "Incolor"
                else -> ""
            }
        }
    }

    fun buildItemFromEntry(entry: RealCatalogEntry): Item {
        val effect = CardKnowledgeBase.findOrGenerateCardEffect(entry.name, entry.category, entry.subCategory)
        val combined = "${entry.name} ${entry.category} ${entry.subCategory}".lowercase()
        val detectedEnergy = effect.manaColor ?: effect.cardType ?: when {
            combined.contains("preto") || combined.contains("black") || combined.contains("sheoldred") -> "Preto"
            combined.contains("azul") || combined.contains("blue") || combined.contains("counterspell") -> "Azul"
            combined.contains("vermelho") || combined.contains("red") || combined.contains("the thing") || combined.contains("lightning bolt") -> "Vermelho"
            combined.contains("branco") || combined.contains("white") -> "Branco"
            combined.contains("verde") || combined.contains("green") -> "Verde"
            combined.contains("fogo") || combined.contains("fire") || combined.contains("charizard") -> "Fogo"
            combined.contains("água") || combined.contains("water") || combined.contains("blastoise") -> "Água"
            combined.contains("planta") || combined.contains("grass") || combined.contains("venusaur") -> "Planta"
            combined.contains("elétrico") || combined.contains("raio") || combined.contains("pikachu") -> "Elétrico / Raios"
            combined.contains("psíquic") || combined.contains("psychic") || combined.contains("mew") -> "Psíquico"
            combined.contains("escuridão") || combined.contains("dark") || combined.contains("umbreon") -> "Escuridão"
            combined.contains("sol ring") || combined.contains("one ring") || combined.contains("black lotus") -> "Incolor"
            else -> null
        }

        val releaseYearVal = when {
            entry.collection.contains("151") -> 2023
            entry.collection.contains("Foundations") -> 2024
            entry.collection.contains("Duskmourn") -> 2024
            entry.collection.contains("Dominaria") -> 2022
            entry.collection.contains("Evolving Skies") -> 2021
            entry.collection.contains("Marvel") -> 2024
            entry.collection.contains("Lord of the Rings") -> 2023
            entry.collection.contains("Commander Masters") -> 2023
            entry.collection.contains("Silver Tempest") -> 2022
            entry.collection.contains("Lost Origin") -> 2022
            else -> 2024
        }

        return Item(
            name = entry.name,
            category = entry.category,
            subCategory = entry.subCategory,
            collection = entry.collection,
            itemNumber = entry.itemNumber,
            rarity = entry.rarity,
            variant = entry.variant,
            condition = "Near Mint",
            quantity = 1,
            storageLocation = if (entry.category.contains("Card")) "Pasta Oficial" else "Expositor",
            purchasePrice = entry.lowPriceBrl,
            estimatedValue = entry.realMarketPriceBrl,
            officialImageUrl = entry.imageUrl,
            imageUri = entry.imageUrl,
            notes = entry.notes,
            cardHp = effect.cardHp,
            cardArtist = effect.cardArtist,
            cardAttacks = effect.cardAttacks,
            cardOracleText = effect.originalOracle,
            cardTranslatedEffect = effect.translatedEffectPt,
            energyType = detectedEnergy,
            releaseYear = releaseYearVal
        )
    }

    fun fillFormFromEntry(entry: RealCatalogEntry) {
        name = entry.name
        category = entry.category
        subCategory = entry.subCategory
        collection = entry.collection
        itemNumber = entry.itemNumber
        rarity = entry.rarity
        variant = entry.variant
        purchasePriceStr = entry.lowPriceBrl.toString()
        estimatedValueStr = entry.realMarketPriceBrl.toString()
        officialImageUrl = entry.imageUrl
        notes = entry.notes
        storageLocation = if (entry.category.contains("Card")) "Pasta Oficial" else "Expositor"
        releaseYearStr = when {
            entry.collection.contains("151") -> "2023"
            entry.collection.contains("Foundations") -> "2024"
            entry.collection.contains("Duskmourn") -> "2024"
            entry.collection.contains("Dominaria") -> "2022"
            entry.collection.contains("Evolving Skies") -> "2021"
            entry.collection.contains("Marvel") -> "2024"
            entry.collection.contains("Lord of the Rings") -> "2023"
            entry.collection.contains("Commander Masters") -> "2023"
            entry.collection.contains("Silver Tempest") -> "2022"
            entry.collection.contains("Lost Origin") -> "2022"
            else -> "2024"
        }
        applyCardEffects(entry.name, entry.category, entry.subCategory)
    }

    // Filtered catalog entries for instant browsing & search
    val displayedCatalogCards = remember(catalogSearchQuery, selectedCatalogCategory) {
        val q = catalogSearchQuery.trim().lowercase()
        RealMarketCatalog.allEntries.filter { entry ->
            val matchesCategory = when (selectedCatalogCategory) {
                "Todas as Cartas" -> true
                "🔮 Magic: The Gathering" -> entry.subCategory.contains("Magic", ignoreCase = true)
                "⚡ Pokémon TCG" -> entry.subCategory.contains("Pokémon", ignoreCase = true) || entry.subCategory.contains("Pokemon", ignoreCase = true)
                "🏎️ Diecast / Hot Wheels" -> entry.category.contains("Diecast", ignoreCase = true) || entry.category.contains("Carrinho", ignoreCase = true)
                else -> true
            }
            val matchesQuery = q.isBlank() ||
                    entry.name.lowercase().contains(q) ||
                    entry.collection.lowercase().contains(q) ||
                    entry.tags.lowercase().contains(q) ||
                    entry.rarity.lowercase().contains(q) ||
                    entry.itemNumber.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    // Suggestions matching typed name in real-time for manual tab
    val matchingSuggestions = remember(name) {
        if (name.trim().length >= 2) {
            val query = name.trim().lowercase()
            RealMarketCatalog.allEntries.filter { entry ->
                entry.name.lowercase().contains(query) ||
                        entry.tags.lowercase().contains(query) ||
                        entry.collection.lowercase().contains(query)
            }.take(5)
        } else {
            emptyList()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Carta & Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
            // Screen Mode Tabs: [🔍 Buscar no Catálogo] vs [✏️ Cadastro Manual]
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Buscar no Catálogo", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("Cadastro Manual", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // =========================================================================
                // TAB 0: BUSCA RÁPIDA NO CATÁLOGO COM LISTA DE CARTAS E ADIÇÃO DIRETA EM 1 TOQUE
                // "quando eu vou adicionar a carta aparece uma lista de cartas meio que um busca aí eu clico e adiciona tudo da carta"
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Search Box & Instructions
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = catalogSearchQuery,
                                onValueChange = { catalogSearchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("catalog_search_input"),
                                placeholder = { Text("Buscar carta (ex: Charizard, Sheoldred, Sol Ring, Pikachu...)") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (catalogSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { catalogSearchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Quick Category Filter Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Todas as Cartas", "🔮 Magic: The Gathering", "⚡ Pokémon TCG", "🏎️ Diecast / Hot Wheels").forEach { cat ->
                                    FilterChip(
                                        selected = selectedCatalogCategory == cat,
                                        onClick = { selectedCatalogCategory = cat },
                                        label = { Text(cat, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Toque na carta ou em 'Adicionar Tudo' para salvar imediatamente:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "${displayedCatalogCards.size} cartas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (displayedCatalogCards.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Text("Nenhuma carta encontrada para '$catalogSearchQuery'", fontWeight = FontWeight.Bold)
                                    Text("Tente buscar por outro nome ou faça o cadastro manual abaixo:", fontSize = 12.sp, color = Color.Gray)
                                    Button(
                                        onClick = {
                                            name = catalogSearchQuery
                                            selectedTab = 1
                                        },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("Cadastrar Manualmente")
                                    }
                                }
                            }
                        }
                    } else {
                        items(displayedCatalogCards) { cardEntry ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { previewCardEntry = cardEntry },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Card Thumbnail Image
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (cardEntry.imageUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = cardEntry.imageUrl,
                                                contentDescription = cardEntry.name,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(28.dp))
                                        }
                                    }

                                    // Card Details
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                cardEntry.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (cardEntry.sourceMarket == "LigaMagic") LigaMagicBlue else LigaPokemonOrange
                                            ) {
                                                Text(
                                                    cardEntry.sourceMarket,
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            "${cardEntry.collection} • #${cardEntry.itemNumber} • ${cardEntry.rarity}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                "R$ ${String.format(java.util.Locale.US, "%.2f", cardEntry.realMarketPriceBrl)}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = NeonEmerald
                                            )
                                            Text(
                                                "• ${cardEntry.subCategory}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // 1-Click "Adicionar Tudo" Button
                                    Button(
                                        onClick = {
                                            val fullItem = buildItemFromEntry(cardEntry)
                                            viewModel.saveItem(fullItem) {
                                                coroutineScope.launch {
                                                    Toast.makeText(context, "✓ '${cardEntry.name}' adicionada com todos os dados!", Toast.LENGTH_SHORT).show()
                                                    navController.popBackStack()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Adicionar Tudo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Bottom Switch to Manual Form
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTab = 1 },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Não encontrou o seu item?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Toque aqui para preencher o formulário manualmente", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            } else {
                // =========================================================================
                // TAB 1: FORMULÁRIO MANUAL COMPLETO COM TODAS AS OPÇÕES E SUGESTÕES
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Back to Catalog Shortcut
                    OutlinedButton(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ManageSearch, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voltar para Busca no Catálogo Oficial", fontWeight = FontWeight.SemiBold)
                    }

                    // Name Input
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (it.length >= 3 && cardTranslatedEffect.isBlank()) {
                                    val direct = CardKnowledgeBase.findCardEffect(it, category, subCategory)
                                    if (direct != null) {
                                        cardTranslatedEffect = direct.translatedEffectPt
                                        cardOracleText = direct.originalOracle
                                        cardAttacks = direct.cardAttacks ?: ""
                                        cardHp = direct.cardHp ?: ""
                                        cardArtist = direct.cardArtist ?: ""
                                    }
                                }
                            },
                            label = { Text("Nome da Carta / Modelo") },
                            placeholder = { Text("Digite ex: Charizard, Pikachu, Sol Ring, Sheoldred...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("item_name_input"),
                            trailingIcon = {
                                if (name.isNotEmpty()) {
                                    IconButton(onClick = { name = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                    }
                                } else {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            },
                            singleLine = true
                        )

                        // LIVE SUGGESTIONS LIST (AutoComplete dropdown)
                        if (matchingSuggestions.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Sugestões do Catálogo (${matchingSuggestions.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("Toque para autopreencher tudo", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    matchingSuggestions.forEach { entry ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { fillFormFromEntry(entry) },
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (entry.imageUrl.isNotEmpty()) {
                                                        AsyncImage(
                                                            model = entry.imageUrl,
                                                            contentDescription = entry.name,
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    } else {
                                                        Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(20.dp))
                                                    }
                                                }

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = if (entry.sourceMarket == "LigaMagic") LigaMagicBlue else LigaPokemonOrange
                                                        ) {
                                                            Text(entry.sourceMarket, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                    Text("${entry.collection} • #${entry.itemNumber} • ${entry.rarity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                                }

                                                Text(
                                                    "R$ ${String.format(java.util.Locale.US, "%.2f", entry.realMarketPriceBrl)}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = NeonEmerald
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Button to fetch official high-res photo from Scryfall
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showScryfallPickerSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buscar Foto Oficial no Scryfall HD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Preview of selected official photo
                    if (officialImageUrl != null) {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = officialImageUrl,
                                        contentDescription = name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Foto Oficial Identificada", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Será vinculada à carta automaticamente em alta resolução.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { officialImageUrl = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover foto")
                                }
                            }
                        }
                    }

                    // Category Selection
                    Text("Categoria", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.take(3).forEach { cat ->
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }

                    // SubCategory
                    Text("Jogo / Linha", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        subCategories.take(3).forEach { sub ->
                            FilterChip(selected = subCategory == sub, onClick = { subCategory = sub }, label = { Text(sub) })
                        }
                    }

                    // --- SECTION: O QUE A CARTA FAZ & EFEITOS (TRADUÇÃO PT-BR) ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text("O que a Carta Faz & Efeitos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                    Text("Traduzido PT-BR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(
                                    onClick = {
                                        if (name.isNotBlank()) {
                                            applyCardEffects(name, category, subCategory)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buscar Efeito Oficial", fontSize = 12.sp)
                                }

                                if (cardOracleText.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            cardTranslatedEffect = CardKnowledgeBase.translateTextToPt(cardOracleText)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Traduzir Texto", fontSize = 12.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = cardTranslatedEffect,
                                onValueChange = { cardTranslatedEffect = it },
                                label = { Text("O que a carta faz / Efeitos (Português PT-BR)") },
                                placeholder = { Text("Ex: Habilidade — Reinado Infernal: Quando você jogar este Pokémon da sua mão...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6
                            )

                            OutlinedTextField(
                                value = cardAttacks,
                                onValueChange = { cardAttacks = it },
                                label = { Text("Ataques / Habilidades Principais") },
                                placeholder = { Text("Ex: Carga / Relâmpago (100 de dano)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = cardOracleText,
                                onValueChange = { cardOracleText = it },
                                label = { Text("Texto Original da Carta (Inglês)") },
                                placeholder = { Text("Ex: Charge: Search your deck...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardHp,
                                    onValueChange = { cardHp = it },
                                    label = { Text("HP / Nível / Poder") },
                                    placeholder = { Text("Ex: 330 HP") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = cardArtist,
                                    onValueChange = { cardArtist = it },
                                    label = { Text("Artista / Ilustrador") },
                                    placeholder = { Text("Ex: Mitsuhiro Arita") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // Collection & Numbers
                    OutlinedTextField(
                        value = collection,
                        onValueChange = { collection = it },
                        label = { Text("Coleção / Set (ex: Scarlet & Violet 151)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = itemNumber,
                            onValueChange = { itemNumber = it },
                            label = { Text("Número (ex: 199/165)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = rarity,
                            onValueChange = { rarity = it },
                            label = { Text("Raridade") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Energy Type / Mana Color / Card Type & Release Year
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = energyType,
                                onValueChange = { energyType = it },
                                label = { Text("Tipo / Cor / Elemento") },
                                placeholder = { Text("Ex: Preto, Azul, Fogo, Criatura...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = releaseYearStr,
                                onValueChange = { releaseYearStr = it },
                                label = { Text("Ano de Lançamento") },
                                placeholder = { Text("Ex: 2024, 2023") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Quick chips for Magic and Pokémon
                        Text(
                            if (subCategory.contains("Magic", ignoreCase = true)) "Sugestões para Magic: The Gathering (Cores & Tipos):"
                            else "Sugestões de Tipo / Elemento:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (subCategory.contains("Magic", ignoreCase = true)) {
                                listOf(
                                    "Preto" to "💀", "Azul" to "💧", "Vermelho" to "🔥", "Branco" to "☀️", "Verde" to "🌳",
                                    "Incolor" to "⚙️", "Multicolorido" to "🌟", "Criatura" to "🛡️", "Mágica Instantânea" to "⚡",
                                    "Feitiço" to "📜", "Encantamento" to "✨", "Artefato" to "⚙️", "Planeswalker" to "👑", "Terreno" to "🏔️"
                                ).forEach { (type, emoji) ->
                                    FilterChip(
                                        selected = energyType.equals(type, ignoreCase = true),
                                        onClick = { energyType = type },
                                        leadingIcon = { Text(emoji, fontSize = 12.sp) },
                                        label = { Text(type, fontSize = 11.sp) }
                                    )
                                }
                            } else {
                                listOf(
                                    "Fogo" to "🔥", "Água" to "💧", "Planta" to "🌿", "Elétrico" to "⚡", "Psíquico" to "🔮",
                                    "Luta" to "🥊", "Escuridão" to "🌑", "Metal" to "🛡️", "Dragão" to "🐉", "Incolor" to "⭐",
                                    "Preto" to "💀", "Azul" to "💧", "Vermelho" to "🔥", "Branco" to "☀️", "Verde" to "🌳"
                                ).forEach { (type, emoji) ->
                                    FilterChip(
                                        selected = energyType.equals(type, ignoreCase = true),
                                        onClick = { energyType = type },
                                        leadingIcon = { Text(emoji, fontSize = 12.sp) },
                                        label = { Text(type, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purchasePriceStr,
                            onValueChange = { purchasePriceStr = it },
                            label = { Text("Preço Pago (R$)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = estimatedValueStr,
                            onValueChange = { estimatedValueStr = it },
                            label = { Text("Cotação Atual (R$)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = condition,
                            onValueChange = { condition = it },
                            label = { Text("Estado / Condição") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Quantidade") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = storageLocation,
                        onValueChange = { storageLocation = it },
                        label = { Text("Local Físico (ex: Pasta de Raras - Pág 1, Gaveta)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Anotações adicionais") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val newItem = Item(
                                    name = name,
                                    category = category,
                                    subCategory = subCategory,
                                    collection = collection,
                                    itemNumber = itemNumber,
                                    rarity = rarity,
                                    variant = variant,
                                    condition = condition,
                                    quantity = quantityStr.toIntOrNull() ?: 1,
                                    storageLocation = storageLocation,
                                    purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0,
                                    estimatedValue = estimatedValueStr.toDoubleOrNull() ?: 0.0,
                                    officialImageUrl = officialImageUrl,
                                    imageUri = officialImageUrl ?: "",
                                    notes = notes,
                                    cardHp = cardHp.ifBlank { null },
                                    cardArtist = cardArtist.ifBlank { null },
                                    cardAttacks = cardAttacks.ifBlank { null },
                                    cardOracleText = cardOracleText.ifBlank { null },
                                    cardTranslatedEffect = cardTranslatedEffect.ifBlank { null },
                                    energyType = energyType.ifBlank { null },
                                    releaseYear = releaseYearStr.toIntOrNull()
                                )
                                viewModel.saveItem(newItem) {
                                    Toast.makeText(context, "✓ '${newItem.name}' salvo com sucesso!", Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_item_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salvar Item", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // =========================================================================
        // MODAL BOTTOM SHEET: PREVIEW DA CARTA CLICADA COM ADIÇÃO IMEDIATA
        // =========================================================================
        if (previewCardEntry != null) {
            val entry = previewCardEntry!!
            val effect = remember(entry) { CardKnowledgeBase.findOrGenerateCardEffect(entry.name, entry.category, entry.subCategory) }

            ModalBottomSheet(
                onDismissRequest = { previewCardEntry = null },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header with Card Image and Metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(154.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (entry.imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = entry.imageUrl,
                                    contentDescription = entry.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(40.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 22.sp)
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (entry.sourceMarket == "LigaMagic") LigaMagicBlue else LigaPokemonOrange
                            ) {
                                Text(
                                    entry.sourceMarket,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text("${entry.collection} • #${entry.itemNumber}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Raridade: ${entry.rarity}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                            Text(
                                "Cotação Média: R$ ${String.format(java.util.Locale.US, "%.2f", entry.realMarketPriceBrl)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald
                            )
                        }
                    }

                    // Card Rules / Translated Effect PT-BR
                    if (effect.translatedEffectPt.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text("Efeito da Carta (Traduzido PT-BR)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(effect.translatedEffectPt, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    }

                    // Action Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val fullItem = buildItemFromEntry(entry)
                                viewModel.saveItem(fullItem) {
                                    coroutineScope.launch {
                                        previewCardEntry = null
                                        Toast.makeText(context, "✓ '${entry.name}' adicionada com sucesso à sua coleção!", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Adicionar Imediatamente à Coleção", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                fillFormFromEntry(entry)
                                previewCardEntry = null
                                selectedTab = 1
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Personalizar / Editar Campos Antes de Salvar", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (showScryfallPickerSheet) {
        ScryfallArtPickerSheet(
            initialQuery = name,
            onDismiss = { showScryfallPickerSheet = false },
            onCardSelected = { card ->
                val newUrl = card.imageUrlLarge.ifBlank { card.imageUrlNormal }
                officialImageUrl = newUrl
                if (name.isBlank()) name = card.name
                if (collection.isBlank()) collection = "[${card.setCode.uppercase()}] ${card.setName}"
                if (itemNumber.isBlank()) itemNumber = card.collectorNumber
                if (rarity.isBlank()) rarity = card.rarity
                if (cardArtist.isBlank() && !card.artist.isNullOrBlank()) cardArtist = card.artist
                if (cardOracleText.isBlank() && !card.oracleText.isNullOrBlank()) cardOracleText = card.oracleText
                if (estimatedValueStr.isBlank() && card.priceUsd != null && card.priceUsd > 0.0) {
                    estimatedValueStr = String.format(java.util.Locale.US, "%.2f", card.priceUsd * 5.75)
                }
                showScryfallPickerSheet = false
                Toast.makeText(context, "✓ Foto oficial de '${card.name}' selecionada do Scryfall!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
