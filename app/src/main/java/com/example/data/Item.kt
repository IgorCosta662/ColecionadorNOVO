package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String, // "Trading Cards", "Carrinhos / Diecast", "Action Figures", "Moedas", "Outros"
    val subCategory: String = "", // "Pokémon TCG", "Magic: The Gathering", "Hot Wheels", etc.
    val collection: String = "", // e.g. "Scarlet & Violet 151", "Foundations", "Mainline 2024"
    val itemNumber: String = "", // e.g. "173/165"
    val edition: String = "",
    val rarity: String = "", // "Illustration Rare", "Mythic Rare", "Super Treasure Hunt"
    val variant: String = "Normal", // "Foil", "Holo", "Reverse Holo", "Normal"
    val condition: String = "Near Mint",
    val conditionScore: Int = 90,
    val language: String = "PT-BR",
    val purchasePrice: Double = 0.0,
    val estimatedValue: Double = 0.0,
    val quantity: Int = 1,
    val storageLocation: String = "", // "Pasta 151 - Pág 1", "Slab Acrílico", "Gaveta #1"
    val notes: String = "",
    val tags: String = "",
    val imageUri: String = "",
    val backImageUri: String? = null,
    val detailImagesJson: String? = null,
    val isFavorite: Boolean = false,
    val isGraded: Boolean = false,
    val gradingCompany: String? = null,
    val gradeScore: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val cardHp: String? = null,
    val cardArtist: String? = null,
    val cardAttacks: String? = null,
    val cardOracleText: String? = null,
    val cardTranslatedEffect: String? = null,
    val officialImageUrl: String? = null,
    val authenticityStatus: String = "Baixo risco aparente",
    val authenticityNotes: String = "",
    val isAlertEnabled: Boolean = false,
    val targetPriceAlert: Double? = null,
    val priceOffersJson: String? = null,
    val priceHistoryJson: String? = null,
    val energyType: String? = null, // e.g. "Fogo", "Água", "Elétrico / Raios", "Planta", "Psíquico", "Incolor"
    val releaseYear: Int? = null // e.g. 2024, 2023
) {
    val totalPurchasePrice: Double
        get() = purchasePrice * quantity

    val totalEstimatedValue: Double
        get() = estimatedValue * quantity

    val profit: Double
        get() = totalEstimatedValue - totalPurchasePrice

    val profitPercentage: Double
        get() = if (purchasePrice > 0) ((estimatedValue - purchasePrice) / purchasePrice) * 100.0 else 0.0

    val isMagicCard: Boolean
        get() = subCategory.contains("Magic", ignoreCase = true) ||
                tags.contains("Magic", ignoreCase = true) ||
                tags.contains("Commander", ignoreCase = true) ||
                tags.contains("Modern", ignoreCase = true) ||
                collection.contains("Dominaria", ignoreCase = true) ||
                collection.contains("Commander", ignoreCase = true) ||
                collection.contains("Secret Lair", ignoreCase = true) ||
                collection.contains("Tales of Middle-earth", ignoreCase = true) ||
                collection.contains("Foundations", ignoreCase = true) ||
                collection.contains("Duskmourn", ignoreCase = true)

    val isPokemonCard: Boolean
        get() = subCategory.contains("Pokémon", ignoreCase = true) ||
                subCategory.contains("Pokemon", ignoreCase = true) ||
                tags.contains("Pokémon", ignoreCase = true) ||
                tags.contains("Pokemon", ignoreCase = true) ||
                collection.contains("151", ignoreCase = true) ||
                collection.contains("Evolving Skies", ignoreCase = true) ||
                collection.contains("Scarlet", ignoreCase = true)

    val resolvedMagicColor: String
        get() {
            val combined = "$energyType $name $tags $collection ${cardOracleText ?: ""} ${cardTranslatedEffect ?: ""} $notes".lowercase()
            return when {
                combined.contains("multicolor") || combined.contains("ouro") || combined.contains("gold") -> "Multicolorido"
                combined.contains("branco") || combined.contains("white") || combined.contains("planície") || combined.contains("plains") -> "Branco"
                combined.contains("azul") || combined.contains("blue") || combined.contains("ilha") || combined.contains("island") || combined.contains("counterspell") -> "Azul"
                combined.contains("preto") || combined.contains("black") || combined.contains("pântano") || combined.contains("swamp") || combined.contains("sheoldred") -> "Preto"
                combined.contains("vermelho") || combined.contains("red") || combined.contains("montanha") || combined.contains("mountain") || combined.contains("lightning bolt") || combined.contains("the thing") -> "Vermelho"
                combined.contains("verde") || combined.contains("green") || combined.contains("floresta") || combined.contains("forest") -> "Verde"
                combined.contains("incolor") || combined.contains("colorless") || combined.contains("artefato") || combined.contains("sol ring") || combined.contains("one ring") || combined.contains("black lotus") -> "Incolor"
                else -> ""
            }
        }

    val resolvedCardType: String
        get() {
            val combined = "$energyType $name $tags $collection ${cardAttacks ?: ""} ${cardOracleText ?: ""} ${cardTranslatedEffect ?: ""} $notes".lowercase()
            return when {
                // Magic card types
                combined.contains("criatura") || combined.contains("creature") || combined.contains("sheoldred") || combined.contains("ben grimm") || combined.contains("the thing") -> "Criatura"
                combined.contains("mágica instantânea") || combined.contains("instantânea") || combined.contains("instant") || combined.contains("counterspell") || combined.contains("lightning bolt") -> "Mágica Instantânea"
                combined.contains("feitiço") || combined.contains("sorcery") -> "Feitiço"
                combined.contains("encantamento") || combined.contains("enchantment") -> "Encantamento"
                combined.contains("artefato") || combined.contains("artifact") || combined.contains("sol ring") || combined.contains("one ring") || combined.contains("black lotus") -> "Artefato"
                combined.contains("planeswalker") -> "Planeswalker"
                combined.contains("terreno") || combined.contains("land") -> "Terreno"
                combined.contains("batalha") || combined.contains("battle") -> "Batalha"

                // Pokémon card types
                combined.contains("vmax") -> "Pokémon VMAX"
                combined.contains("vstar") -> "Pokémon VSTAR"
                combined.contains("ex ") || combined.contains(" ex") -> "Pokémon ex"
                combined.contains("treinador") || combined.contains("trainer") || combined.contains("apoiador") -> "Treinador"
                else -> ""
            }
        }

    val resolvedEnergyType: String
        get() {
            if (!energyType.isNullOrBlank()) return energyType
            val combined = "$name $tags $collection ${cardAttacks ?: ""} ${cardTranslatedEffect ?: ""} $notes".lowercase()
            return when {
                // If it's Magic, prefer Magic color or type
                isMagicCard -> {
                    val mColor = resolvedMagicColor
                    if (mColor.isNotEmpty()) mColor
                    else {
                        val mType = resolvedCardType
                        if (mType.isNotEmpty()) mType else "Incolor"
                    }
                }
                // Pokémon types
                combined.contains("fogo") || combined.contains("fire") || combined.contains("charizard") -> "Fogo"
                combined.contains("água") || combined.contains("water") || combined.contains("blastoise") -> "Água"
                combined.contains("planta") || combined.contains("grass") || combined.contains("venusaur") -> "Planta"
                combined.contains("elétrico") || combined.contains("raio") || combined.contains("lightning") || combined.contains("pikachu") -> "Elétrico / Raios"
                combined.contains("psíquic") || combined.contains("psychic") || combined.contains("mew") -> "Psíquico"
                combined.contains("escuridão") || combined.contains("dark") || combined.contains("umbreon") -> "Escuridão"
                combined.contains("luta") || combined.contains("fighting") -> "Luta"
                combined.contains("metal") || combined.contains("steel") || combined.contains("aço") -> "Metal"
                combined.contains("dragão") || combined.contains("dragon") -> "Dragão"
                combined.contains("incolor") || combined.contains("colorless") || combined.contains("sol ring") || combined.contains("one ring") -> "Incolor"
                else -> ""
            }
        }

    val typeEmoji: String
        get() {
            val t = resolvedEnergyType.ifEmpty { resolvedCardType }
            return when {
                // Magic colors
                t.contains("Preto", ignoreCase = true) -> "💀"
                t.contains("Azul", ignoreCase = true) -> "💧"
                t.contains("Vermelho", ignoreCase = true) -> "🔥"
                t.contains("Branco", ignoreCase = true) -> "☀️"
                t.contains("Verde", ignoreCase = true) -> "🌳"
                t.contains("Multicolor", ignoreCase = true) -> "🌟"
                t.contains("Criatura", ignoreCase = true) -> "🛡️"
                t.contains("Instantânea", ignoreCase = true) -> "⚡"
                t.contains("Feitiço", ignoreCase = true) -> "📜"
                t.contains("Encantamento", ignoreCase = true) -> "✨"
                t.contains("Artefato", ignoreCase = true) -> "⚙️"
                t.contains("Planeswalker", ignoreCase = true) -> "👑"
                t.contains("Terreno", ignoreCase = true) -> "🏔️"
                t.contains("Batalha", ignoreCase = true) -> "⚔️"

                // Pokémon types
                t.contains("Fogo", ignoreCase = true) -> "🔥"
                t.contains("Água", ignoreCase = true) -> "💧"
                t.contains("Planta", ignoreCase = true) -> "🌿"
                t.contains("Elétrico", ignoreCase = true) || t.contains("Raios", ignoreCase = true) -> "⚡"
                t.contains("Psíquico", ignoreCase = true) -> "🔮"
                t.contains("Luta", ignoreCase = true) -> "🥊"
                t.contains("Escuridão", ignoreCase = true) -> "🌑"
                t.contains("Metal", ignoreCase = true) -> "🛡️"
                t.contains("Dragão", ignoreCase = true) -> "🐉"
                t.contains("Incolor", ignoreCase = true) -> "⚙️"
                else -> "⭐"
            }
        }

    val resolvedReleaseYear: Int?
        get() {
            if (releaseYear != null && releaseYear > 0) return releaseYear
            val combined = "$collection $notes $tags $edition"
            val yearRegex = Regex("\\b(19\\d\\d|20\\d\\d)\\b")
            val match = yearRegex.find(combined)
            if (match != null) return match.value.toIntOrNull()
            if (collection.contains("151", ignoreCase = true)) return 2023
            if (collection.contains("Evolving Skies", ignoreCase = true)) return 2021
            if (collection.contains("Dominaria", ignoreCase = true)) return 2022
            if (collection.contains("Marvel", ignoreCase = true)) return 2024
            if (collection.contains("Foundations", ignoreCase = true)) return 2024
            return null
        }

    val isCard: Boolean
        get() = category.contains("Card", ignoreCase = true) ||
                subCategory.contains("Pokémon", ignoreCase = true) ||
                subCategory.contains("Magic", ignoreCase = true) ||
                subCategory.contains("Yu-Gi-Oh", ignoreCase = true)

    val isDiecast: Boolean
        get() = category.contains("Diecast", ignoreCase = true) ||
                category.contains("Carrinho", ignoreCase = true) ||
                subCategory.contains("Hot Wheels", ignoreCase = true)
}
