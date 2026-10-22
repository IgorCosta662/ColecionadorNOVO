package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.Item
import com.example.data.SetRegistry
import com.example.ui.CollectorViewModel
import com.example.ui.navigation.Routes
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    viewModel: CollectorViewModel,
    navController: NavController,
    onOpenDrawer: (() -> Unit)? = null
) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val appMode by viewModel.appMode.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedSet by remember { mutableStateOf<String?>(null) }
    var selectedEnergyType by remember { mutableStateOf<String?>(null) }
    var selectedMagicColor by remember { mutableStateOf<String?>(null) }
    var selectedMagicType by remember { mutableStateOf<String?>(null) }
    var selectedReleaseYear by remember { mutableStateOf<Int?>(null) }

    var showFilterSheet by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "Trading Cards", "Carrinhos / Diecast", "Favoritos")

    val magicColors = listOf(
        "Preto" to "💀",
        "Azul" to "💧",
        "Vermelho" to "🔥",
        "Branco" to "☀️",
        "Verde" to "🌳",
        "Incolor" to "⚙️",
        "Multicolorido" to "🌟"
    )

    val magicCardTypes = listOf(
        "Criatura" to "🛡️",
        "Mágica Instantânea" to "⚡",
        "Feitiço" to "📜",
        "Encantamento" to "✨",
        "Artefato" to "⚙️",
        "Planeswalker" to "👑",
        "Terreno" to "🏔️",
        "Batalha" to "⚔️"
    )

    val pokemonEnergyTypes = listOf(
        "Fogo" to "🔥",
        "Água" to "💧",
        "Planta" to "🌿",
        "Elétrico / Raios" to "⚡",
        "Psíquico" to "🔮",
        "Luta" to "🥊",
        "Escuridão" to "🌑",
        "Metal" to "🛡️",
        "Dragão" to "🐉",
        "Incolor" to "⭐"
    )

    // Dynamically available sets from collection items + official registry
    val availableSets = remember(items) {
        val itemSets = items.map { it.collection.trim() }.filter { it.isNotBlank() }
        val registrySets = SetRegistry.sets.map { it.name }
        val popularSets = listOf(
            "Scarlet & Violet 151",
            "Foundations",
            "Evolving Skies",
            "Commander Masters",
            "Secret Lair x Marvel",
            "Dominaria United",
            "Duskmourn: House of Horror",
            "Hot Wheels Mainline 2024",
            "Fast & Furious Premium"
        )
        (itemSets + registrySets + popularSets).distinct().sorted()
    }

    // Dynamically available release years
    val availableYears = remember(items) {
        val itemYears = items.mapNotNull { it.resolvedReleaseYear }
        val registryYears = SetRegistry.sets.map { it.releaseYear }
        val defaultYears = listOf(2025, 2024, 2023, 2022, 2021, 2020, 2019, 2016, 2000, 1999)
        (itemYears + registryYears + defaultYears).distinct().sortedDescending()
    }

    // Active filters count
    val activeFiltersCount = (if (selectedCategory != "Todos") 1 else 0) +
            (if (selectedSet != null) 1 else 0) +
            (if (selectedEnergyType != null) 1 else 0) +
            (if (selectedMagicColor != null) 1 else 0) +
            (if (selectedMagicType != null) 1 else 0) +
            (if (selectedReleaseYear != null) 1 else 0)

    val filteredItems = items.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.collection.contains(searchQuery, ignoreCase = true) ||
                item.itemNumber.contains(searchQuery, ignoreCase = true) ||
                item.tags.contains(searchQuery, ignoreCase = true)

        val matchesCategory = when (selectedCategory) {
            "Todos" -> true
            "Favoritos" -> item.isFavorite
            else -> item.category.equals(selectedCategory, ignoreCase = true)
        }

        val matchesSet = selectedSet == null || item.collection.contains(selectedSet!!, ignoreCase = true)

        val matchesMagicColor = selectedMagicColor == null || run {
            val color = item.resolvedMagicColor.ifEmpty { item.resolvedEnergyType }
            color.contains(selectedMagicColor!!, ignoreCase = true)
        }

        val matchesMagicType = selectedMagicType == null || run {
            val cType = item.resolvedCardType
            cType.contains(selectedMagicType!!, ignoreCase = true) ||
                    item.tags.contains(selectedMagicType!!, ignoreCase = true) ||
                    item.notes.contains(selectedMagicType!!, ignoreCase = true)
        }

        val matchesEnergy = selectedEnergyType == null || run {
            val itemEnergy = item.resolvedEnergyType
            if (selectedEnergyType == "Elétrico / Raios") {
                itemEnergy.contains("Elétrico", ignoreCase = true) || itemEnergy.contains("Raios", ignoreCase = true)
            } else {
                itemEnergy.contains(selectedEnergyType!!, ignoreCase = true)
            }
        }

        val matchesYear = selectedReleaseYear == null || item.resolvedReleaseYear == selectedReleaseYear

        matchesSearch && matchesCategory && matchesSet && matchesMagicColor && matchesMagicType && matchesEnergy && matchesYear
    }

    fun clearAllFilters() {
        selectedCategory = "Todos"
        selectedSet = null
        selectedEnergyType = null
        selectedMagicColor = null
        selectedMagicType = null
        selectedReleaseYear = null
        searchQuery = ""
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Limpar Toda a Coleção?") },
            text = { Text("Tem certeza de que deseja remover todos os itens cadastrados? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllItems()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sim, Remover Tudo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- ADVANCED FILTERS MODAL BOTTOM SHEET ---
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Filtros Avançados",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (activeFiltersCount > 0) {
                        TextButton(onClick = { clearAllFilters() }) {
                            Text("Limpar Tudo", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                HorizontalDivider()

                // 1. FILTER BY CARD SET / COLEÇÃO
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Coleção / Set da Carta", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (selectedSet != null) {
                            Text(
                                "Selecionado",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedSet == null,
                            onClick = { selectedSet = null },
                            label = { Text("Todos os Sets") }
                        )
                        availableSets.forEach { setName ->
                            FilterChip(
                                selected = selectedSet == setName,
                                onClick = { selectedSet = if (selectedSet == setName) null else setName },
                                label = { Text(setName, maxLines = 1) }
                            )
                        }
                    }
                }

                // 2. FILTER BY MAGIC MANA COLORS
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Magic: The Gathering — Cores de Mana", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        if (selectedMagicColor != null) {
                            Text(
                                selectedMagicColor ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedMagicColor == null,
                            onClick = { selectedMagicColor = null },
                            label = { Text("Todas as Cores") }
                        )
                        magicColors.forEach { (type, emoji) ->
                            FilterChip(
                                selected = selectedMagicColor == type,
                                onClick = { selectedMagicColor = if (selectedMagicColor == type) null else type },
                                leadingIcon = { Text(emoji, fontSize = 14.sp) },
                                label = { Text(type) }
                            )
                        }
                    }
                }

                // 3. FILTER BY MAGIC CARD TYPES
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Magic: The Gathering — Tipos de Carta", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        if (selectedMagicType != null) {
                            Text(
                                selectedMagicType ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedMagicType == null,
                            onClick = { selectedMagicType = null },
                            label = { Text("Todos os Tipos") }
                        )
                        magicCardTypes.forEach { (type, emoji) ->
                            FilterChip(
                                selected = selectedMagicType == type,
                                onClick = { selectedMagicType = if (selectedMagicType == type) null else type },
                                leadingIcon = { Text(emoji, fontSize = 14.sp) },
                                label = { Text(type) }
                            )
                        }
                    }
                }

                // 4. FILTER BY POKÉMON ENERGY TYPES
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pokémon TCG — Tipos de Energia", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (selectedEnergyType != null) {
                            Text(
                                selectedEnergyType ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedEnergyType == null,
                            onClick = { selectedEnergyType = null },
                            label = { Text("Todas as Energias") }
                        )
                        pokemonEnergyTypes.forEach { (type, emoji) ->
                            FilterChip(
                                selected = selectedEnergyType == type,
                                onClick = { selectedEnergyType = if (selectedEnergyType == type) null else type },
                                leadingIcon = { Text(emoji, fontSize = 14.sp) },
                                label = { Text(type) }
                            )
                        }
                    }
                }

                // 3. FILTER BY RELEASE YEAR
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ano de Lançamento", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (selectedReleaseYear != null) {
                            Text(
                                "${selectedReleaseYear}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedReleaseYear == null,
                            onClick = { selectedReleaseYear = null },
                            label = { Text("Todos os Anos") }
                        )
                        availableYears.forEach { year ->
                            FilterChip(
                                selected = selectedReleaseYear == year,
                                onClick = { selectedReleaseYear = if (selectedReleaseYear == year) null else year },
                                label = { Text(year.toString()) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Apply button
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar Filtros (${filteredItems.size} resultados)", fontWeight = FontWeight.Bold)
                }
            }
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
                title = { Text("Minha Coleção", fontWeight = FontWeight.Bold) },
                actions = {
                    if (items.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpar todos os itens")
                        }
                    }
                    IconButton(
                        onClick = { navController.navigate(Routes.COMPARE) },
                        modifier = Modifier.testTag("collection_compare_button")
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = "Comparador")
                    }
                    IconButton(onClick = { navController.navigate(Routes.ADD_ITEM) }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Item")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.ADD_ITEM) },
                modifier = Modifier.testTag("add_item_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Item")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar & Advanced Filters Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Buscar nome, coleção, #...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Advanced Filter Button with Active Count Badge
                BadgedBox(
                    badge = {
                        if (activeFiltersCount > 0) {
                            Badge { Text(activeFiltersCount.toString()) }
                        }
                    }
                ) {
                    FilledTonalIconButton(
                        onClick = { showFilterSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Filtros Avançados")
                    }
                }
            }

            // Quick Category Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            // Active Advanced Filters Pill Bar (when filters are active)
            if (activeFiltersCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { clearAllFilters() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("Limpar (${activeFiltersCount})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (selectedSet != null) {
                        InputChip(
                            selected = true,
                            onClick = { selectedSet = null },
                            label = { Text("Set: $selectedSet", fontSize = 11.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    if (selectedMagicColor != null) {
                        val emoji = magicColors.find { it.first == selectedMagicColor }?.second ?: "🔮"
                        InputChip(
                            selected = true,
                            onClick = { selectedMagicColor = null },
                            label = { Text("Magic Cor: $emoji $selectedMagicColor", fontSize = 11.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    if (selectedMagicType != null) {
                        val emoji = magicCardTypes.find { it.first == selectedMagicType }?.second ?: "🛡️"
                        InputChip(
                            selected = true,
                            onClick = { selectedMagicType = null },
                            label = { Text("Magic Tipo: $emoji $selectedMagicType", fontSize = 11.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    if (selectedEnergyType != null) {
                        val emoji = pokemonEnergyTypes.find { it.first == selectedEnergyType }?.second ?: "⚡"
                        InputChip(
                            selected = true,
                            onClick = { selectedEnergyType = null },
                            label = { Text("Pokémon: $emoji $selectedEnergyType", fontSize = 11.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    if (selectedReleaseYear != null) {
                        InputChip(
                            selected = true,
                            onClick = { selectedReleaseYear = null },
                            label = { Text("Ano: $selectedReleaseYear", fontSize = 11.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Exibindo ${filteredItems.size} de ${items.size} itens",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                if (items.isNotEmpty()) {
                    val totalVal = filteredItems.sumOf { it.totalEstimatedValue }
                    Text(
                        "Subtotal: ${currency.formatValue(totalVal)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Collection Items List or Empty State
            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Nenhum item corresponde aos filtros",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            "Tente alterar a pesquisa, o set selecionado, o tipo de energia ou o ano.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (activeFiltersCount > 0 || searchQuery.isNotEmpty()) {
                            OutlinedButton(onClick = { clearAllFilters() }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Limpar Todos os Filtros")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        ItemCard(
                            item = item,
                            currencySymbol = currency.symbol,
                            formattedValue = currency.formatValue(item.estimatedValue),
                            onClick = { navController.navigate(Routes.itemDetail(item.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    currencySymbol: String,
    formattedValue: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
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
            val img = item.officialImageUrl ?: item.imageUri
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (img.isNotEmpty()) {
                    AsyncImage(
                        model = img,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        if (item.isCard) Icons.Default.Style else Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Collection & Set with Year
                val resolvedYear = item.resolvedReleaseYear
                Text(
                    "${item.collection}${if (item.itemNumber.isNotBlank()) " #${item.itemNumber}" else ""}${if (resolvedYear != null) " • $resolvedYear" else ""}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Tags Row: Energy Type, Condition, Variant
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val magicColor = item.resolvedMagicColor
                    val cardType = item.resolvedCardType
                    if (item.isMagicCard && magicColor.isNotBlank() && cardType.isNotBlank() && !cardType.equals(magicColor, ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                "${item.typeEmoji} $magicColor",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                cardType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        val energy = item.resolvedEnergyType.ifEmpty { item.resolvedCardType }
                        if (energy.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    "${item.typeEmoji} $energy",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            item.condition,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    if (item.variant.isNotEmpty() && item.variant != "Normal") {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                item.variant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    formattedValue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Qtd: ${item.quantity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
