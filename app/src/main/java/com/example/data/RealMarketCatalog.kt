package com.example.data

data class RealCatalogEntry(
    val name: String,
    val category: String,
    val subCategory: String,
    val collection: String,
    val itemNumber: String,
    val rarity: String,
    val variant: String,
    val lowPriceBrl: Double,
    val realMarketPriceBrl: Double,
    val highPriceBrl: Double,
    val sourceMarket: String, // "LigaMagic", "LigaPokemon", "MercadoLivre"
    val imageUrl: String,
    val tags: String,
    val notes: String
)

object RealMarketCatalog {

    val allEntries: List<RealCatalogEntry> = listOf(
        // --- LIGAMAGIC: MAGIC: THE GATHERING ---
        RealCatalogEntry(
            name = "Sol Ring",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Commander Masters",
            itemNumber = "404",
            rarity = "Incomum",
            variant = "Normal",
            lowPriceBrl = 4.50,
            realMarketPriceBrl = 8.90,
            highPriceBrl = 15.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/a/a/aa677568-15fe-4f1e-9276-8d1e389d3434.jpg",
            tags = "Staple, Commander, Artefato, Mana",
            notes = "Cotação LigaMagic: Menor R$ 4,50 | Médio R$ 8,90 | Maior R$ 15,00."
        ),
        RealCatalogEntry(
            name = "Counterspell",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Foundations",
            itemNumber = "042",
            rarity = "Incomum",
            variant = "Normal",
            lowPriceBrl = 3.50,
            realMarketPriceBrl = 6.90,
            highPriceBrl = 12.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/4/f/4f0ae978-44d8-4970-8aee-c03d1b8c838b.jpg",
            tags = "Azul, Anulação, Contrafeitiço, Staple",
            notes = "Cotação oficial LigaMagic: Menor R$ 3,50 | Médio R$ 6,90 | Maior R$ 12,00."
        ),
        RealCatalogEntry(
            name = "Lightning Bolt",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Foundations",
            itemNumber = "118",
            rarity = "Incomum",
            variant = "Normal",
            lowPriceBrl = 3.00,
            realMarketPriceBrl = 6.50,
            highPriceBrl = 12.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/7/7/77c6fa74-5543-42ac-9ead-0e890b188e99.jpg",
            tags = "Vermelho, Raio, Dano Direto, Staple",
            notes = "Cotação oficial LigaMagic: Menor R$ 3,00 | Médio R$ 6,50 | Maior R$ 12,00."
        ),
        RealCatalogEntry(
            name = "The One Ring",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "The Lord of the Rings: Tales of Middle-earth",
            itemNumber = "0246",
            rarity = "Mítica Rara",
            variant = "Foil",
            lowPriceBrl = 380.00,
            realMarketPriceBrl = 480.00,
            highPriceBrl = 620.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/d/5/d5806e68-1054-458e-866d-5f4707282549.jpg",
            tags = "O Um Anel, LOTR, Modern, Mítica, Staple",
            notes = "Cotação LigaMagic: Menor R$ 380,00 | Médio R$ 480,00 | Maior R$ 620,00."
        ),
        RealCatalogEntry(
            name = "Sheoldred, the Apocalypse",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Dominaria United",
            itemNumber = "107",
            rarity = "Mítica Rara",
            variant = "Normal",
            lowPriceBrl = 320.00,
            realMarketPriceBrl = 390.00,
            highPriceBrl = 490.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/d/6/d67be074-cdd4-41d9-ac89-0a0456c4e4b2.jpg",
            tags = "Standard, Pioneer, Commander, Mítica",
            notes = "Cotação LigaMagic: Menor R$ 320,00 | Médio R$ 390,00 | Maior R$ 490,00."
        ),
        RealCatalogEntry(
            name = "Black Lotus",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Vintage Masters",
            itemNumber = "001",
            rarity = "Mítica Rara",
            variant = "Normal",
            lowPriceBrl = 65000.00,
            realMarketPriceBrl = 85000.00,
            highPriceBrl = 120000.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/b/d/bd8fa327-dd41-4737-8f19-2cf5eb1f7cdd.jpg",
            tags = "Power Nine, Vintage, Santo Graal, Artefato",
            notes = "Cotação oficial LigaMagic Brasil: Menor R$ 65.000,00 | Médio R$ 85.000,00 | Maior R$ 120.000,00."
        ),
        RealCatalogEntry(
            name = "The Thing, Ben Grimm",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Secret Lair x Marvel",
            itemNumber = "0049",
            rarity = "Incomum",
            variant = "Normal",
            lowPriceBrl = 0.90,
            realMarketPriceBrl = 2.68,
            highPriceBrl = 5.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/d/e/de5da8eb-fb49-41e7-8b02-aa1e7dd1517f.jpg",
            tags = "Marvel, Quarteto Fantástico, Cena Promocional, Ben Grimm",
            notes = "Cotação oficial LigaMagic Brasil: Menor R$ 0,90 | Médio R$ 2,68 | Maior R$ 5,00."
        ),
        RealCatalogEntry(
            name = "The Thing, Ben Grimm (Foil)",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Secret Lair x Marvel",
            itemNumber = "0049",
            rarity = "Incomum",
            variant = "Foil",
            lowPriceBrl = 1.40,
            realMarketPriceBrl = 4.19,
            highPriceBrl = 9.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/d/e/de5da8eb-fb49-41e7-8b02-aa1e7dd1517f.jpg",
            tags = "Marvel, Foil, Quarteto Fantástico",
            notes = "Cotação oficial LigaMagic Brasil (Foil): Menor R$ 1,40 | Médio R$ 4,19 | Maior R$ 9,00."
        ),
        RealCatalogEntry(
            name = "Llanowar Elves",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Foundations",
            itemNumber = "210",
            rarity = "Comum",
            variant = "Normal",
            lowPriceBrl = 2.00,
            realMarketPriceBrl = 4.50,
            highPriceBrl = 10.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/8/b/8bbcfb77-daa1-4ce5-b5f6-48405a722400.jpg",
            tags = "Verde, Mana, Elfo, Staple",
            notes = "Cotação oficial LigaMagic: Menor R$ 2,00 | Médio R$ 4,50 | Maior R$ 10,00."
        ),
        RealCatalogEntry(
            name = "Valgavoth, Terror Eater",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Duskmourn: House of Horror",
            itemNumber = "120",
            rarity = "Mítica Rara",
            variant = "Foil",
            lowPriceBrl = 140.00,
            realMarketPriceBrl = 180.00,
            highPriceBrl = 240.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/7/7/7740e507-e636-4f11-9774-d4f339e65da4.jpg",
            tags = "Duskmourn, Mítica, Demônio, Standard",
            notes = "Cotação oficial LigaMagic: Menor R$ 140,00 | Médio R$ 180,00 | Maior R$ 240,00."
        ),

        // --- LIGAPOKÉMON: POKÉMON TCG ---
        RealCatalogEntry(
            name = "Pikachu (Illustration Rare)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Scarlet & Violet 151",
            itemNumber = "173/165",
            rarity = "Illustration Rare",
            variant = "Holo",
            lowPriceBrl = 65.00,
            realMarketPriceBrl = 89.00,
            highPriceBrl = 130.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/sv/sv03.5/173/high.webp",
            tags = "Pikachu, 151, Illustration Rare, Kanto, Elétrico",
            notes = "Cotação oficial LigaPokémon Brasil: Menor R$ 65,00 | Médio R$ 89,00 | Maior R$ 130,00."
        ),
        RealCatalogEntry(
            name = "Charizard ex (Special Illustration Rare)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Scarlet & Violet 151",
            itemNumber = "199/165",
            rarity = "Special Art Rare (SIR)",
            variant = "Holo",
            lowPriceBrl = 650.00,
            realMarketPriceBrl = 780.00,
            highPriceBrl = 980.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/sv/sv03.5/199/high.webp",
            tags = "Charizard, 151, SIR, Fogo, Santo Graal",
            notes = "Cotação oficial LigaPokémon Brasil: Menor R$ 650,00 | Médio R$ 780,00 | Maior R$ 980,00."
        ),
        RealCatalogEntry(
            name = "Blastoise ex (Special Illustration Rare)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Scarlet & Violet 151",
            itemNumber = "200/165",
            rarity = "Special Art Rare (SIR)",
            variant = "Holo",
            lowPriceBrl = 260.00,
            realMarketPriceBrl = 320.00,
            highPriceBrl = 420.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/sv/sv03.5/200/high.webp",
            tags = "Blastoise, 151, SIR, Água, Kanto",
            notes = "Cotação oficial LigaPokémon Brasil: Menor R$ 260,00 | Médio R$ 320,00 | Maior R$ 420,00."
        ),
        RealCatalogEntry(
            name = "Venusaur ex (Special Illustration Rare)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Scarlet & Violet 151",
            itemNumber = "198/165",
            rarity = "Special Art Rare (SIR)",
            variant = "Holo",
            lowPriceBrl = 210.00,
            realMarketPriceBrl = 270.00,
            highPriceBrl = 350.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/sv/sv03.5/198/high.webp",
            tags = "Venusaur, 151, SIR, Planta, Kanto",
            notes = "Cotação oficial LigaPokémon Brasil: Menor R$ 210,00 | Médio R$ 270,00 | Maior R$ 350,00."
        ),
        RealCatalogEntry(
            name = "Umbreon VMAX (Alternate Art Secret)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Evolving Skies",
            itemNumber = "215/203",
            rarity = "Secret Rare (Moonbreon)",
            variant = "Holo",
            lowPriceBrl = 3800.00,
            realMarketPriceBrl = 4500.00,
            highPriceBrl = 5900.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/swsh/swsh07/215/high.webp",
            tags = "Moonbreon, Umbreon, Eeveelution, Santo Graal",
            notes = "Cotação oficial LigaPokémon Brasil: Menor R$ 3.800,00 | Médio R$ 4.500,00 | Maior R$ 5.900,00."
        ),
        RealCatalogEntry(
            name = "Mew ex (Gold Hyper Rare)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Scarlet & Violet 151",
            itemNumber = "205/165",
            rarity = "Hyper Rare (Gold)",
            variant = "Gold Holo",
            lowPriceBrl = 80.00,
            realMarketPriceBrl = 115.00,
            highPriceBrl = 160.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/sv/sv03.5/205/high.webp",
            tags = "Mew, Dourada, 151, Psíquico",
            notes = "Cotação oficial LigaPokémon Brasil: Menor R$ 80,00 | Médio R$ 115,00 | Maior R$ 160,00."
        ),
        RealCatalogEntry(
            name = "Gengar (Illustration Rare)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Scarlet & Violet 151",
            itemNumber = "094/165",
            rarity = "Rara Holo",
            variant = "Holo",
            lowPriceBrl = 95.00,
            realMarketPriceBrl = 140.00,
            highPriceBrl = 190.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/sv/sv03.5/094/high.webp",
            tags = "Gengar, 151, Fantasma, Psíquico",
            notes = "Cotação oficial LigaPokémon Brasil: Menor R$ 95,00 | Médio R$ 140,00 | Maior R$ 190,00."
        ),
        RealCatalogEntry(
            name = "Mewtwo (Illustration Rare Promo)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Scarlet & Violet 151",
            itemNumber = "SVP-052",
            rarity = "Illustration Rare Promo",
            variant = "Holo",
            lowPriceBrl = 85.00,
            realMarketPriceBrl = 110.00,
            highPriceBrl = 150.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/sv/sv03.5/150/high.webp",
            tags = "Mewtwo, 151, Promo, Psíquico",
            notes = "Cotação oficial LigaPokémon: Menor R$ 85,00 | Médio R$ 110,00 | Maior R$ 150,00."
        ),
        RealCatalogEntry(
            name = "Lugia VSTAR (Gold Secret Rare)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Silver Tempest",
            itemNumber = "211/195",
            rarity = "Secret Rare (Gold)",
            variant = "Gold Holo",
            lowPriceBrl = 140.00,
            realMarketPriceBrl = 190.00,
            highPriceBrl = 260.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/swsh/swsh12/211/high.webp",
            tags = "Lugia, VSTAR, Gold, Incolor, Johto",
            notes = "Cotação oficial LigaPokémon: Menor R$ 140,00 | Médio R$ 190,00 | Maior R$ 260,00."
        ),
        RealCatalogEntry(
            name = "Giratina V (Alternate Art Secret)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Lost Origin",
            itemNumber = "186/196",
            rarity = "Alternate Art Secret",
            variant = "Holo",
            lowPriceBrl = 1500.00,
            realMarketPriceBrl = 1850.00,
            highPriceBrl = 2400.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/swsh/swsh11/186/high.webp",
            tags = "Giratina, Alt Art, Santo Graal, Dragão",
            notes = "Cotação oficial LigaPokémon: Menor R$ 1.500,00 | Médio R$ 1.850,00 | Maior R$ 2.400,00."
        ),
        RealCatalogEntry(
            name = "Rayquaza VMAX (Alternate Art Secret)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Evolving Skies",
            itemNumber = "218/203",
            rarity = "Alternate Art Secret",
            variant = "Holo",
            lowPriceBrl = 1900.00,
            realMarketPriceBrl = 2400.00,
            highPriceBrl = 3100.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/swsh/swsh07/218/high.webp",
            tags = "Rayquaza, Alt Art, Evolving Skies, Dragão",
            notes = "Cotação oficial LigaPokémon: Menor R$ 1.900,00 | Médio R$ 2.400,00 | Maior R$ 3.100,00."
        ),
        RealCatalogEntry(
            name = "Giratina VSTAR (Secret Gold)",
            category = "Trading Cards",
            subCategory = "Pokémon TCG",
            collection = "Crown Zenith",
            itemNumber = "GG69/GG70",
            rarity = "Secret Rare (Gold)",
            variant = "Gold Holo",
            lowPriceBrl = 550.00,
            realMarketPriceBrl = 680.00,
            highPriceBrl = 850.00,
            sourceMarket = "LigaPokemon",
            imageUrl = "https://assets.tcgdex.net/en/swsh/swsh12.5/GG69/high.webp",
            tags = "Giratina, Dourada, Dragão, Crown Zenith",
            notes = "Cotação oficial LigaPokémon: Menor R$ 550,00 | Médio R$ 680,00 | Maior R$ 850,00."
        ),
        RealCatalogEntry(
            name = "Swords to Plowshares",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Foundations",
            itemNumber = "012",
            rarity = "Incomum",
            variant = "Normal",
            lowPriceBrl = 4.00,
            realMarketPriceBrl = 8.50,
            highPriceBrl = 15.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/a/a/aa51458e-0f0e-436f-96a9-8356bb576ff9.jpg",
            tags = "Branco, Remoção, Espadas em Arados, Staple",
            notes = "Cotação oficial LigaMagic: Menor R$ 4,00 | Médio R$ 8,50 | Maior R$ 15,00."
        ),
        RealCatalogEntry(
            name = "Birds of Paradise",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Foundations",
            itemNumber = "215",
            rarity = "Rara",
            variant = "Normal",
            lowPriceBrl = 25.00,
            realMarketPriceBrl = 38.00,
            highPriceBrl = 55.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/3/d/3d69a508-3729-459f-9dd3-39d6796c3d9a.jpg",
            tags = "Verde, Mana dork, Pássaros do Paraíso, Staple",
            notes = "Cotação oficial LigaMagic: Menor R$ 25,00 | Médio R$ 38,00 | Maior R$ 55,00."
        ),
        RealCatalogEntry(
            name = "Rhystic Study",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Commander Masters",
            itemNumber = "110",
            rarity = "Rara",
            variant = "Normal",
            lowPriceBrl = 150.00,
            realMarketPriceBrl = 190.00,
            highPriceBrl = 240.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/d/6/d6914dba-0d27-4055-ac34-b3ebf5802221.jpg",
            tags = "Azul, Encantamento, Compra, Commander, Staple",
            notes = "Cotação oficial LigaMagic: Menor R$ 150,00 | Médio R$ 190,00 | Maior R$ 240,00."
        ),
        RealCatalogEntry(
            name = "Dark Ritual",
            category = "Trading Cards",
            subCategory = "Magic: The Gathering",
            collection = "Foundations",
            itemNumber = "085",
            rarity = "Comum",
            variant = "Normal",
            lowPriceBrl = 2.00,
            realMarketPriceBrl = 4.50,
            highPriceBrl = 8.00,
            sourceMarket = "LigaMagic",
            imageUrl = "https://cards.scryfall.io/large/front/9/5/95f27eeb-6f14-4db3-adb9-9be5ed76b34b.jpg",
            tags = "Preto, Mana, Ritual Sombrio, Staple",
            notes = "Cotação oficial LigaMagic: Menor R$ 2,00 | Médio R$ 4,50 | Maior R$ 8,00."
        ),

        // --- DIECAST: HOT WHEELS & COLECIONÁVEIS ---
        RealCatalogEntry(
            name = "Datsun 510 Wagon Super Treasure Hunt",
            category = "Carrinhos / Diecast",
            subCategory = "Hot Wheels",
            collection = "Mainline STH 2024",
            itemNumber = "STH-04",
            rarity = "Super Treasure Hunt (STH)",
            variant = "Spectraflame",
            lowPriceBrl = 380.00,
            realMarketPriceBrl = 480.00,
            highPriceBrl = 650.00,
            sourceMarket = "MercadoLivre",
            imageUrl = "https://images.unsplash.com/photo-1594787318286-3d835c1d207f?w=600&auto=format&fit=crop",
            tags = "STH, Real Riders, Borracha, Raro, Datsun",
            notes = "Cotação Brasil: Menor R$ 380,00 | Médio R$ 480,00 | Maior R$ 650,00."
        ),
        RealCatalogEntry(
            name = "Nissan Skyline GT-R (BNR34) Premium",
            category = "Carrinhos / Diecast",
            subCategory = "Hot Wheels",
            collection = "Fast & Furious Premium",
            itemNumber = "FF-01",
            rarity = "Premium Metal/Metal",
            variant = "Real Riders",
            lowPriceBrl = 120.00,
            realMarketPriceBrl = 160.00,
            highPriceBrl = 220.00,
            sourceMarket = "MercadoLivre",
            imageUrl = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=600&auto=format&fit=crop",
            tags = "Skyline, R34, Fast and Furious, Paul Walker",
            notes = "Cotação Brasil: Menor R$ 120,00 | Médio R$ 160,00 | Maior R$ 220,00."
        )
    )

    fun findBestMatch(name: String, number: String = "", collection: String = "", preferredSubCategory: String = ""): RealCatalogEntry? {
        val qName = name.trim().lowercase()
        val qNum = number.trim().lowercase()

        // If query is blank or generic placeholder, do NOT match
        if ((qName.isBlank() || qName == "carta desconhecida" || qName == "desconhecida" || qName == "carta") && qNum.isBlank()) {
            return null
        }

        val targetList = if (preferredSubCategory.isNotBlank()) {
            val filtered = allEntries.filter {
                it.subCategory.contains(preferredSubCategory, ignoreCase = true) ||
                        it.category.contains(preferredSubCategory, ignoreCase = true)
            }
            if (filtered.isNotEmpty()) filtered else allEntries
        } else {
            allEntries
        }

        // Exact match by number + name
        if (qNum.isNotBlank()) {
            val numMatch = targetList.firstOrNull { it.itemNumber.lowercase() == qNum }
            if (numMatch != null) return numMatch
        }

        // Exact name match
        if (qName.isNotBlank()) {
            val exactMatch = targetList.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
            if (exactMatch != null) return exactMatch
        }

        // Fuzzy name match within target subcategory only if query has sufficient length
        if (qName.length >= 3) {
            return targetList.firstOrNull { entry ->
                val eName = entry.name.lowercase()
                (qName.contains(eName) || (eName.contains(qName) && qName.length >= 4)) ||
                        (qName.contains("sheoldred") && eName.contains("sheoldred")) ||
                        (qName.contains("sol ring") && eName.contains("sol ring")) ||
                        (qName.contains("one ring") && eName.contains("one ring")) ||
                        (qName.contains("black lotus") && eName.contains("black lotus")) ||
                        (qName.contains("counterspell") && eName.contains("counterspell")) ||
                        (qName.contains("lightning bolt") && eName.contains("lightning bolt")) ||
                        (qName.contains("llanowar") && eName.contains("llanowar")) ||
                        (qName.contains("dark ritual") && eName.contains("dark ritual")) ||
                        (qName.contains("swords to plowshares") && eName.contains("swords to plowshares")) ||
                        (qName.contains("birds of paradise") && eName.contains("birds of paradise")) ||
                        (qName.contains("rhystic study") && eName.contains("rhystic study")) ||
                        (qName.contains("ben grimm") && eName.contains("ben grimm")) ||
                        (qName.contains("pikachu") && eName.contains("pikachu")) ||
                        (qName.contains("charizard") && eName.contains("charizard")) ||
                        (qName.contains("blastoise") && eName.contains("blastoise")) ||
                        (qName.contains("umbreon") && eName.contains("umbreon"))
            }
        }

        return null
    }
}
