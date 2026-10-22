package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.Item
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.Locale

object CollectionBackupManager {

    fun exportToCsv(items: List<Item>): String {
        val sb = StringBuilder()
        // Header
        sb.append("id,name,category,subCategory,collection,itemNumber,rarity,variant,condition,purchasePrice,estimatedValue,quantity,storageLocation,notes,imageUri\n")
        items.forEach { item ->
            val line = listOf(
                item.id.toString(),
                escapeCsv(item.name),
                escapeCsv(item.category),
                escapeCsv(item.subCategory),
                escapeCsv(item.collection),
                escapeCsv(item.itemNumber),
                escapeCsv(item.rarity),
                escapeCsv(item.variant),
                escapeCsv(item.condition),
                String.format(Locale.US, "%.2f", item.purchasePrice),
                String.format(Locale.US, "%.2f", item.estimatedValue),
                item.quantity.toString(),
                escapeCsv(item.storageLocation),
                escapeCsv(item.notes),
                escapeCsv(item.officialImageUrl ?: item.imageUri)
            ).joinToString(",")
            sb.append(line).append("\n")
        }
        return sb.toString()
    }

    fun exportToJson(items: List<Item>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("name", item.name)
                put("category", item.category)
                put("subCategory", item.subCategory)
                put("collection", item.collection)
                put("itemNumber", item.itemNumber)
                put("rarity", item.rarity)
                put("variant", item.variant)
                put("condition", item.condition)
                put("purchasePrice", item.purchasePrice)
                put("estimatedValue", item.estimatedValue)
                put("quantity", item.quantity)
                put("storageLocation", item.storageLocation)
                put("notes", item.notes)
                put("imageUrl", item.officialImageUrl ?: item.imageUri)
                put("cardHp", item.cardHp ?: "")
                put("cardArtist", item.cardArtist ?: "")
                put("cardAttacks", item.cardAttacks ?: "")
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString(2)
    }

    fun parseCsv(content: String): List<Item> {
        val list = mutableListOf<Item>()
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return list

        val hasHeader = lines.first().contains("name", ignoreCase = true)
        val dataLines = if (hasHeader) lines.drop(1) else lines

        dataLines.forEach { line ->
            val cols = parseCsvLine(line)
            if (cols.isNotEmpty()) {
                val name = cols.getOrNull(1)?.ifBlank { cols.getOrNull(0) } ?: cols.first()
                if (name.isNotBlank() && !name.equals("name", ignoreCase = true)) {
                    val item = Item(
                        id = 0,
                        name = name,
                        category = cols.getOrNull(2) ?: "Trading Cards",
                        subCategory = cols.getOrNull(3) ?: "Pokémon TCG",
                        collection = cols.getOrNull(4) ?: "",
                        itemNumber = cols.getOrNull(5) ?: "",
                        rarity = cols.getOrNull(6) ?: "",
                        variant = cols.getOrNull(7) ?: "Normal",
                        condition = cols.getOrNull(8) ?: "Near Mint",
                        purchasePrice = cols.getOrNull(9)?.toDoubleOrNull() ?: 0.0,
                        estimatedValue = cols.getOrNull(10)?.toDoubleOrNull() ?: 0.0,
                        quantity = cols.getOrNull(11)?.toIntOrNull() ?: 1,
                        storageLocation = cols.getOrNull(12) ?: "",
                        notes = cols.getOrNull(13) ?: "",
                        imageUri = cols.getOrNull(14) ?: "",
                        officialImageUrl = cols.getOrNull(14)
                    )
                    list.add(item)
                }
            }
        }
        return list
    }

    fun parseJson(content: String): List<Item> {
        val list = mutableListOf<Item>()
        try {
            val jsonArray = if (content.trim().startsWith("[")) {
                JSONArray(content)
            } else {
                val rootObj = JSONObject(content)
                rootObj.optJSONArray("items") ?: rootObj.optJSONArray("collection") ?: JSONArray()
            }

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val item = Item(
                    id = 0,
                    name = obj.optString("name", "Sem Nome"),
                    category = obj.optString("category", "Trading Cards"),
                    subCategory = obj.optString("subCategory", "Pokémon TCG"),
                    collection = obj.optString("collection", ""),
                    itemNumber = obj.optString("itemNumber", ""),
                    rarity = obj.optString("rarity", ""),
                    variant = obj.optString("variant", "Normal"),
                    condition = obj.optString("condition", "Near Mint"),
                    purchasePrice = obj.optDouble("purchasePrice", 0.0),
                    estimatedValue = obj.optDouble("estimatedValue", 0.0),
                    quantity = obj.optInt("quantity", 1),
                    storageLocation = obj.optString("storageLocation", ""),
                    notes = obj.optString("notes", ""),
                    imageUri = obj.optString("imageUrl", ""),
                    officialImageUrl = obj.optString("imageUrl", null),
                    cardHp = obj.optString("cardHp", null),
                    cardArtist = obj.optString("cardArtist", null),
                    cardAttacks = obj.optString("cardAttacks", null)
                )
                list.add(item)
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun readUriContent(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        return BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
    }

    fun writeToUri(context: Context, uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os).use { writer ->
                writer.write(content)
            }
        }
    }

    private fun escapeCsv(str: String): String {
        return if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
}
