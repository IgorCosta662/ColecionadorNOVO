package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.AppCurrency
import com.example.ui.CollectorViewModel
import com.example.ui.navigation.Routes
import com.example.ui.theme.NeonEmerald
import com.example.util.AiSettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CollectorViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val currentCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()

    var customKeyInput by remember { mutableStateOf(AiSettingsManager.getCustomApiKey(context)) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isUsingCustomKey by remember { mutableStateOf(AiSettingsManager.isUsingCustomKey(context)) }

    var selectedModel by remember { mutableStateOf(AiSettingsManager.getSelectedModel(context)) }
    var isAssistantEnabled by remember { mutableStateOf(AiSettingsManager.isAiAssistantEnabled(context)) }
    var isScanAiEnabled by remember { mutableStateOf(AiSettingsManager.isScanAiEnabled(context)) }

    // Test Key state
    var isTestingKey by remember { mutableStateOf(false) }
    var testResultSuccess by remember { mutableStateOf<String?>(null) }
    var testResultError by remember { mutableStateOf<String?>(null) }

    fun refreshKeyState() {
        customKeyInput = AiSettingsManager.getCustomApiKey(context)
        isUsingCustomKey = AiSettingsManager.isUsingCustomKey(context)
        selectedModel = AiSettingsManager.getSelectedModel(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações & Chave de IA", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =====================================================================
            // 1. CHAVE DE INTELIGÊNCIA ARTIFICIAL (GEMINI API KEY)
            // =====================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Chave de IA (Gemini API)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUsingCustomKey) NeonEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isUsingCustomKey) NeonEmerald else MaterialTheme.colorScheme.error)
                                )
                                Text(
                                    if (isUsingCustomKey) "Chave Pessoal Ativa" else "Nenhuma Chave Configurada",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUsingCustomKey) NeonEmerald else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Text(
                        "Cada cliente ou usuário deve configurar sua própria chave gratuita de API do Google Gemini para habilitar o Assistente de Coleção e os recursos inteligentes.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Input Field with Visibility Toggle & Paste
                    OutlinedTextField(
                        value = customKeyInput,
                        onValueChange = {
                            customKeyInput = it
                            testResultSuccess = null
                            testResultError = null
                        },
                        label = { Text("Sua Chave de API Gemini") },
                        placeholder = { Text("Cole aqui sua chave (ex: AIzaSy...)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input"),
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isKeyVisible) "Ocultar" else "Mostrar"
                                    )
                                }
                                if (customKeyInput.isNotEmpty()) {
                                    IconButton(onClick = {
                                        customKeyInput = ""
                                        testResultSuccess = null
                                        testResultError = null
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                    }
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (customKeyInput.isNotBlank()) {
                                AiSettingsManager.saveCustomApiKey(context, customKeyInput)
                                refreshKeyState()
                                Toast.makeText(context, "Chave de IA salva com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                        }),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick Paste Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val text = clip.getItemAt(0).text?.toString() ?: ""
                                    if (text.isNotBlank()) {
                                        customKeyInput = text.trim()
                                        Toast.makeText(context, "Chave colada da área de transferência!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Área de transferência vazia.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Colar da Área de Transferência", fontSize = 12.sp)
                        }
                    }

                    // Test Results Banner
                    if (testResultSuccess != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = NeonEmerald.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald)
                                Text(testResultSuccess ?: "", fontSize = 12.sp, color = NeonEmerald, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    if (testResultError != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(testResultError ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Save Button
                        Button(
                            onClick = {
                                if (customKeyInput.isNotBlank()) {
                                    AiSettingsManager.saveCustomApiKey(context, customKeyInput)
                                    refreshKeyState()
                                    Toast.makeText(context, "✓ Sua chave pessoal de IA foi salva e está ativa!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Digite uma chave antes de salvar.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar Chave", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Test Button
                        OutlinedButton(
                            onClick = {
                                val keyToTest = if (customKeyInput.isNotBlank()) customKeyInput else AiSettingsManager.getActiveApiKey(context)
                                if (keyToTest.isBlank()) {
                                    testResultError = "Nenhuma chave disponível para testar."
                                    return@OutlinedButton
                                }
                                isTestingKey = true
                                testResultSuccess = null
                                testResultError = null

                                coroutineScope.launch {
                                    val result = AiSettingsManager.testApiKey(keyToTest)
                                    isTestingKey = false
                                    result.fold(
                                        onSuccess = { responseText ->
                                            testResultSuccess = "✓ Conexão bem-sucedida! Resposta da IA: \"$responseText\""
                                        },
                                        onFailure = { error ->
                                            testResultError = "Falha na validação: ${error.localizedMessage}"
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isTestingKey
                        ) {
                            if (isTestingKey) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testando...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Testar Chave", fontSize = 13.sp)
                            }
                        }
                    }

                    // Clear user custom key
                    if (isUsingCustomKey) {
                        TextButton(
                            onClick = {
                                AiSettingsManager.clearCustomApiKey(context)
                                refreshKeyState()
                                testResultSuccess = null
                                testResultError = null
                                Toast.makeText(context, "Chave pessoal removida com sucesso.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remover / Limpar Minha Chave", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    // Guide Card: How to get a free key
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("Como criar sua chave gratuita do Gemini:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(
                                "1. Acesse o site: aistudio.google.com/apikey no seu navegador.\n" +
                                "2. Faça login com sua conta Google e clique em 'Create API Key'.\n" +
                                "3. Copie a chave gerada e cole no campo acima.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // =====================================================================
            // 2. MODELO DE IA PREFERENCIAL
            // =====================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Modelo de Inteligência Artificial", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Text("Selecione o modelo do Google Gemini que responderá às suas dúvidas no Assistente:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    AiSettingsManager.AVAILABLE_MODELS.forEach { (modelId, label) ->
                        val isSelected = selectedModel == modelId
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            onClick = {
                                selectedModel = modelId
                                AiSettingsManager.setSelectedModel(context, modelId)
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedModel = modelId
                                        AiSettingsManager.setSelectedModel(context, modelId)
                                    }
                                )
                                Column {
                                    Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                                    Text(modelId, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // =====================================================================
            // 3. RECURSOS INTELIGENTES
            // =====================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Recursos Inteligentes de Coleção", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Assistente IA da Coleção", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Permite conversar sobre valorizações, decks e raridades.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isAssistantEnabled,
                            onCheckedChange = {
                                isAssistantEnabled = it
                                AiSettingsManager.setAiAssistantEnabled(context, it)
                            }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Análise com IA ao Escanear", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Identifica edições e cotações automaticamente pela foto.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isScanAiEnabled,
                            onCheckedChange = {
                                isScanAiEnabled = it
                                AiSettingsManager.setScanAiEnabled(context, it)
                            }
                        )
                    }
                }
            }

            // =====================================================================
            // 4. MOEDA E EXIBIÇÃO
            // =====================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Moeda da Coleção", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppCurrency.entries.forEach { currency ->
                            val isSelected = currentCurrency == currency
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setCurrency(currency) },
                                label = {
                                    Text("${currency.symbol} (${currency.code})", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // =====================================================================
            // 5. ATALHO PARA ASSISTENTE IA & DADOS DO ACERVO
            // =====================================================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Status da Coleção", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total de Itens Cadastrados:")
                        Text("${items.size} itens", fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Banco de Dados:")
                        Text("Room Database v3 (Local & Seguro)", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Button(
                        onClick = { navController.navigate(Routes.AI_ASSISTANT) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir Assistente IA Agora", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
