package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * In-App CameraX Preview & Capture Component.
 * Allows capturing card images directly inside the app without launching external camera apps,
 * preventing activity recreation, process death, and camera intent crashes.
 */
@Composable
fun CameraXCardScannerView(
    modifier: Modifier = Modifier,
    isCapturing: Boolean = false,
    onImageCaptured: (Bitmap) -> Unit,
    onFallbackToGallery: () -> Unit,
    onFallbackToCatalog: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(
                context,
                "Permissão de câmera negada. Você ainda pode usar fotos da galeria ou o catálogo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionFallbackCard(
            modifier = modifier,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onFallbackToGallery = onFallbackToGallery,
            onFallbackToCatalog = onFallbackToCatalog
        )
    } else {
        CameraXLiveViewfinder(
            modifier = modifier,
            isCapturing = isCapturing,
            onImageCaptured = onImageCaptured,
            onFallbackToGallery = onFallbackToGallery
        )
    }
}

@Composable
private fun CameraPermissionFallbackCard(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    onFallbackToGallery: () -> Unit,
    onFallbackToCatalog: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                "Câmera ao Vivo para Cartas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                "Para fotografar e escanear suas cartas Magic e Pokémon diretamente sem sair do app, permita o acesso à câmera.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("request_camera_permission_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Permitir Acesso à Câmera")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onFallbackToGallery,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galeria", fontSize = 12.sp)
                }
                FilledTonalButton(
                    onClick = onFallbackToCatalog,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ManageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Catálogo", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CameraXLiveViewfinder(
    modifier: Modifier = Modifier,
    isCapturing: Boolean,
    onImageCaptured: (Bitmap) -> Unit,
    onFallbackToGallery: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isTorchEnabled by remember { mutableStateOf(false) }
    var cameraInitError by remember { mutableStateOf<String?>(null) }
    var isProcessingCapture by remember { mutableStateOf(false) }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // Single-thread executor for camera background operations
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Check camera hardware
    val hasCameraHardware = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    if (!hasCameraHardware || cameraInitError != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.VideocamOff,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Câmera não inicializada",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    cameraInitError ?: "Dispositivo não possui sensor de câmera ativo (modo emulador). Escolha uma foto da galeria para analisar:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onFallbackToGallery,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escolher Foto da Galeria")
                }
            }
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
    ) {
        // CameraX PreviewView hosted inside Compose AndroidView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        provider.unbindAll()
                        boundCamera = provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraX", "Binding failed", e)
                        cameraInitError = "Falha ao inicializar câmera: ${e.localizedMessage}"
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = { previewView ->
                // Handle lens change or rebinding
                val provider = cameraProvider
                if (provider != null) {
                    try {
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        provider.unbindAll()
                        boundCamera = provider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        Log.e("CameraX", "Re-binding failed", e)
                    }
                }
            }
        )

        // Viewfinder Card Guide Overlay (Aspect ratio ~ 5:7 for TCG cards)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(0.71f) // Standard 2.5 x 3.5 inches card ratio
                    .border(
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // Subtle corner markers
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                ) {
                    Text(
                        "Alinhe a Carta",
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Top Controls: Torch / Flash & Lens Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash/Torch Toggle
            Surface(
                shape = CircleShape,
                color = if (isTorchEnabled) Color(0xFFFFD700).copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = {
                        val cam = boundCamera
                        if (cam != null && cam.cameraInfo.hasFlashUnit()) {
                            val nextTorch = !isTorchEnabled
                            cam.cameraControl.enableTorch(nextTorch)
                            isTorchEnabled = nextTorch
                        } else {
                            Toast.makeText(context, "Flash indisponível neste sensor", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(
                        if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash / Lanterna",
                        tint = if (isTorchEnabled) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Live Indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00E676), CircleShape)
                    )
                    Text("CameraX Direto", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Lens Switch (Back / Front)
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.FlipCameraAndroid,
                        contentDescription = "Alternar Câmera",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Capture Shutter Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val busy = isCapturing || isProcessingCapture

            // Main Shutter Button
            Surface(
                shape = CircleShape,
                color = if (busy) Color.Gray else Color.White,
                border = BorderStroke(4.dp, if (busy) Color.LightGray else MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(64.dp)
                    .testTag("camerax_shutter_button")
            ) {
                IconButton(
                    onClick = {
                        if (busy) return@IconButton
                        isProcessingCapture = true

                        imageCapture.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    try {
                                        val rawBitmap = image.toBitmap()
                                        val rotationDegrees = image.imageInfo.rotationDegrees

                                        val finalBitmap = if (rotationDegrees != 0) {
                                            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                                            Bitmap.createBitmap(
                                                rawBitmap,
                                                0,
                                                0,
                                                rawBitmap.width,
                                                rawBitmap.height,
                                                matrix,
                                                true
                                            )
                                        } else {
                                            rawBitmap
                                        }

                                        ContextCompat.getMainExecutor(context).execute {
                                            isProcessingCapture = false
                                            onImageCaptured(finalBitmap)
                                        }
                                    } catch (e: Throwable) {
                                        Log.e("CameraX", "Erro ao processar imagem capturada", e)
                                        ContextCompat.getMainExecutor(context).execute {
                                            isProcessingCapture = false
                                            Toast.makeText(context, "Erro ao processar captura da câmera", Toast.LENGTH_SHORT).show()
                                        }
                                    } finally {
                                        image.close()
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraX", "Falha na captura: ${exception.message}", exception)
                                    ContextCompat.getMainExecutor(context).execute {
                                        isProcessingCapture = false
                                        Toast.makeText(
                                            context,
                                            "Não foi possível capturar a foto. Tente novamente.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    },
                    enabled = !busy
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = "Capturar Carta",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
