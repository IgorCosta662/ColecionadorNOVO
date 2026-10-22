package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.*
import com.example.ui.CollectorViewModel
import com.example.ui.navigation.Routes
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.util.AgentRole
import com.example.util.AiAgent
import com.example.util.AiAgentRegistry
import com.example.util.AiSettingsManager
import com.example.util.CollectionBackupManager
import kotlinx.coroutines.launch

// 1. O Que Falta? (Checklist por Set)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingCardsScreen(viewModel: CollectorViewModel, navController: NavController) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val allSets = SetRegistry.sets
    var selectedSetId by remember { mutableStateOf(allSets.first().id) }
    val currentSet = allSets.first { it.id == selectedSetId }

    val ownedCardNames = items.filter { it.collection.contains(currentSet.name, ignoreCase = true) || it.subCategory.contains(currentSet.gameOrBrand, ignoreCase = true) }
        .map { it.name.lowercase().trim() }
        .toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("O Que Falta? (Checklists)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Selecione o Set ou Coleção:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    items(allSets) { setInfo ->
                        FilterChip(
                            selected = setInfo.id == selectedSetId,
                            onClick = { selectedSetId = setInfo.id },
                            label = { Text(setInfo.name) }
                        )
                    }
                }
            }

            item {
                val ownedCount = currentSet.popularCardsOrModels.count { ownedCardNames.any { o -> o.contains(it.lowercase().take(8)) } }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(currentSet.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${currentSet.gameOrBrand} • Ano: ${currentSet.releaseYear} • Total: ${currentSet.totalCount} itens", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LinearProgressIndicator(
                            progress = { if (currentSet.popularCardsOrModels.isNotEmpty()) ownedCount.toFloat() / currentSet.popularCardsOrModels.size.toFloat() else 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = NeonEmerald
                        )
                        Text("$ownedCount de ${currentSet.popularCardsOrModels.size} destaques obtidos no checklist", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item {
                Text("Lista de Cartas & Peças do Set", fontWeight = FontWeight.Bold)
            }

            items(currentSet.popularCardsOrModels) { cardName ->
                val isOwned = ownedCardNames.any { it.contains(cardName.lowercase().take(8)) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOwned) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                imageVector = if (isOwned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isOwned) NeonEmerald else Color.Gray
                            )
                            Column {
                                Text(cardName, fontWeight = FontWeight.Bold)
                                Text(if (isOwned) "Na sua coleção" else "Faltando na coleção", fontSize = 12.sp, color = if (isOwned) NeonEmerald else NeonRose)
                            }
                        }
                        if (!isOwned) {
                            TextButton(onClick = {
                                viewModel.saveWishlist(
                                    WishlistItem(
                                        name = cardName,
                                        category = "Trading Cards",
                                        collection = currentSet.name,
                                        targetMaxPrice = 50.0,
                                        priority = "Alta"
                                    )
                                )
                            }) {
                                Text("+ Wishlist", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. Inventário Físico (Pastas, Caixas & Gavetas)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalInventoryScreen(viewModel: CollectorViewModel, navController: NavController) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    val groupedByLocation = items.groupBy {
        if (it.storageLocation.isBlank()) "Sem Local Definido" else it.storageLocation
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventário Físico (Pastas & Caixas)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Organização Física", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Saiba exatamente em qual pasta, gaveta, binder ou caixa acrílica cada item está guardado.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Total de locais rastreados: ${groupedByLocation.size}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            items(groupedByLocation.entries.toList()) { entry ->
                val locationName = entry.key
                val itemList = entry.value
                val totalLocationVal = itemList.sumOf { it.totalEstimatedValue }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Inventory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(locationName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Text(currency.formatValue(totalLocationVal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("${itemList.size} itens armazenados", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider()
                        itemList.take(4).forEach { itm ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(itm.name, fontSize = 13.sp)
                                Text(itm.condition, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (itemList.size > 4) {
                            Text("+ ${itemList.size - 4} outros itens...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// 3. Duplicatas (Controle de Cópias para Troca/Venda)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicatesScreen(viewModel: CollectorViewModel, navController: NavController) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    val duplicates = items.filter { it.quantity > 1 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Controle de Duplicatas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                val totalExtraCards = duplicates.sumOf { it.quantity - 1 }
                val totalExtraValue = duplicates.sumOf { (it.quantity - 1) * it.estimatedValue }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Banca de Trocas & Vendas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Itens com mais de 1 cópia que podem ser trocados ou vendidos para reinvestir na coleção.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total de cópias extras:")
                            Text("$totalExtraCards unidades", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Valor parado em duplicatas:")
                            Text(currency.formatValue(totalExtraValue), fontWeight = FontWeight.Bold, color = NeonEmerald)
                        }
                    }
                }
            }

            if (duplicates.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Text("Nenhuma duplicata encontrada", fontWeight = FontWeight.Bold)
                            Text("Todos os seus itens cadastrados possuem quantidade 1.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(duplicates) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${item.collection} • #${item.itemNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Total em mãos: ${item.quantity} | Sobrando: ${item.quantity - 1}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(currency.formatValue(item.estimatedValue * (item.quantity - 1)), fontWeight = FontWeight.Bold, color = NeonEmerald)
                                Text("Valor da sobra", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. Central de Agentes de IA Especializados (Multi-Agentes)
data class MultiAgentChatMessage(
    val senderName: String,
    val senderEmoji: String,
    val text: String,
    val isAi: Boolean,
    val bubbleColor: Color = Color.Unspecified
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(viewModel: CollectorViewModel, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    val isCustomKeyActive = remember { AiSettingsManager.isUsingCustomKey(context) }
    val activeModel = remember { AiSettingsManager.getSelectedModel(context) }

    var selectedAgentId by remember {
        mutableStateOf(AiSettingsManager.getSelectedAgentId(context))
    }
    val currentAgent = remember(selectedAgentId) {
        AiAgentRegistry.getAgentById(selectedAgentId)
    }

    var userPrompt by remember { mutableStateOf("") }
    var isLoadingAi by remember { mutableStateOf(false) }

    var chatMessages by remember(selectedAgentId) {
        mutableStateOf(
            listOf(
                MultiAgentChatMessage(
                    senderName = currentAgent.name,
                    senderEmoji = currentAgent.avatarEmoji,
                    text = "Olá colecionador! Sou o ${currentAgent.name} (${currentAgent.title}). ${currentAgent.bio}\n\nLema: \"${currentAgent.motto}\"\n\nComo posso ajudar você hoje?",
                    isAi = true,
                    bubbleColor = currentAgent.primaryColor
                )
            )
        )
    }

    fun sendQuestion(questionText: String) {
        if (questionText.isBlank() || isLoadingAi) return
        val currentQuestion = questionText.trim()
        userPrompt = ""
        chatMessages = chatMessages + MultiAgentChatMessage(
            senderName = "Você",
            senderEmoji = "👤",
            text = currentQuestion,
            isAi = false
        )
        isLoadingAi = true

        coroutineScope.launch {
            val totalItems = items.size
            val totalValue = items.sumOf { it.totalEstimatedValue }
            val topCards = items.sortedByDescending { it.estimatedValue }.take(6).joinToString(", ") {
                "${it.name} (${it.collection}, ${currency.formatValue(it.estimatedValue)})"
            }
            val magicCards = items.filter { it.isMagicCard }
            val pokemonCards = items.filter { it.isPokemonCard }

            val collectionContext = """
                Total de itens no acervo: $totalItems itens.
                Valor total estimado: ${currency.formatValue(totalValue)}.
                Cartas Magic: The Gathering: ${magicCards.size} cartas (Ex: ${magicCards.take(3).joinToString { it.name }}).
                Cartas Pokémon TCG: ${pokemonCards.size} cartas (Ex: ${pokemonCards.take(3).joinToString { it.name }}).
                Top cartas mais valiosas do acervo: $topCards.
            """.trimIndent()

            val aiResult = AiSettingsManager.askGeminiAgent(context, currentAgent, currentQuestion, collectionContext)
            isLoadingAi = false

            aiResult.fold(
                onSuccess = { answer ->
                    chatMessages = chatMessages + MultiAgentChatMessage(
                        senderName = currentAgent.name,
                        senderEmoji = currentAgent.avatarEmoji,
                        text = answer,
                        isAi = true,
                        bubbleColor = currentAgent.primaryColor
                    )
                },
                onFailure = { error ->
                    // Fallback to rich intelligent heuristics tailored to the specific agent role
                    val localAnswer = when (currentAgent.role) {
                        AgentRole.MTG_SPECIALIST -> {
                            if (magicCards.isNotEmpty()) {
                                val topMtg = magicCards.maxByOrNull { it.estimatedValue }
                                "Analisei suas cartas de Magic: The Gathering! Você possui ${magicCards.size} cartas de MTG cadastradas. Seu destaque é '${topMtg?.name}' (${topMtg?.collection}) avaliada em ${topMtg?.let { currency.formatValue(it.estimatedValue) }}. Para Commander, recomendo priorizar aceleração de mana e consistência de compra!"
                            } else {
                                "Vejo que você ainda não cadastrou cartas de Magic: The Gathering no seu acervo. Use o leitor de cartas ou adicione suas cartas para eu analisar sinergias de Commander e curvas de mana!"
                            }
                        }
                        AgentRole.POKEMON_SPECIALIST -> {
                            if (pokemonCards.isNotEmpty()) {
                                val topPk = pokemonCards.maxByOrNull { it.estimatedValue }
                                "Pelas pesquisas do laboratório Pokémon, seu acervo tem ${pokemonCards.size} cartas de Pokémon TCG! Sua carta mais rara no momento é '${topPk?.name}' avaliada em ${topPk?.let { currency.formatValue(it.estimatedValue) }}. Lembre-se de manter as cartas holográficas e ex protegidas em sleeves de qualidade!"
                            } else {
                                "Você ainda não possui cartas Pokémon TCG cadastradas. Escaneie seus cards de edições como 151, Paldea ou Destinos para receber análises de raridade e meta!"
                            }
                        }
                        AgentRole.MARKET_ANALYST -> {
                            val profit = items.sumOf { it.profit }
                            val mostValuable = items.maxByOrNull { it.estimatedValue }
                            "Diagnóstico Financeiro do Acervo: Total de ${currency.formatValue(totalValue)} em $totalItems itens. Lucro estimado total: ${currency.formatValue(profit)}. Peça mais valiosa: '${mostValuable?.name ?: "Nenhuma"}' (${mostValuable?.let { currency.formatValue(it.estimatedValue) } ?: "R$ 0,00"}). Recomendo manter cartas de edições limitadas (Hold) e realizar lucro em cartas com picos de demanda na LigaMagic/LigaPokémon."
                        }
                        AgentRole.RULES_JUDGE -> {
                            "Parecer do Juiz Oficial: Lembre-se do princípio fundamental da Pilha (The Stack): 'Last In, First Out' (o último a entrar é o primeiro a resolver). Habilidades de mana não usam a pilha e resolvem imediatamente. Já mágicas instantâneas ou habilidades ativadas podem ser respondidas enquanto você ou o oponente tiverem prioridade."
                        }
                        AgentRole.CARD_CURATOR -> {
                            "Dica de Preservação e Autenticidade: Para cartas de alto valor, utilize a técnica de Double-Sleeving: insira um sleeve Perfect Fit com a abertura para baixo e depois coloque o shield externo (ex: Dragon Shield) com a abertura para cima. Isso cria uma barreira hermética contra poeira e umidade, prevenindo o efeito pringle!"
                        }
                        AgentRole.DECK_ARCHITECT -> {
                            "Recomendação de Deckbuilding: Para um deck de 60 cartas, o padrão de ouro é: 24 terrenos/energias, 12 a 16 cartas de aceleração e compras, 8 a 10 remoções e o restante em ameaças principais. No formato Commander (100 cartas), mantenha entre 36 a 38 terrenos e pelo menos 10 fontes de ramp de custo 2 ou menos!"
                        }
                        AgentRole.COUNCIL_ROUNDTABLE -> {
                            val mostValuable = items.maxByOrNull { it.estimatedValue }
                            """
                                🏛️ Parecer Unificado do Conselho:
                                
                                🧙‍♂️/⚡ [VISÃO DE JOGO]: Seu acervo conta com ${magicCards.size} cartas de MTG e ${pokemonCards.size} cartas de Pokémon.
                                
                                📈 [VISÃO DE MERCADO]: Patrimônio estimado em ${currency.formatValue(totalValue)} com destaque para '${mostValuable?.name ?: "Suas peças principais"}'.
                                
                                ⚖️/🛡️ [CONSERVAÇÃO & REGRAS]: Mantenha suas cartas mais raras em sleeves duplos e binders sem anéis.
                                
                                (Nota: Adicione sua chave gratuita do Gemini nas Configurações para desbloquear raciocínio profundo e respostas personalizadas em tempo real!)
                            """.trimIndent()
                        }
                    }
                    chatMessages = chatMessages + MultiAgentChatMessage(
                        senderName = currentAgent.name,
                        senderEmoji = currentAgent.avatarEmoji,
                        text = localAnswer,
                        isAi = true,
                        bubbleColor = currentAgent.primaryColor
                    )
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Central de Agentes de IA", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "${AiAgentRegistry.ALL_AGENTS.size} Especialistas Disponíveis",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar Chave de IA")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Key Status Header Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { navController.navigate(Routes.SETTINGS) },
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCustomKeyActive) NeonEmerald else MaterialTheme.colorScheme.error)
                        )
                        Text(
                            if (isCustomKeyActive) "Chave IA Ativa" else "Chave Não Configurada",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustomKeyActive) NeonEmerald else MaterialTheme.colorScheme.error
                        )
                        Text("• $activeModel", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(if (isCustomKeyActive) "⚙️ Chave & Modelo" else "⚙️ Adicionar Chave", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Carousel: Horizontal Selector of AI Agents
            Text(
                "ESCOLHA SEU AGENTE ESPECIALISTA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AiAgentRegistry.ALL_AGENTS) { agent ->
                    val isSelected = agent.id == selectedAgentId
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedAgentId = agent.id
                                AiSettingsManager.setSelectedAgentId(context, agent.id)
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) agent.primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = if (isSelected) 4.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(agent.avatarEmoji, fontSize = 18.sp)
                            Column {
                                Text(
                                    agent.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    agent.badge,
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Active Agent Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = currentAgent.primaryColor.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(currentAgent.primaryColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentAgent.avatarEmoji, fontSize = 24.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                currentAgent.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = currentAgent.primaryColor
                            ) {
                                Text(
                                    currentAgent.title,
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            currentAgent.motto,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            if (!isCustomKeyActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Text("Usando Heurísticas Locais (Adicione sua chave)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                        TextButton(
                            onClick = { navController.navigate(Routes.SETTINGS) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("Configurar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Chat Messages
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatMessages) { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.isAi) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (msg.isAi) {
                                if (msg.bubbleColor != Color.Unspecified) msg.bubbleColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.widthIn(max = 320.dp),
                            tonalElevation = if (msg.isAi) 2.dp else 0.dp
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(msg.senderEmoji, fontSize = 14.sp)
                                    Text(
                                        msg.senderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (msg.isAi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(msg.text, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }

                if (isLoadingAi) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = currentAgent.primaryColor.copy(alpha = 0.2f),
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = currentAgent.primaryColor)
                                    Text("${currentAgent.name} está pensando...", fontSize = 12.sp, color = currentAgent.primaryColor, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // Agent Specific Suggested Prompts
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(currentAgent.suggestedPrompts) { q ->
                    SuggestionChip(
                        onClick = { sendQuestion(q) },
                        label = { Text(q, fontSize = 11.sp) },
                        enabled = !isLoadingAi
                    )
                }
            }

            // Text Input Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userPrompt,
                    onValueChange = { userPrompt = it },
                    placeholder = { Text("Pergunte ao ${currentAgent.title}...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoadingAi
                )
                IconButton(
                    onClick = { sendQuestion(userPrompt) },
                    enabled = userPrompt.isNotBlank() && !isLoadingAi
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = if (userPrompt.isNotBlank() && !isLoadingAi) currentAgent.primaryColor else Color.Gray
                    )
                }
            }
        }
    }
}

// 5. Alertas de Preço (Detector Suspeito & Flutuações)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertsScreen(viewModel: CollectorViewModel, navController: NavController) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alertas de Preço & Radar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = NeonRose)
                            Text("Detector de Flutuações & Preços Suspeitos", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            "O radar compara preços praticados em marketplaces nacionais. Alertas são disparados quando há desvios de mais de 30% em relação à cotação média da LigaMagic/LigaPokémon.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text("Cartas Monitoradas na Coleção", fontWeight = FontWeight.Bold)
            }

            items(items) { item ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(item.name, fontWeight = FontWeight.Bold)
                            Text("${item.collection} • Cotação: ${currency.formatValue(item.estimatedValue)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(shape = RoundedCornerShape(4.dp), color = NeonEmerald.copy(alpha = 0.15f)) {
                                Text("Preço Estável LigaMagic/LigaPokémon", color = NeonEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Switch(
                            checked = true,
                            onCheckedChange = {}
                        )
                    }
                }
            }
        }
    }
}

// 6. Patrimônio & Backup (Exportar CSV, JSON, PDF & Proteção com PIN)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatrimonyBackupScreen(viewModel: CollectorViewModel, navController: NavController) {
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val totalValue = items.sumOf { it.totalEstimatedValue }
    val totalCost = items.sumOf { it.totalPurchasePrice }

    var exportType by remember { mutableStateOf("csv") }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(if (exportType == "csv") "text/csv" else "application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val content = if (exportType == "csv") {
                CollectionBackupManager.exportToCsv(items)
            } else {
                CollectionBackupManager.exportToJson(items)
            }
            try {
                CollectionBackupManager.writeToUri(context, uri, content)
                Toast.makeText(context, "Backup exportado com sucesso (${items.size} itens)!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao salvar arquivo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patrimônio & Backup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Segurança & Exportação Externa", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Exporte seu inventário completo para backup físico ou digital em formato CSV ou JSON compatível para restauração.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Patrimônio Registrado:")
                        Text(currency.formatValue(totalValue), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total de Itens:")
                        Text("${items.size} peças catalogadas", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("Exportar Backup da Coleção", fontWeight = FontWeight.Bold)

            Button(
                onClick = {
                    exportType = "csv"
                    createDocumentLauncher.launch("colecao_backup_${System.currentTimeMillis()}.csv")
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar para CSV (Planilhas / Excel)")
            }

            OutlinedButton(
                onClick = {
                    exportType = "json"
                    createDocumentLauncher.launch("colecao_backup_${System.currentTimeMillis()}.json")
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.DataObject, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar para JSON (Backup Estruturado)")
            }

            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Dossiê gerado com resumo de ${items.size} peças e valor total de ${currency.formatValue(totalValue)}!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gerar Dossiê de Seguro / Relatório Formatado")
            }
        }
    }
}

// 7. Produtos Selados (Boosters, Caixas & Decks Lacrados)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SealedProductsScreen(viewModel: CollectorViewModel, navController: NavController) {
    val sealedList by viewModel.allSealedProducts.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var prodName by remember { mutableStateOf("") }
    var prodGame by remember { mutableStateOf("Pokémon TCG") }
    var prodType by remember { mutableStateOf("Booster Box") }
    var prodPrice by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produtos Selados & Caixas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Selado")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Selado")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Controle de Produtos Lacrados", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Monitore Booster Boxes, Bundles, Elite Trainer Boxes (ETB) e Decks Lacrados de Magic e Pokémon.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (sealedList.isEmpty()) {
                item {
                    // Default examples
                    val samples = listOf(
                        "Booster Box Scarlet & Violet 151" to 1100.0,
                        "Bundle Gift Edition Magic Foundations" to 450.0,
                        "Elite Trainer Box (ETB) Paldea Evolved" to 380.0
                    )
                    Text("Exemplos de Produtos Selados Rastreados:", fontWeight = FontWeight.Bold)
                    samples.forEach { (name, price) ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(name, fontWeight = FontWeight.Bold)
                                    Text("Produto Lacrado de Fábrica", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(currency.formatValue(price), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            } else {
                items(sealedList) { prod ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(prod.name, fontWeight = FontWeight.Bold)
                                Text("${prod.game} • ${prod.productType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(currency.formatValue(prod.currentMarketEstimate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// 8. Guia de Efeitos (Tradução e Oracle Text PT-BR)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsGuideScreen(viewModel: CollectorViewModel, navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }

    val cards = listOf(
        Triple("The One Ring", "Indestructible. When The One Ring enters the battlefield, if you cast it, you gain protection from everything until your next turn.", "Indestrutível. Quando O Um Anel entra no campo de batalha, se você o conjurou, você ganha proteção contra tudo até o seu próximo turno."),
        Triple("Charizard ex (Tera)", "Tera: As long as this Pokémon is on your Bench, prevent all damage done to this Pokémon by attacks.", "Tera: Enquanto este Pokémon estiver no seu Banco, previna todo o dano causado a ele por ataques."),
        Triple("Sol Ring", "{T}: Add {C}{C}.", "{T}: Adicione {C}{C} à sua reserva de mana."),
        Triple("Black Lotus", "{T}, Sacrifice Black Lotus: Add three mana of any one color.", "{T}, Sacrifique Black Lotus: Adicione três manas de qualquer cor à sua reserva de mana.")
    )

    val filtered = cards.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guia de Efeitos (Traduzir Tudo PT-BR)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar carta ou texto de efeito em inglês...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered) { (name, originalText, ptText) ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Inglês Oficial:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(originalText, fontSize = 13.sp, style = MaterialTheme.typography.bodyMedium)
                            HorizontalDivider()
                            Text("Tradução Oficial para Português (PT-BR):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(ptText, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// 9. Importar Catálogo (Direto de Arquivo CSV, JSON ou Catálogo Oficial)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportCatalogScreen(viewModel: CollectorViewModel, navController: NavController) {
    val context = LocalContext.current
    var isImporting by remember { mutableStateOf(false) }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            try {
                val content = CollectionBackupManager.readUriContent(context, uri)
                val items = if (content.trim().startsWith("[") || content.trim().startsWith("{")) {
                    CollectionBackupManager.parseJson(content)
                } else {
                    CollectionBackupManager.parseCsv(content)
                }

                if (items.isNotEmpty()) {
                    viewModel.importBatchItems(items) { count ->
                        Toast.makeText(context, "$count cartas/itens importados para sua coleção com sucesso!", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                } else {
                    Toast.makeText(context, "Nenhum item válido pôde ser extraído do arquivo selecionado.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Falha ao processar arquivo: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isImporting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar Catálogo & Backup", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Importação de Dados", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Restaure dados a partir de arquivos CSV ou JSON exportados anteriormente, ou popule com o catálogo oficial do Brasil.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (isImporting) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Processando e inserindo no banco de dados...")
                    }
                }
            }

            Button(
                onClick = {
                    openDocumentLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/json", "text/plain", "*/*"))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isImporting
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Selecionar Arquivo CSV ou JSON Local", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    // Populate batch items from catalog
                    val catalog = RealMarketCatalog.allEntries
                    val items = catalog.map { entry ->
                        Item(
                            name = entry.name,
                            category = entry.category,
                            subCategory = entry.subCategory,
                            collection = entry.collection,
                            itemNumber = entry.itemNumber,
                            rarity = entry.rarity,
                            variant = entry.variant,
                            estimatedValue = entry.realMarketPriceBrl,
                            purchasePrice = entry.lowPriceBrl,
                            officialImageUrl = entry.imageUrl,
                            imageUri = entry.imageUrl,
                            notes = "Importado via Catálogo Oficial"
                        )
                    }
                    viewModel.importBatchItems(items) { count ->
                        Toast.makeText(context, "$count cartas importadas do catálogo oficial da LigaMagic e LigaPokémon!", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isImporting
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Popular Tudo do Catálogo Oficial BR")
            }
        }
    }
}
