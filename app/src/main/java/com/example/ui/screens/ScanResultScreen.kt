package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.api.ScryfallApiClient
import com.example.data.Item
import com.example.data.RealCatalogEntry
import com.example.data.RealMarketCatalog
import com.example.ui.CollectorViewModel
import com.example.ui.components.ScryfallArtPickerSheet
import com.example.ui.navigation.Routes
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonRose
import com.example.util.AiSettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    viewModel: CollectorViewModel,
    navController: NavController
) {
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val currency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        viewModel.resetScanState()
        navController.popBackStack()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val hasAiKey = remember { AiSettingsManager.hasConfiguredApiKey(context) }

    val result = scanResult

    var customName by remember(result) { mutableStateOf(result?.detectedName ?: "") }
    var customSubCategory by remember(result) { mutableStateOf(result?.subCategory ?: "Magic: The Gathering") }
    var customCollection by remember(result) { mutableStateOf(result?.collection ?: "") }
    var customNumber by remember(result) { mutableStateOf(result?.itemNumber ?: "") }
    var customRarity by remember(result) { mutableStateOf(result?.rarity ?: "Comum") }
    var customPrice by remember(result) { mutableStateOf(result?.estimatedValue ?: 0.0) }
    var customImageUrl by remember(result) { mutableStateOf(result?.officialImageUrl ?: result?.capturedImageUri) }
    var officialScryfallUrl by remember(result) { mutableStateOf(result?.officialImageUrl) }
    var isFetchingScryfall by remember { mutableStateOf(false) }
    var showScryfallPickerSheet by remember { mutableStateOf(false) }
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUrl = uri.toString()
            Toast.makeText(context, "Foto personalizada selecionada!", Toast.LENGTH_SHORT).show()
        }
    }
    var isSaving by remember { mutableStateOf(false) }
    var isEditingNameDirectly by remember { mutableStateOf(false) }
    var showCatalogPickerSheet by remember { mutableStateOf(false) }
    var catalogSearchQuery by remember { mutableStateOf("") }

    var customEffectPt by remember(result) { mutableStateOf(result?.cardTranslatedEffect ?: result?.cardAttacks ?: "") }
    var customAttacks by remember(result) { mutableStateOf(result?.cardAttacks ?: "") }
    var customOracleEn by remember(result) { mutableStateOf(result?.cardOracleText ?: "") }
    var customHp by remember(result) { mutableStateOf(result?.cardHp ?: "") }
    var customArtist by remember(result) { mutableStateOf(result?.cardArtist ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultado da Análise", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetScanState()
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum resultado disponível.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Official Card Image
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (customImageUrl != null) {
                                Box(
                                    modifier = Modifier
                                        .height(280.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF1E1E1E))
                                ) {
                                    AsyncImage(
                                        model = customImageUrl,
                                        contentDescription = customName,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxHeight()
                                    )

                                    // Badge showing if image is Official Scryfall, Catalog, or User Photo
                                    val isScryfallOrOfficial = customImageUrl == officialScryfallUrl ||
                                        customImageUrl == result.officialImageUrl ||
                                        (customImageUrl?.contains("scryfall") == true)
                                    val isCaptured = customImageUrl == result.capturedImageUri

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isScryfallOrOfficial -> MaterialTheme.colorScheme.primaryContainer
                                            isCaptured -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                isScryfallOrOfficial -> "✨ Scryfall HD Oficial"
                                                isCaptured -> "📷 Foto da Câmera"
                                                else -> "🖼️ Da Galeria"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isScryfallOrOfficial -> MaterialTheme.colorScheme.onPrimaryContainer
                                                isCaptured -> MaterialTheme.colorScheme.onSecondaryContainer
                                                else -> MaterialTheme.colorScheme.onTertiaryContainer
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Image Selection Actions Bar (Scryfall / Ver Artes / Galeria / Alternar)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. One-tap fetch from Scryfall
                                    Button(
                                        onClick = {
                                            if (customName.isNotBlank() && !isFetchingScryfall) {
                                                isFetchingScryfall = true
                                                coroutineScope.launch {
                                                    try {
                                                        val match = ScryfallApiClient.getBestCardMatch(customName)
                                                        if (match != null) {
                                                            customImageUrl = match.imageUrlLarge
                                                            officialScryfallUrl = match.imageUrlLarge
                                                            if (customCollection.isBlank() || customCollection == "Coleção Oficial") {
                                                                customCollection = "[${match.setCode.uppercase()}] ${match.setName}"
                                                            }
                                                            if (customNumber.isBlank() || customNumber == "—") {
                                                                customNumber = match.collectorNumber
                                                            }
                                                            if (customRarity == "Comum" && match.rarity.isNotBlank()) {
                                                                customRarity = match.rarity
                                                            }
                                                            if (customOracleEn.isBlank() && !match.oracleText.isNullOrBlank()) {
                                                                customOracleEn = match.oracleText
                                                            }
                                                            if (customArtist.isBlank() && !match.artist.isNullOrBlank()) {
                                                                customArtist = match.artist
                                                            }
                                                            Toast.makeText(context, "✨ Foto oficial de '${match.name}' obtida no Scryfall!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            showScryfallPickerSheet = true
                                                            Toast.makeText(context, "Buscando versões no Scryfall...", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Erro Scryfall: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    } finally {
                                                        isFetchingScryfall = false
                                                    }
                                                }
                                            } else {
                                                showScryfallPickerSheet = true
                                            }
                                        },
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        if (isFetchingScryfall) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Buscando...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Pegar do Scryfall", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // 2. Open Scryfall Art Picker Grid
                                    OutlinedButton(
                                        onClick = { showScryfallPickerSheet = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ver Artes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 3. Option to select from phone gallery
                                    OutlinedButton(
                                        onClick = {
                                            galleryPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Da Galeria", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }

                                    // 4. Toggle back to captured photo or official
                                    val isShowingCaptured = customImageUrl == result.capturedImageUri
                                    val hasCaptured = !result.capturedImageUri.isNullOrBlank()
                                    val hasOfficial = !officialScryfallUrl.isNullOrBlank() || !result.officialImageUrl.isNullOrBlank()
                                    val officialTarget = officialScryfallUrl ?: result.officialImageUrl

                                    if (hasCaptured && hasOfficial) {
                                        FilledTonalButton(
                                            onClick = {
                                                if (isShowingCaptured) {
                                                    customImageUrl = officialTarget
                                                    Toast.makeText(context, "Exibindo arte oficial HD!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    customImageUrl = result.capturedImageUri
                                                    Toast.makeText(context, "Exibindo foto tirada pela câmera!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1.2f),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                if (isShowingCaptured) Icons.Default.Verified else Icons.Default.CameraAlt,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                if (isShowingCaptured) "Oficial HD" else "Foto Tirada",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Helpful hint when viewing camera photo
                                if (customImageUrl == result.capturedImageUri && !result.capturedImageUri.isNullOrBlank()) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("💡", fontSize = 12.sp)
                                            Text(
                                                "A foto da câmera ficou com reflexo ou corte? Toque em 'Pegar do Scryfall' para a arte oficial digitalizada!",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            if (isEditingNameDirectly) {
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    label = { Text("Nome da Carta") },
                                    trailingIcon = {
                                        IconButton(onClick = { isEditingNameDirectly = false }) {
                                            Icon(Icons.Default.Check, contentDescription = "Concluir")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        customName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    IconButton(onClick = { isEditingNameDirectly = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar Nome", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Text(
                                "$customCollection • #${customNumber} • $customRarity",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Interactive Game Type Chips (Enables user to confirm Magic or Pokémon)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = customSubCategory == "Magic: The Gathering",
                                    onClick = {
                                        customSubCategory = "Magic: The Gathering"
                                        val effect = viewModel.lookupCardEffect(customName, "Trading Cards", "Magic: The Gathering")
                                        if (effect.translatedEffectPt.isNotBlank()) {
                                            customEffectPt = effect.translatedEffectPt
                                            customOracleEn = effect.originalOracle
                                        }
                                    },
                                    label = { Text("🔮 Magic (LigaMagic)", fontSize = 12.sp) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = customSubCategory == "Pokémon TCG",
                                    onClick = {
                                        customSubCategory = "Pokémon TCG"
                                        val effect = viewModel.lookupCardEffect(customName, "Trading Cards", "Pokémon TCG")
                                        if (effect.translatedEffectPt.isNotBlank()) {
                                            customEffectPt = effect.translatedEffectPt
                                            customOracleEn = effect.originalOracle
                                        }
                                    },
                                    label = { Text("⚡ Pokémon (LigaPokémon)", fontSize = 12.sp) }
                                )
                            }

                            // Quick Button to Pick Card from Official Catalog
                            FilledTonalButton(
                                onClick = { showCatalogPickerSheet = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trocar por Carta do Catálogo Oficial", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NeonEmerald.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                    Text(
                                        result.authenticityStatus,
                                        color = NeonEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Key Tip Banner if no key configured
                if (!hasAiKey) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { navController.navigate(Routes.SETTINGS) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Reconhecimento Automático por Foto", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Para identificar o nome da carta automaticamente por IA, adicione sua chave gratuita do Gemini nas Configurações.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Official Brazilian Market Quotations Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Cotações Oficiais Brasil",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "Preço Médio: ${currency.formatValue(result.estimatedValue)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            HorizontalDivider()

                            result.marketOffers.forEach { offer ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(offer.storeName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(offer.listingType, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        currency.formatValue(offer.priceInBRL),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Card Effects & Translation in Portuguese Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f))
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "O que a Carta Faz (Tradução PT-BR)",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(
                                        "Traduzido Oficial",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                "Tradução automática do que a carta faz em jogo. Você pode editar, complementar ou adicionar novos efeitos abaixo antes de salvar.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Quick Translation Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (customOracleEn.isNotBlank()) {
                                    FilledTonalButton(
                                        onClick = {
                                            customEffectPt = viewModel.translateEffectText(customOracleEn)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Traduzir Texto", fontSize = 12.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        val effect = viewModel.lookupCardEffect(result.detectedName, result.category, result.subCategory)
                                        customEffectPt = effect.translatedEffectPt
                                        customOracleEn = effect.originalOracle
                                        customAttacks = effect.cardAttacks ?: ""
                                        customHp = effect.cardHp ?: ""
                                        customArtist = effect.cardArtist ?: ""
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Recarregar Oficial", fontSize = 12.sp)
                                }
                            }

                            OutlinedTextField(
                                value = customEffectPt,
                                onValueChange = { customEffectPt = it },
                                label = { Text("Efeito da Carta & O que faz (PT-BR)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6
                            )

                            OutlinedTextField(
                                value = customAttacks,
                                onValueChange = { customAttacks = it },
                                label = { Text("Ataques / Habilidades Principais") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            if (customOracleEn.isNotBlank()) {
                                OutlinedTextField(
                                    value = customOracleEn,
                                    onValueChange = { customOracleEn = it },
                                    label = { Text("Texto Original (Inglês)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    maxLines = 4
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customHp,
                                    onValueChange = { customHp = it },
                                    label = { Text("HP / Nível") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customArtist,
                                    onValueChange = { customArtist = it },
                                    label = { Text("Artista / Ilustrador") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Save to Collection Button
                item {
                    Button(
                        onClick = {
                            if (isSaving) return@Button
                            isSaving = true
                            try {
                                val targetCategory = if (customSubCategory.contains("Diecast", ignoreCase = true) || customSubCategory.contains("Carrinho", ignoreCase = true)) {
                                    "Carrinhos / Diecast"
                                } else {
                                    "Trading Cards"
                                }
                                val resolvedImageUri = customImageUrl?.ifBlank { null } ?: result.capturedImageUri ?: ""
                                val newItem = Item(
                                    name = customName.ifBlank { "Carta Colecionável" },
                                    category = targetCategory,
                                    subCategory = customSubCategory,
                                    collection = customCollection,
                                    itemNumber = customNumber,
                                    rarity = customRarity,
                                    estimatedValue = customPrice,
                                    purchasePrice = if (customPrice > 0.0) customPrice * 0.8 else 0.0,
                                    officialImageUrl = customImageUrl,
                                    imageUri = resolvedImageUri,
                                    notes = "Adicionado via escaneamento oficial",
                                    cardHp = customHp.ifBlank { null },
                                    cardArtist = customArtist.ifBlank { null },
                                    cardAttacks = customAttacks.ifBlank { null },
                                    cardOracleText = customOracleEn.ifBlank { null },
                                    cardTranslatedEffect = customEffectPt.ifBlank { null },
                                    energyType = result.energyType
                                )
                                viewModel.saveItem(newItem) { savedId ->
                                    isSaving = false
                                    viewModel.resetScanState()
                                    Toast.makeText(
                                        context,
                                        "✓ '${newItem.name}' salva com sucesso na sua coleção!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(Routes.COLLECTION) {
                                        popUpTo(Routes.SCANNER) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } catch (e: Throwable) {
                                isSaving = false
                                android.util.Log.e("ScanResultScreen", "Erro ao preparar carta para salvar: ${e.message}", e)
                                Toast.makeText(
                                    context,
                                    "Erro ao salvar: ${e.localizedMessage ?: "Tente novamente"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        enabled = !isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_scanned_item_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvando...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar na Minha Coleção", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet to Pick Card from Official Catalog
    if (showCatalogPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCatalogPickerSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Catálogo Oficial — Escolha sua Carta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Selecione a carta oficial do acervo da LigaMagic / LigaPokémon:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = catalogSearchQuery,
                    onValueChange = { catalogSearchQuery = it },
                    placeholder = { Text("Buscar carta (ex: Sheoldred, Charizard, Sol Ring...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (catalogSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { catalogSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Subcategory Filter Chips in the Modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = customSubCategory.contains("Magic", ignoreCase = true),
                        onClick = { customSubCategory = "Magic: The Gathering" },
                        label = { Text("🔮 Magic", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = customSubCategory.contains("Pokémon", ignoreCase = true) || customSubCategory.contains("Pokemon", ignoreCase = true),
                        onClick = { customSubCategory = "Pokémon TCG" },
                        label = { Text("⚡ Pokémon", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = !customSubCategory.contains("Magic", ignoreCase = true) && !customSubCategory.contains("Pokémon", ignoreCase = true),
                        onClick = { customSubCategory = "Todos" },
                        label = { Text("🌐 Todos", fontSize = 12.sp) }
                    )
                }

                val filteredEntries = remember(catalogSearchQuery, customSubCategory) {
                    RealMarketCatalog.allEntries.filter { entry ->
                        val matchesSubCategory = customSubCategory == "Todos" ||
                                entry.subCategory.contains(customSubCategory, ignoreCase = true) ||
                                entry.category.contains(customSubCategory, ignoreCase = true)
                        val matchesQuery = catalogSearchQuery.isBlank() ||
                                entry.name.contains(catalogSearchQuery, ignoreCase = true) ||
                                entry.collection.contains(catalogSearchQuery, ignoreCase = true) ||
                                entry.tags.contains(catalogSearchQuery, ignoreCase = true) ||
                                entry.itemNumber.contains(catalogSearchQuery, ignoreCase = true)
                        matchesSubCategory && matchesQuery
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEntries) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    customName = entry.name
                                    customCollection = entry.collection
                                    customNumber = entry.itemNumber
                                    customRarity = entry.rarity
                                    customPrice = entry.realMarketPriceBrl
                                    customImageUrl = entry.imageUrl
                                    customSubCategory = entry.subCategory

                                    val effect = viewModel.lookupCardEffect(entry.name, entry.category, entry.subCategory)
                                    customEffectPt = effect.translatedEffectPt
                                    customOracleEn = effect.originalOracle
                                    customAttacks = effect.cardAttacks ?: ""
                                    customHp = effect.cardHp ?: ""
                                    customArtist = effect.cardArtist ?: ""

                                    showCatalogPickerSheet = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = entry.imageUrl,
                                    contentDescription = entry.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "${entry.collection} • #${entry.itemNumber} • ${entry.rarity}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    currency.formatValue(entry.realMarketPriceBrl),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showScryfallPickerSheet) {
        ScryfallArtPickerSheet(
            initialQuery = customName,
            onDismiss = { showScryfallPickerSheet = false },
            onCardSelected = { card ->
                val chosenUrl = card.imageUrlLarge.ifBlank { card.imageUrlNormal }
                customImageUrl = chosenUrl
                officialScryfallUrl = chosenUrl
                if (customCollection.isBlank() || customCollection == "Coleção Oficial") {
                    customCollection = "[${card.setCode.uppercase()}] ${card.setName}"
                }
                if (customNumber.isBlank() || customNumber == "—") {
                    customNumber = card.collectorNumber
                }
                if (customRarity == "Comum" && card.rarity.isNotBlank()) {
                    customRarity = card.rarity
                }
                if (customOracleEn.isBlank() && !card.oracleText.isNullOrBlank()) {
                    customOracleEn = card.oracleText
                }
                if (customArtist.isBlank() && !card.artist.isNullOrBlank()) {
                    customArtist = card.artist
                }
                Toast.makeText(context, "✨ Arte oficial de '${card.name}' aplicada!", Toast.LENGTH_SHORT).show()
                showScryfallPickerSheet = false
            }
        )
    }
}
