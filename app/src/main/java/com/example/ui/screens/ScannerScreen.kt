package com.example.ui.screens

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.ImageStorageHelper
import com.example.data.RealMarketCatalog
import com.example.ui.CollectorViewModel
import com.example.ui.components.CameraXCardScannerView
import com.example.ui.navigation.Routes
import com.example.ui.theme.LigaMagicBlue
import com.example.ui.theme.LigaPokemonOrange
import com.example.ui.theme.NeonEmerald
import com.example.util.AiSettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: CollectorViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    val hasAiKey = remember { AiSettingsManager.hasConfiguredApiKey(context) }
    var selectedGameType by remember { mutableStateOf(if (hasAiKey) "✨ Detecção Automática (IA)" else "🔮 Magic: The Gathering") }
    var showCatalogPickerSheet by remember { mutableStateOf(false) }

    fun resolveCategoryHint(): String = when (selectedGameType) {
        "🔮 Magic: The Gathering" -> "Magic: The Gathering"
        "⚡ Pokémon TCG" -> "Pokémon TCG"
        "🏎️ Hot Wheels" -> "Carrinhos / Diecast"
        else -> "Auto"
    }

    // Reset scan state when entering ScannerScreen to avoid stale redirects
    DisposableEffect(Unit) {
        viewModel.resetScanState()
        onDispose { }
    }

    LaunchedEffect(scanResult) {
        if (scanResult != null) {
            navController.navigate(Routes.SCAN_RESULT)
        }
    }

    // Camera Launcher with safe bitmap callback
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        try {
            if (bitmap != null) {
                viewModel.analyzeCard(bitmap, resolveCategoryHint())
            } else {
                Toast.makeText(context, "Foto cancelada. Você também pode usar a Galeria ou o Catálogo abaixo.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Throwable) {
            android.util.Log.e("ScannerScreen", "Erro no retorno da câmera: ${e.localizedMessage}", e)
            Toast.makeText(context, "Não foi possível carregar a captura da câmera. Tente selecionar da Galeria.", Toast.LENGTH_LONG).show()
        }
    }

    fun safeLaunchCamera() {
        try {
            val pm = context.packageManager
            val testIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            val activities = testIntent.resolveActivity(pm)
            if (activities == null && !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                Toast.makeText(
                    context,
                    "Câmera não disponível no dispositivo. Escolha uma foto da galeria ou uma carta do catálogo abaixo.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
            cameraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "Nenhum aplicativo de câmera disponível no dispositivo. Escolha uma foto da galeria abaixo.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Permissão de câmera não concedida pelo sistema. Escolha uma foto da galeria.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Throwable) {
            android.util.Log.e("ScannerScreen", "Erro ao abrir câmera: ${e.localizedMessage}", e)
            Toast.makeText(
                context,
                "Não foi possível abrir a câmera. Use a opção 'Escolher Foto da Galeria' abaixo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Permission Launcher for Android 6.0+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            safeLaunchCamera()
        } else {
            Toast.makeText(
                context,
                "Permissão de câmera necessária para fotografar. Você também pode escolher da galeria.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun handleTakePhoto() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            safeLaunchCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Photo Picker Launcher (zero-permission standard Android photo picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        try {
            if (uri != null) {
                val bitmap = ImageStorageHelper.loadBitmapFromUri(context, uri.toString())
                if (bitmap != null) {
                    viewModel.analyzeCard(bitmap, resolveCategoryHint())
                } else {
                    Toast.makeText(context, "Não foi possível carregar a imagem da galeria.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("ScannerScreen", "Erro ao carregar imagem: ${e.localizedMessage}", e)
            Toast.makeText(context, "Erro ao processar imagem selecionada.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear e Analisar Carta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações de IA")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Instructions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Identificação Oficial & Cotação Brasil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Fotografe sua carta Magic, Pokémon ou miniatura. A IA identifica com precisão o jogo, extrai o texto traduzido e sincroniza com LigaMagic e LigaPokémon.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Game Type Filters
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Selecione o jogo ou deixe em Detecção Automática com IA:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedGameType == "🔮 Magic: The Gathering",
                        onClick = { selectedGameType = "🔮 Magic: The Gathering" },
                        label = { Text("🔮 Magic: The Gathering (LigaMagic)") }
                    )
                    FilterChip(
                        selected = selectedGameType == "⚡ Pokémon TCG",
                        onClick = { selectedGameType = "⚡ Pokémon TCG" },
                        label = { Text("⚡ Pokémon TCG (LigaPokémon)") }
                    )
                    FilterChip(
                        selected = selectedGameType == "✨ Detecção Automática (IA)",
                        onClick = { selectedGameType = "✨ Detecção Automática (IA)" },
                        label = { Text("✨ Auto / IA Gemini Vision") }
                    )
                    FilterChip(
                        selected = selectedGameType == "🏎️ Hot Wheels",
                        onClick = { selectedGameType = "🏎️ Hot Wheels" },
                        label = { Text("🏎️ Hot Wheels / Diecast") }
                    )
                }
            }

            // AI Status & Routing Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { navController.navigate(Routes.SETTINGS) },
                color = if (hasAiKey) NeonEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (hasAiKey) Icons.Default.AutoAwesome else Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = if (hasAiKey) NeonEmerald else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                if (hasAiKey) "IA Gemini Vision Pronta para Fotos" else "Chave de IA Não Configurada",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (hasAiKey) NeonEmerald else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (hasAiKey)
                                    "A IA diferencia Magic vs Pokémon automaticamente sem converter o jogo."
                                else
                                    "Para reconhecimento automático por foto, configure sua chave Gemini. Sem ela, o jogo segue o filtro acima.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // CameraX Live In-App Viewfinder Component
            if (isAnalyzing) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                            Text(
                                "Analisando Carta com IA...",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                            Text(
                                "Identificando detalhes da carta e consultando bases oficiais da LigaMagic / LigaPokémon...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                CameraXCardScannerView(
                    modifier = Modifier.fillMaxWidth(),
                    isCapturing = isAnalyzing,
                    onImageCaptured = { bitmap ->
                        viewModel.analyzeCard(bitmap, resolveCategoryHint())
                    },
                    onFallbackToGallery = {
                        try {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao abrir galeria: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFallbackToCatalog = {
                        showCatalogPickerSheet = true
                    }
                )
            }

            // Secondary Alternative Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button: Choose from Photo Gallery
                OutlinedButton(
                    onClick = {
                        try {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao abrir galeria: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("open_gallery_button"),
                    enabled = !isAnalyzing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Galeria", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                // Button: Select Directly from Catalog
                FilledTonalButton(
                    onClick = { showCatalogPickerSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !isAnalyzing,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Catálogo", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            // Fallback System Camera Link (if user prefers external app)
            TextButton(
                onClick = { handleTakePhoto() },
                modifier = Modifier.testTag("open_external_camera_link")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Usar aplicativo de câmera externo", fontSize = 12.sp)
            }

            // Bottom Demo Card for Instant Testing
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val fakeBitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
                        viewModel.analyzeCard(fakeBitmap, resolveCategoryHint())
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Simular Análise de Teste", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Testa reconhecimento do jogo selecionado ($selectedGameType)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    // Modal Bottom Sheet: Select card from catalog for instant scan
    if (showCatalogPickerSheet) {
        var sheetCategoryFilter by remember {
            mutableStateOf(
                if (selectedGameType.contains("Magic")) "🔮 Magic: The Gathering"
                else if (selectedGameType.contains("Pok")) "⚡ Pokémon TCG"
                else "Todas as Cartas"
            )
        }

        val catalogEntries = remember(sheetCategoryFilter) {
            RealMarketCatalog.allEntries.filter { entry ->
                when (sheetCategoryFilter) {
                    "🔮 Magic: The Gathering" -> entry.subCategory.contains("Magic", ignoreCase = true)
                    "⚡ Pokémon TCG" -> entry.subCategory.contains("Pokémon", ignoreCase = true) || entry.subCategory.contains("Pokemon", ignoreCase = true)
                    "🏎️ Hot Wheels" -> entry.category.contains("Diecast", ignoreCase = true)
                    else -> true
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showCatalogPickerSheet = false },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Escolher Carta para Análise", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { showCatalogPickerSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                // Filter chips inside sheet
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todas as Cartas", "🔮 Magic: The Gathering", "⚡ Pokémon TCG", "🏎️ Hot Wheels").forEach { cat ->
                        FilterChip(
                            selected = sheetCategoryFilter == cat,
                            onClick = { sheetCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Text(
                    "Toque em qualquer carta para carregar a cotação oficial em tempo real da Liga correspondente:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    catalogEntries.forEach { entry ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    showCatalogPickerSheet = false
                                    viewModel.applyCandidateToScan(entry)
                                    navController.navigate(Routes.SCAN_RESULT)
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface),
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
                                        Icon(Icons.Default.Style, contentDescription = null, modifier = Modifier.size(24.dp))
                                    }
                                }

                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                                    fontSize = 13.sp,
                                    color = NeonEmerald
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
