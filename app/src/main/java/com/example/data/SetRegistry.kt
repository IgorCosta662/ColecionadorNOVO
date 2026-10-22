package com.example.data

data class CollectibleSetInfo(
    val id: String,
    val name: String,
    val gameOrBrand: String, // "Pokémon TCG", "Magic: The Gathering", "Hot Wheels"
    val releaseYear: Int,
    val totalCount: Int,
    val logoUrl: String,
    val popularCardsOrModels: List<String>
)

object SetRegistry {
    val sets: List<CollectibleSetInfo> = listOf(
        CollectibleSetInfo(
            id = "sv3pt5",
            name = "Scarlet & Violet 151",
            gameOrBrand = "Pokémon TCG",
            releaseYear = 2023,
            totalCount = 165,
            logoUrl = "https://assets.tcgdex.net/en/sv/sv03.5/logo.webp",
            popularCardsOrModels = listOf(
                "Charizard ex (#199)",
                "Pikachu (#173)",
                "Blastoise ex (#200)",
                "Venusaur ex (#198)",
                "Mew ex (#205)"
            )
        ),
        CollectibleSetInfo(
            id = "mtg_fdn",
            name = "Foundations",
            gameOrBrand = "Magic: The Gathering",
            releaseYear = 2024,
            totalCount = 271,
            logoUrl = "https://svgs.scryfall.io/sets/fdn.svg",
            popularCardsOrModels = listOf(
                "Llanowar Elves",
                "Day of Judgment",
                "Omniscience",
                "Doubling Season",
                "Twinflame Tyrant"
            )
        ),
        CollectibleSetInfo(
            id = "hw_2024",
            name = "Hot Wheels Mainline 2024",
            gameOrBrand = "Hot Wheels",
            releaseYear = 2024,
            totalCount = 250,
            logoUrl = "https://images.unsplash.com/photo-1594787318286-3d835c1d207f?w=600&auto=format&fit=crop",
            popularCardsOrModels = listOf(
                "Datsun 510 Wagon STH",
                "Porsche 911 GT3 RS",
                "Nissan Skyline GT-R R34",
                "Ford Mustang GT",
                "Mazda RX-7"
            )
        ),
        CollectibleSetInfo(
            id = "mtg_dsk",
            name = "Duskmourn: House of Horror",
            gameOrBrand = "Magic: The Gathering",
            releaseYear = 2024,
            totalCount = 276,
            logoUrl = "https://svgs.scryfall.io/sets/dsk.svg",
            popularCardsOrModels = listOf(
                "Valgavoth, Terror Eater",
                "Kaito, Bane of Nightmares",
                "Tyvar, the Pummeler",
                "Overlord of the Hauntwoods"
            )
        )
    )

    fun calculateSetProgress(set: CollectibleSetInfo, items: List<Item>): Pair<Int, Float> {
        val ownedCount = items.filter { item ->
            item.collection.contains(set.name, ignoreCase = true) ||
                    (set.name.contains("151") && (item.collection.contains("151") || item.name.contains("151"))) ||
                    (set.name.contains("Foundations") && item.collection.contains("Foundations", ignoreCase = true)) ||
                    (set.name.contains("Mainline") && item.isDiecast)
        }.distinctBy { it.itemNumber.ifBlank { it.name } }.size

        val progress = if (set.totalCount > 0) (ownedCount.toFloat() / set.totalCount).coerceIn(0f, 1f) else 0f
        return Pair(ownedCount, progress)
    }
}
