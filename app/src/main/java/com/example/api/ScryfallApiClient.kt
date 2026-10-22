package com.example.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class ScryfallCard(
    val id: String,
    val name: String,
    val printedName: String? = null,
    val setCode: String,
    val setName: String,
    val collectorNumber: String,
    val rarity: String,
    val imageUrlNormal: String,
    val imageUrlLarge: String,
    val imageUrlPng: String? = null,
    val manaCost: String? = null,
    val typeLine: String? = null,
    val oracleText: String? = null,
    val printedText: String? = null,
    val artist: String? = null,
    val priceUsd: Double? = null,
    val priceEur: Double? = null
)

object ScryfallApiClient {
    private const val BASE_URL = "https://api.scryfall.com"
    private const val TAG = "ScryfallApiClient"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Search named card: /cards/named?fuzzy={name}
     * Returns the best matching Scryfall card.
     */
    suspend fun getCardByNameFuzzy(name: String, setCode: String? = null): ScryfallCard? = withContext(Dispatchers.IO) {
        if (name.isBlank()) return@withContext null
        try {
            val encodedName = URLEncoder.encode(name.trim(), "UTF-8")
            val urlBuilder = StringBuilder("$BASE_URL/cards/named?fuzzy=$encodedName")
            if (!setCode.isNullOrBlank()) {
                urlBuilder.append("&set=").append(URLEncoder.encode(setCode.trim().lowercase(), "UTF-8"))
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .header("User-Agent", "ColecionadorApp/1.0 (Android; Scryfall Integration)")
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Scryfall fuzzy search HTTP ${response.code} for '$name'")
                return@withContext null
            }

            val body = response.body?.string() ?: return@withContext null
            parseCardJson(JSONObject(body))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Scryfall for '$name': ${e.message}", e)
            null
        }
    }

    /**
     * Best matching lookup: first tries exact/fuzzy named search, then falls back to cards search.
     */
    suspend fun getBestCardMatch(name: String, setCode: String? = null): ScryfallCard? {
        val fuzzy = getCardByNameFuzzy(name, setCode)
        if (fuzzy != null) return fuzzy
        val search = searchCards(name)
        return search.firstOrNull()
    }

    /**
     * Search cards using query: /cards/search?q={query}
     * Useful for name search, set filters, or auto-complete.
     */
    suspend fun searchCards(query: String): List<ScryfallCard> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val encoded = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "$BASE_URL/cards/search?q=$encoded&include_multilingual=true"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ColecionadorApp/1.0 (Android; Scryfall Integration)")
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            val body = response.body?.string() ?: return@withContext emptyList()
            val root = JSONObject(body)
            val dataArray = root.optJSONArray("data") ?: return@withContext emptyList()

            val list = mutableListOf<ScryfallCard>()
            for (i in 0 until minOf(dataArray.length(), 20)) {
                val cardObj = dataArray.getJSONObject(i)
                parseCardJson(cardObj)?.let { list.add(it) }
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error searching Scryfall: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get direct image URL for a card name:
     * Scryfall provides direct redirect: https://api.scryfall.com/cards/named?exact=...&format=image&version=large
     * Or can extract directly from parsed JSON.
     */
    fun getDirectImageUrl(cardName: String, version: String = "large"): String {
        val encoded = URLEncoder.encode(cardName.trim(), "UTF-8")
        return "$BASE_URL/cards/named?fuzzy=$encoded&format=image&version=$version"
    }

    private fun parseCardJson(obj: JSONObject): ScryfallCard? {
        val id = obj.optString("id")
        val name = obj.optString("name")
        val printedName = obj.optString("printed_name").ifEmpty { null }
        val setCode = obj.optString("set", "")
        val setName = obj.optString("set_name", "")
        val collectorNumber = obj.optString("collector_number", "")
        val rarity = when (obj.optString("rarity", "common").lowercase()) {
            "mythic" -> "Mítica"
            "rare" -> "Rara"
            "uncommon" -> "Incomum"
            "common" -> "Comum"
            "special" -> "Especial"
            else -> "Rara"
        }

        // Image URIs (check root image_uris, or card_faces for double-faced cards)
        var normalImg = ""
        var largeImg = ""
        var pngImg: String? = null

        val imageUris = obj.optJSONObject("image_uris")
        if (imageUris != null) {
            normalImg = imageUris.optString("normal")
            largeImg = imageUris.optString("large").ifEmpty { normalImg }
            pngImg = imageUris.optString("png").ifEmpty { null }
        } else {
            // Check double-faced card faces
            val cardFaces = obj.optJSONArray("card_faces")
            if (cardFaces != null && cardFaces.length() > 0) {
                val firstFace = cardFaces.getJSONObject(0)
                val faceUris = firstFace.optJSONObject("image_uris")
                if (faceUris != null) {
                    normalImg = faceUris.optString("normal")
                    largeImg = faceUris.optString("large").ifEmpty { normalImg }
                    pngImg = faceUris.optString("png").ifEmpty { null }
                }
            }
        }

        if (largeImg.isBlank() && normalImg.isBlank()) {
            return null
        }

        val manaCost = obj.optString("mana_cost").ifEmpty { null }
        val typeLine = obj.optString("type_line").ifEmpty { null }
        val oracleText = obj.optString("oracle_text").ifEmpty { null }
        val printedText = obj.optString("printed_text").ifEmpty { null }
        val artist = obj.optString("artist").ifEmpty { null }

        val prices = obj.optJSONObject("prices")
        val priceUsd = prices?.optString("usd")?.toDoubleOrNull()
        val priceEur = prices?.optString("eur")?.toDoubleOrNull()

        return ScryfallCard(
            id = id,
            name = name,
            printedName = printedName,
            setCode = setCode,
            setName = setName,
            collectorNumber = collectorNumber,
            rarity = rarity,
            imageUrlNormal = normalImg.ifBlank { largeImg },
            imageUrlLarge = largeImg.ifBlank { normalImg },
            imageUrlPng = pngImg,
            manaCost = manaCost,
            typeLine = typeLine,
            oracleText = oracleText,
            printedText = printedText,
            artist = artist,
            priceUsd = priceUsd,
            priceEur = priceEur
        )
    }
}
