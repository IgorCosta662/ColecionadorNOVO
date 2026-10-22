package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class AiCardRecognitionResult(
    val cardName: String,
    val gameType: String, // "Magic: The Gathering", "Pokémon TCG", "Yu-Gi-Oh!", etc.
    val collection: String,
    val itemNumber: String,
    val rarity: String,
    val cardType: String,
    val energyOrMana: String,
    val cardHp: String? = null,
    val attacksOrAbilities: String? = null,
    val artist: String? = null,
    val oracleText: String? = null,
    val translatedEffectPt: String? = null,
    val estimatedPriceBrl: Double = 0.0
)

object AiSettingsManager {

    private const val PREFS_NAME = "collector_ai_prefs"
    private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
    private const val KEY_AI_MODEL = "selected_ai_model"
    private const val KEY_AI_ASSISTANT_ENABLED = "ai_assistant_enabled"
    private const val KEY_SCAN_AI_ENABLED = "scan_ai_enabled"
    private const val KEY_SELECTED_AGENT_ID = "selected_agent_id"

    const val DEFAULT_MODEL = "gemini-3.5-flash"
    val AVAILABLE_MODELS = listOf(
        "gemini-3.5-flash" to "Gemini 3.5 Flash (Oficial - Rápido & Preciso)",
        "gemini-flash-latest" to "Gemini Flash Latest (Estável)",
        "gemini-3.1-pro-preview" to "Gemini 3.1 Pro Preview (Raciocínio Avançado)"
    )

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun getCustomApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
    }

    fun saveCustomApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun clearCustomApiKey(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
    }

    fun hasConfiguredApiKey(context: Context): Boolean {
        return getActiveApiKey(context).isNotBlank()
    }

    fun isUsingCustomKey(context: Context): Boolean {
        return getCustomApiKey(context).isNotBlank()
    }

    fun getActiveApiKey(context: Context): String {
        val customKey = getCustomApiKey(context)
        if (customKey.isNotBlank()) return customKey
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
    }

    fun getSelectedModel(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_AI_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        return if (saved == "gemini-2.5-flash" || saved.contains("2.0") || saved.contains("1.5")) {
            DEFAULT_MODEL
        } else {
            saved
        }
    }

    fun setSelectedModel(context: Context, model: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AI_MODEL, model).apply()
    }

    fun isAiAssistantEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AI_ASSISTANT_ENABLED, true)
    }

    fun setAiAssistantEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AI_ASSISTANT_ENABLED, enabled).apply()
    }

    fun isScanAiEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SCAN_AI_ENABLED, true)
    }

    fun setScanAiEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SCAN_AI_ENABLED, enabled).apply()
    }

    fun getSelectedAgentId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SELECTED_AGENT_ID, AiAgentRegistry.AGENT_COUNCIL.id) ?: AiAgentRegistry.AGENT_COUNCIL.id
    }

    fun setSelectedAgentId(context: Context, agentId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SELECTED_AGENT_ID, agentId).apply()
    }

    suspend fun testApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("A chave fornecida está em branco."))
        }
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$DEFAULT_MODEL:generateContent?key=$apiKey"
            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Responda apenas: 'Chave do Gemini validada com sucesso!'")
                            })
                        })
                    })
                })
            }

            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(bodyString)
                    errJson.optJSONObject("error")?.optString("message") ?: "Erro HTTP $code"
                } catch (e: Exception) {
                    "Erro HTTP $code: $bodyString"
                }
                return@withContext Result.failure(Exception("Falha na validação ($code): $errorMsg"))
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: "Conexão estabelecida com sucesso!"

            Result.success(text.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askGemini(
        context: Context,
        userPrompt: String,
        collectionContext: String = ""
    ): Result<String> {
        val selectedAgentId = getSelectedAgentId(context)
        val agent = AiAgentRegistry.getAgentById(selectedAgentId)
        return askGeminiAgent(context, agent, userPrompt, collectionContext)
    }

    suspend fun askGeminiAgent(
        context: Context,
        agent: AiAgent,
        userPrompt: String,
        collectionContext: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Nenhuma chave de IA configurada. Cada cliente/usuário deve adicionar sua própria chave gratuita do Google Gemini nas Configurações."))
        }
        val model = getSelectedModel(context)

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val systemInstructionText = """
                ${agent.systemPrompt}
                
                Nome do Agente: ${agent.name}
                Especialidade: ${agent.badge}
                Lema: "${agent.motto}"
                
                Contexto oficial do acervo do usuário atualmente:
                $collectionContext
            """.trimIndent()

            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstructionText\n\nPergunta do Colecionador: $userPrompt")
                            })
                        })
                    })
                })
            }

            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(bodyString)
                    errJson.optJSONObject("error")?.optString("message") ?: "Erro HTTP $code"
                } catch (e: Exception) {
                    "Erro HTTP $code"
                }
                return@withContext Result.failure(Exception("Erro na IA ($code): $errorMsg"))
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                Result.failure(Exception("O Agente não retornou resposta."))
            } else {
                Result.success(text.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun encodeBitmapToBase64(bitmap: Bitmap, maxDim: Int = 1024): String {
        val scale = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val max = maxOf(bitmap.width, bitmap.height)
            maxDim.toFloat() / max
        } else 1.0f

        val scaled = if (scale < 1.0f) {
            val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
            val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun identifyCardWithGeminiVision(
        context: Context,
        bitmap: Bitmap,
        preferredGameHint: String? = null
    ): Result<AiCardRecognitionResult> = withContext(Dispatchers.IO) {
        val apiKey = getActiveApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Nenhuma chave de IA configurada. Cada usuário deve adicionar sua própria chave gratuita do Google Gemini nas Configurações.")
            )
        }
        val model = getSelectedModel(context)

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val base64Image = encodeBitmapToBase64(bitmap)

            val prompt = """
Você é um especialista em autenticação, reconhecimento e avaliação de cartas colecionáveis (TCG) e colecionáveis.
Analise a imagem da carta enviada com extremo rigor visual (leia com máxima precisão o título/nome da carta no topo, tipo, arte, borda, símbolos, textos da caixa de regras).

INSTRUÇÃO CRÍTICA PARA NOME DA CARTA:
- Leia e transcreva com exatidão o NOME DA CARTA como impresso no topo da carta física.
- NUNCA invente ou use nomes padrão fixos. Se for "Counterspell", retorne "Counterspell". Se for "Lightning Bolt", retorne "Lightning Bolt". Se for "Sol Ring", retorne "Sol Ring". Se não conseguir ler com clareza, retorne o que for visível.

ATENÇÃO PARA IDENTIFICAÇÃO DO JOGO:
1. "Magic: The Gathering":
- Identifique pelos símbolos de mana (círculos branco, azul, preto, vermelho, verde, incolor), tipos como Criatura/Feitiço/Instantânea/Encantamento/Artefato/Terreno, ou caixa de texto característica de MTG.
- Se for Magic, o gameType DEVE ser "Magic: The Gathering". NUNCA a classifique como Pokémon TCG!
2. "Pokémon TCG":
- Identifique por indicação de HP/PV, criaturas Pokémon, energias elementais, estágios (Básico, Fase 1/2, ex).
3. "Yu-Gi-Oh!":
- Identifique por estrelas de nível, valores ATK/DEF, layout Yugioh.

Identifique os dados da carta:
- cardName: Nome oficial exato impresso no topo da carta
- gameType: "Magic: The Gathering" ou "Pokémon TCG" ou "Yu-Gi-Oh!" ou "Outro"
- collection: Nome da edição / expansão / set se visível ou deduzido
- itemNumber: Número da carta (ex: 107/281 ou 0049)
- rarity: Raridade (Mítica, Rara, Incomum, Comum, etc.)
- cardType: Tipo de carta (ex: Criatura Lendária, Mágica Instantânea, Artefato, Pokémon ex Fase 2)
- energyOrMana: Custo de mana (ex: 2BB, 1U) ou Tipo de energia
- cardHp: Poder/Resistência (ex: 4/5, 2/2) ou HP
- attacksOrAbilities: Ataques ou habilidades principais
- artist: Ilustrador da carta
- oracleText: Texto de regras em inglês
- translatedEffectPt: Tradução fiel das regras e efeitos para Português do Brasil (PT-BR)
- estimatedPriceBrl: Cotação média estimada em Reais no Brasil (número decimal, ex: 15.50)

Responda ESTRITAMENTE em formato JSON puro, sem blocos markdown (sem ```json), no seguinte formato:
{
  "cardName": "Nome exato da carta",
  "gameType": "Magic: The Gathering",
  "collection": "Coleção",
  "itemNumber": "Número",
  "rarity": "Raridade",
  "cardType": "Tipo",
  "energyOrMana": "Custo de Mana ou Energia",
  "cardHp": "P/T ou HP",
  "attacksOrAbilities": "Habilidade ou Ataque",
  "artist": "Nome do artista",
  "oracleText": "Texto original",
  "translatedEffectPt": "Efeito traduzido em Português",
  "estimatedPriceBrl": 0.0
}
""".trimIndent()

            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
            }

            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            // Try with selected model, with automatic fallback if model not found (404)
            val candidateModels = listOf(model, "gemini-3.5-flash", "gemini-flash-latest").distinct()
            var response: okhttp3.Response? = null
            var bodyString = ""
            var lastCode = 0

            for (candidate in candidateModels) {
                val callUrl = "https://generativelanguage.googleapis.com/v1beta/models/$candidate:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(callUrl)
                    .post(requestBody)
                    .build()

                val callResponse = httpClient.newCall(request).execute()
                lastCode = callResponse.code
                val callBody = callResponse.body?.string() ?: ""

                if (callResponse.isSuccessful) {
                    response = callResponse
                    bodyString = callBody
                    break
                } else if (callResponse.code == 404 || callResponse.code == 400) {
                    // Try next model
                    continue
                } else {
                    bodyString = callBody
                    break
                }
            }

            if (response == null || !response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(bodyString)
                    errJson.optJSONObject("error")?.optString("message") ?: "Erro HTTP $lastCode"
                } catch (e: Exception) {
                    "Erro HTTP $lastCode"
                }
                return@withContext Result.failure(Exception("Falha na API Gemini Vision ($lastCode): $errorMsg"))
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            if (rawText.isBlank()) {
                return@withContext Result.failure(Exception("A API Gemini Vision não retornou dados."))
            }

            val cleanedJson = rawText
                .replace("```json", "", ignoreCase = true)
                .replace("```", "")
                .trim()

            val cardObj = try {
                JSONObject(cleanedJson)
            } catch (e: Exception) {
                // If wrapped or preceded by text, find { ... }
                val start = cleanedJson.indexOf('{')
                val end = cleanedJson.lastIndexOf('}')
                if (start in 0 until end) {
                    JSONObject(cleanedJson.substring(start, end + 1))
                } else {
                    throw e
                }
            }

            val rawGameType = cardObj.optString("gameType", "")
            val detectedName = cardObj.optString("cardName", "Carta Desconhecida").trim()

            // Strict game type determination
            val finalGameType = when {
                preferredGameHint != null && preferredGameHint.contains("Magic", ignoreCase = true) -> "Magic: The Gathering"
                preferredGameHint != null && preferredGameHint.contains("Pokémon", ignoreCase = true) -> "Pokémon TCG"
                preferredGameHint != null && preferredGameHint.contains("Hot", ignoreCase = true) -> "Carrinhos / Diecast"
                rawGameType.contains("Magic", ignoreCase = true) -> "Magic: The Gathering"
                rawGameType.contains("Pokémon", ignoreCase = true) || rawGameType.contains("Pokemon", ignoreCase = true) -> "Pokémon TCG"
                rawGameType.contains("Yu-Gi", ignoreCase = true) -> "Yu-Gi-Oh!"
                detectedName.contains("Sheoldred", ignoreCase = true) ||
                        detectedName.contains("Sol Ring", ignoreCase = true) ||
                        detectedName.contains("Black Lotus", ignoreCase = true) ||
                        detectedName.contains("Counterspell", ignoreCase = true) ||
                        detectedName.contains("Lightning Bolt", ignoreCase = true) ||
                        detectedName.contains("Dark Ritual", ignoreCase = true) ||
                        detectedName.contains("Swords to Plowshares", ignoreCase = true) ||
                        detectedName.contains("One Ring", ignoreCase = true) -> "Magic: The Gathering"
                detectedName.contains("Charizard", ignoreCase = true) ||
                        detectedName.contains("Pikachu", ignoreCase = true) ||
                        detectedName.contains("Blastoise", ignoreCase = true) ||
                        detectedName.contains("Venusaur", ignoreCase = true) ||
                        detectedName.contains("Umbreon", ignoreCase = true) ||
                        detectedName.contains("Mew", ignoreCase = true) -> "Pokémon TCG"
                else -> if (rawGameType.isNotBlank()) rawGameType else "Magic: The Gathering"
            }

            Result.success(
                AiCardRecognitionResult(
                    cardName = detectedName,
                    gameType = finalGameType,
                    collection = cardObj.optString("collection", "Oficial"),
                    itemNumber = cardObj.optString("itemNumber", "001"),
                    rarity = cardObj.optString("rarity", "Rara"),
                    cardType = cardObj.optString("cardType", "Carta TCG"),
                    energyOrMana = cardObj.optString("energyOrMana", ""),
                    cardHp = cardObj.optString("cardHp").takeIf { it.isNotBlank() },
                    attacksOrAbilities = cardObj.optString("attacksOrAbilities").takeIf { it.isNotBlank() },
                    artist = cardObj.optString("artist").takeIf { it.isNotBlank() },
                    oracleText = cardObj.optString("oracleText").takeIf { it.isNotBlank() },
                    translatedEffectPt = cardObj.optString("translatedEffectPt").takeIf { it.isNotBlank() },
                    estimatedPriceBrl = cardObj.optDouble("estimatedPriceBrl", 0.0)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
