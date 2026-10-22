package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.api.PriceSourceRegistry
import com.example.api.ScryfallApiClient
import com.example.data.Item
import com.example.ui.CollectorViewModel
import com.example.ui.components.ScryfallArtPickerSheet
import com.example.ui.navigation.Routes
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.util.AiAgentRegistry
import com.example.util.AiSettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    viewModel: CollectorViewModel,
    navController: NavController,
    itemId: Int
) {
    val context = LocalContext.current
    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val appMode by viewModel.appMode.collectAsStateWithLifecycle()

    val item = items.find { it.id == itemId }

    val coroutineScope = rememberCoroutineScope()
    var showScryfallPicker by remember { mutableStateOf(false) }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && item != null) {
            val updated = item.copy(imageUri = uri.toString())
            viewModel.saveItem(updated)
            android.widget.Toast.makeText(context, "✓ Foto atualizada pela galeria!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    var showEffectDialog by remember { mutableStateOf(false) }

    // Dialog state
    var editTranslatedEffect by remember { mutableStateOf("") }
    var editAttacks by remember { mutableStateOf("") }
    var editOracleText by remember { mutableStateOf("") }
    var editHp by remember { mutableStateOf("") }
    var editArtist by remember { mutableStateOf("") }
    var editEnergyType by remember { mutableStateOf("") }
    var editReleaseYear by remember { mutableStateOf("") }

    fun openEffectEditor(current: Item) {
        val existingEffect = current.cardTranslatedEffect
        val existingAttacks = current.cardAttacks
        val existingOracle = current.cardOracleText
        val existingHp = current.cardHp
        val existingArtist = current.cardArtist

        editEnergyType = current.energyType ?: current.resolvedEnergyType
        editReleaseYear = current.releaseYear?.toString() ?: current.resolvedReleaseYear?.toString() ?: ""

        if (!existingEffect.isNullOrBlank() || !existingAttacks.isNullOrBlank() || !existingOracle.isNullOrBlank()) {
            editTranslatedEffect = existingEffect ?: ""
            editAttacks = existingAttacks ?: ""
            editOracleText = existingOracle ?: ""
            editHp = existingHp ?: ""
            editArtist = existingArtist ?: ""
        } else {
            // Auto lookup from knowledge base
            val info = viewModel.lookupCardEffect(current.name, current.category, current.subCategory)
            editTranslatedEffect = info.translatedEffectPt
            editAttacks = info.cardAttacks ?: ""
            editOracleText = info.originalOracle
            editHp = info.cardHp ?: ""
            editArtist = info.cardArtist ?: ""
        }
        showEffectDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "Detalhes do Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (item != null) {
                        IconButton(onClick = { openEffectEditor(item) }) {
                            Icon(Icons.Default.EditNote, contentDescription = "Editar ou adicionar o que a carta faz")
                        }
                        IconButton(onClick = {
                            viewModel.toggleComparisonItem(item.id)
                            navController.navigate(Routes.COMPARE)
                        }) {
                            Icon(Icons.Default.CompareArrows, contentDescription = "Comparar")
                        }
                        IconButton(onClick = { viewModel.toggleFavorite(item) }) {
                            Icon(
                                if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = {
                            viewModel.deleteItem(item)
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (item == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Item não encontrado.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Official HD Card Picture with swap options
                val currentImg = item.officialImageUrl ?: item.imageUri
                val isOfficialScryfall = currentImg.contains("scryfall") || (!item.officialImageUrl.isNullOrBlank() && currentImg == item.officialImageUrl)
                val hasBothImages = !item.officialImageUrl.isNullOrBlank() && item.imageUri.isNotBlank() && item.imageUri != item.officialImageUrl

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentImg.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(androidx.compose.ui.graphics.Color(0xFF1E1E1E)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = currentImg,
                                    contentDescription = item.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxHeight()
                                )

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isOfficialScryfall) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = if (isOfficialScryfall) "✨ Arte Oficial HD" else "📷 Sua Foto",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOfficialScryfall) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        // Photo Swap Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showScryfallPicker = true },
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Foto Scryfall HD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    galleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Galeria", fontSize = 11.sp)
                            }

                            if (hasBothImages) {
                                FilledTonalButton(
                                    onClick = {
                                        val isCurrentlyOfficial = currentImg == item.officialImageUrl
                                        val nextImg = if (isCurrentlyOfficial) item.imageUri else (item.officialImageUrl ?: item.imageUri)
                                        val updated = item.copy(
                                            officialImageUrl = if (isCurrentlyOfficial) null else item.officialImageUrl,
                                            imageUri = nextImg
                                        )
                                        viewModel.saveItem(updated)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text("Alternar", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Header Info
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "${item.collection} • #${item.itemNumber} • ${item.rarity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val magicColor = item.resolvedMagicColor
                            val cardType = item.resolvedCardType
                            if (item.isMagicCard && magicColor.isNotBlank()) {
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                                    Text(
                                        "${item.typeEmoji} $magicColor",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (cardType.isNotBlank() && !cardType.equals(magicColor, ignoreCase = true)) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                        Text(
                                            cardType,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                val energy = item.resolvedEnergyType.ifEmpty { item.resolvedCardType }
                                if (energy.isNotBlank()) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                                        Text(
                                            "${item.typeEmoji} $energy",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            val year = item.resolvedReleaseYear
                            if (year != null) {
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        "Ano $year",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    item.condition,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (item.variant.isNotEmpty() && item.variant != "Normal") {
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(
                                        item.variant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // --- CARD EFFECTS & TRANSLATION SECTION: O QUE A CARTA FAZ ---
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
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    "O que a Carta Faz & Efeitos",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            FilledTonalIconButton(
                                onClick = { openEffectEditor(item) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar o que a carta faz", modifier = Modifier.size(18.dp))
                            }
                        }

                        // Badges for HP and Artist
                        if (!item.cardHp.isNullOrBlank() || !item.cardArtist.isNullOrBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!item.cardHp.isNullOrBlank()) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text(
                                            item.cardHp,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (!item.cardArtist.isNullOrBlank()) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                                        Text(
                                            "Artista: ${item.cardArtist}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Attacks / Abilities summary
                        if (!item.cardAttacks.isNullOrBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(
                                    item.cardAttacks,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Translated Effect in Portuguese
                        if (!item.cardTranslatedEffect.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "Tradução em Português (PT-BR):",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        item.cardTranslatedEffect,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }

                        // Original Oracle text if present
                        if (!item.cardOracleText.isNullOrBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "Texto Original (Inglês):",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        item.cardOracleText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

                        // Empty State if no effect is registered
                        if (item.cardTranslatedEffect.isNullOrBlank() && item.cardAttacks.isNullOrBlank() && item.cardOracleText.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Nenhum efeito ou tradução cadastrado para esta carta ainda.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = { openEffectEditor(item) }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Adicionar o que a Carta Faz", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Quotations Card
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Cotação e Mercado Brasileiro", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Preço Médio Estimado:")
                            Text(currency.formatValue(item.estimatedValue), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Preço de Aquisição:")
                            Text(currency.formatValue(item.purchasePrice), fontWeight = FontWeight.Bold)
                        }
                        if (item.purchasePrice > 0) {
                            val profit = item.profit
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Rentabilidade:")
                                Text(
                                    "${if (profit >= 0) "+" else ""}${currency.formatValue(profit)} (${String.format(java.util.Locale.US, "%.1f", item.profitPercentage)}%)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (profit >= 0) NeonEmerald else NeonRose
                                )
                            }
                        }
                    }
                }

                // Official Price Source Offers
                val (offers, _) = PriceSourceRegistry.generateRealisticOffersAndHistory(
                    itemName = item.name,
                    subCategory = item.subCategory,
                    rarity = item.rarity,
                    variant = item.variant,
                    baseEstimatedPrice = item.estimatedValue
                )

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Cotações Balcão LigaMagic / LigaPokémon", fontWeight = FontWeight.Bold)
                        HorizontalDivider()
                        offers.forEach { offer ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(offer.storeName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(offer.listingType, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(currency.formatValue(offer.priceInBRL), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // AI Agent Consultation Card
                val recommendedAgent = when {
                    item.isMagicCard -> AiAgentRegistry.AGENT_MTG
                    item.isPokemonCard -> AiAgentRegistry.AGENT_POKEMON
                    else -> AiAgentRegistry.AGENT_MARKET
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = recommendedAgent.primaryColor.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(recommendedAgent.avatarEmoji, fontSize = 22.sp)
                            Column {
                                Text(
                                    "Consultar ${recommendedAgent.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "${recommendedAgent.badge} • IA Especialista",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            "Tire dúvidas estratégicas, regras de jogo, potencial de valorização ou conservação sobre '${item.name}'.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    AiSettingsManager.setSelectedAgentId(context, recommendedAgent.id)
                                    navController.navigate(Routes.AI_ASSISTANT)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Perguntar ao ${recommendedAgent.title}", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    AiSettingsManager.setSelectedAgentId(context, AiAgentRegistry.AGENT_COUNCIL.id)
                                    navController.navigate(Routes.AI_ASSISTANT)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🏛️ Mesa Redonda", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG: ADICIONAR OU EDITAR O QUE A CARTA FAZ ---
    if (showEffectDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showEffectDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("O que a Carta Faz & Efeitos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Adicione ou edite os efeitos em português e as habilidades desta carta.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick lookup & translate actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val info = viewModel.lookupCardEffect(item.name, item.category, item.subCategory)
                                editTranslatedEffect = info.translatedEffectPt
                                editAttacks = info.cardAttacks ?: ""
                                editOracleText = info.originalOracle
                                editHp = info.cardHp ?: ""
                                editArtist = info.cardArtist ?: ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buscar Oficial", fontSize = 11.sp)
                        }

                        if (editOracleText.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    editTranslatedEffect = viewModel.translateEffectText(editOracleText)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Traduzir Texto", fontSize = 11.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editTranslatedEffect,
                        onValueChange = { editTranslatedEffect = it },
                        label = { Text("O que a carta faz (Tradução PT-BR)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6
                    )

                    OutlinedTextField(
                        value = editAttacks,
                        onValueChange = { editAttacks = it },
                        label = { Text("Ataques & Habilidades") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editOracleText,
                        onValueChange = { editOracleText = it },
                        label = { Text("Texto Original (Inglês)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editEnergyType,
                            onValueChange = { editEnergyType = it },
                            label = { Text("Tipo de Energia / Cor") },
                            placeholder = { Text("Ex: Fogo, Água, Elétrico") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editReleaseYear,
                            onValueChange = { editReleaseYear = it },
                            label = { Text("Ano de Lançamento") },
                            placeholder = { Text("Ex: 2024, 2023") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editHp,
                            onValueChange = { editHp = it },
                            label = { Text("HP / Poder") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editArtist,
                            onValueChange = { editArtist = it },
                            label = { Text("Artista / Ilustrador") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = item.copy(
                            cardTranslatedEffect = editTranslatedEffect.ifBlank { null },
                            cardAttacks = editAttacks.ifBlank { null },
                            cardOracleText = editOracleText.ifBlank { null },
                            cardHp = editHp.ifBlank { null },
                            cardArtist = editArtist.ifBlank { null },
                            energyType = editEnergyType.ifBlank { null },
                            releaseYear = editReleaseYear.toIntOrNull()
                        )
                        viewModel.saveItem(updated)
                        showEffectDialog = false
                    }
                ) {
                    Text("Salvar Efeitos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEffectDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showScryfallPicker && item != null) {
        ScryfallArtPickerSheet(
            initialQuery = item.name,
            onDismiss = { showScryfallPicker = false },
            onCardSelected = { card ->
                val newUrl = card.imageUrlLarge.ifBlank { card.imageUrlNormal }
                val updated = item.copy(
                    officialImageUrl = newUrl,
                    imageUri = newUrl,
                    cardArtist = card.artist ?: item.cardArtist,
                    cardOracleText = card.oracleText ?: item.cardOracleText
                )
                viewModel.saveItem(updated) {
                    android.widget.Toast.makeText(context, "✓ Imagem oficial do Scryfall atualizada!", android.widget.Toast.LENGTH_SHORT).show()
                }
                showScryfallPicker = false
            }
        )
    }
}
