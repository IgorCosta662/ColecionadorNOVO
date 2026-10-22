package com.example.data

data class PreconCardTemplate(
    val name: String,
    val quantity: Int = 1,
    val cardType: String = "Mágica / Pokémon",
    val estimatedPriceBrl: Double = 5.0,
    val oracleText: String = "",
    val effectPt: String = "",
    val manaCostOrStats: String = "",
    val imageUrl: String = "",
    val roleInDeck: String = ""
) {
    fun resolveEffectiveImage(gameHint: String = "Magic: The Gathering"): String {
        return imageUrl.ifBlank { CardImageResolver.resolveImageUrl(name, gameHint) }
    }
}

data class PreconDeckTemplate(
    val id: String,
    val name: String,
    val game: String,
    val format: String,
    val commanderOrLead: String,
    val themeOrColors: String,
    val releaseYear: Int,
    val estimatedTotalBrl: Double,
    val description: String,
    val cardList: List<PreconCardTemplate>,
    val productCode: String? = null,
    val aliases: List<String> = emptyList(),
    val strategyGuide: String = "",
    val winCondition: String = "",
    val keyCombos: List<String> = emptyList(),
    val isMultiDeckBox: Boolean = false,
    val boxDecks: List<PreconDeckTemplate> = emptyList(),
    val coverImageUrl: String = ""
)

object DecklistParser {
    /**
     * Parses standard MTG Arena, Moxfield, LigaMagic, or Pokémon TCG Live decklists.
     * Examples of supported line formats:
     * - "1 Sol Ring"
     * - "1x Arcane Signet"
     * - "4 Lightning Bolt (DMU) 141"
     * - "3 Charizard ex OBF 125"
     * - "Command Tower"
     */
    fun parseDecklistText(rawText: String, defaultGame: String = "Magic: The Gathering"): List<PreconCardTemplate> {
        val result = mutableListOf<PreconCardTemplate>()
        val lines = rawText.lines()

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue
            // Skip comments and headers
            if (line.startsWith("//") || line.startsWith("#") || line.startsWith("Deck") || line.startsWith("Sideboard") || line.startsWith("Commander")) {
                continue
            }

            // Regex matches: optional quantity (e.g., "1", "1x", "4 "), followed by card name, and optional set/number (e.g., "(DMU) 123" or "OBF 125")
            val regex = Regex("""^(\d+)x?\s+(.+)$""")
            val match = regex.find(line)

            val (qty, rawName) = if (match != null) {
                val q = match.groupValues[1].toIntOrNull() ?: 1
                val n = match.groupValues[2].trim()
                Pair(q, n)
            } else {
                Pair(1, line)
            }

            // Clean up trailing set indicators like (MOM) 123 or [FDN] or *F* or collector numbers
            var cleanName = rawName
                .replace(Regex("""\([A-Za-z0-9_]+\)\s*\d*"""), "")
                .replace(Regex("""\[[A-Za-z0-9_]+\]\s*\d*"""), "")
                .replace(Regex("""\s+[A-Za-z]{2,4}\s+\d+$"""), "")
                .replace(Regex("""\s+\*.*\*$"""), "")
                .trim()

            if (cleanName.isBlank()) continue

            // Determine card type based on name and game
            val cardType = guessCardType(cleanName, defaultGame)
            val approxPrice = guessEstimatedPrice(cleanName, defaultGame)

            result.add(
                PreconCardTemplate(
                    name = cleanName,
                    quantity = qty,
                    cardType = cardType,
                    estimatedPriceBrl = approxPrice
                )
            )
        }

        return result
    }

    private fun guessCardType(name: String, game: String): String {
        val lower = name.lowercase()
        return when {
            game.contains("One Piece", ignoreCase = true) -> when {
                lower.contains("luffy") || lower.contains("leader") || lower.contains("líder") || lower.contains("law") || lower.contains("kid") || lower.contains("ace") -> "Líder / Personagem"
                lower.contains("pistol") || lower.contains("event") || lower.contains("evento") || lower.contains("red roc") || lower.contains("radical") -> "Card de Evento"
                lower.contains("stage") || lower.contains("estágio") || lower.contains("onigashima") || lower.contains("sunny") -> "Card de Estágio"
                lower.contains("don") -> "DON!!"
                else -> "Personagem"
            }
            game.contains("Yu-Gi-Oh", ignoreCase = true) || game.contains("Yugioh", ignoreCase = true) -> when {
                lower.contains("fusion") || lower.contains("fusão") || lower.contains("mirrorjade") || lower.contains("lubellion") -> "Monstro de Fusão"
                lower.contains("synchro") || lower.contains("sincro") || lower.contains("archfiend") -> "Monstro Sincro"
                lower.contains("xyz") || lower.contains("link") -> "Monstro do Deck Adicional"
                lower.contains("spell") || lower.contains("magia") || lower.contains("branded fusion") || lower.contains("sanctuary") || lower.contains("island") -> "Card de Magia"
                lower.contains("trap") || lower.contains("armadilha") || lower.contains("impermanence") || lower.contains("judgment") -> "Card de Armadilha"
                else -> "Monstro de Efeito"
            }
            game.contains("Magic", ignoreCase = true) -> when {
                lower in listOf("mountain", "island", "swamp", "plains", "forest", "montanha", "ilha", "pântano", "planície", "floresta", "command tower", "torre de comando") -> "Terreno"
                lower.contains("terreno") || lower.contains("land") -> "Terreno"
                lower.contains("sol ring") || lower.contains("signet") || lower.contains("talisman") || lower.contains("boots") || lower.contains("greaves") || lower.contains("artefato") || lower.contains("sword") || lower.contains("masamune") -> "Artefato"
                lower.contains("counterspell") || lower.contains("bolt") || lower.contains("path") || lower.contains("swords") || lower.contains("instant") || lower.contains("instantânea") -> "Mágica Instantânea"
                lower.contains("cultivate") || lower.contains("wrath") || lower.contains("feitiço") || lower.contains("sorcery") || lower.contains("meteor") -> "Feitiço"
                else -> "Criatura / Mágica"
            }
            else -> when {
                lower.contains("energy") || lower.contains("energia") -> "Energia"
                lower.contains("bola") || lower.contains("ball") || lower.contains("caramelo") || lower.contains("candy") || lower.contains("item") -> "Item / Treinador"
                lower.contains("chefia") || lower.contains("professor") || lower.contains("boss") || lower.contains("apoiador") || lower.contains("supporter") -> "Apoiador"
                lower.contains("estádio") || lower.contains("stadium") -> "Estádio"
                else -> "Pokémon"
            }
        }
    }

    private fun guessEstimatedPrice(name: String, game: String): Double {
        val lower = name.lowercase()
        return when {
            // One Piece
            lower.contains("luffy") -> 30.00
            lower.contains("zoro") -> 25.00
            lower.contains("nami") -> 22.00
            lower.contains("law") -> 28.00
            lower.contains("kid") -> 24.00
            lower.contains("ace") -> 35.00
            lower.contains("sabo") -> 26.00
            // Yu-Gi-Oh!
            lower.contains("albaz") -> 18.00
            lower.contains("mirrorjade") -> 28.00
            lower.contains("branded fusion") -> 15.00
            lower.contains("ash blossom") -> 25.00
            lower.contains("garunix") -> 22.00
            lower.contains("ponix") -> 16.00
            lower.contains("grapha") -> 15.00
            lower.contains("archfiend") -> 14.00
            // Magic / Final Fantasy
            lower.contains("cloud") -> 35.00
            lower.contains("sephiroth") -> 40.00
            lower.contains("terra") -> 32.00
            lower.contains("tidus") || lower.contains("yuna") -> 28.00
            lower.contains("y'shtola") -> 30.00
            lower.contains("masamune") -> 18.00
            lower.contains("buster sword") -> 22.00
            lower.contains("ultimecia") -> 15.00
            lower.contains("jenova") -> 16.00
            lower.contains("sol ring") -> 8.50
            lower.contains("arcane signet") -> 4.00
            lower.contains("command tower") -> 2.50
            // Pokemon
            lower.contains("charizard ex") -> 85.00
            lower.contains("miraidon ex") -> 25.00
            lower.contains("gardevoir ex") -> 30.00
            lower.contains("pantlaza") -> 45.00
            lower.contains("chatterfang") -> 35.00
            lower.contains("bello") -> 30.00
            lower.contains("ulalek") -> 55.00
            lower.contains("mountain") || lower.contains("island") || lower.contains("swamp") || lower.contains("plains") || lower.contains("forest") -> 0.50
            lower.contains("energia") || lower.contains("energy") -> 0.50
            else -> 4.00
        }
    }
}

object PreconstructedDecksRegistry {
    private val ffCloudCards: List<PreconCardTemplate> = listOf(
        PreconCardTemplate("Cloud, Planet's Champion", 1, "Criatura Lendária / Herói", 38.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/0/01/Fin-482-cloud-planet-s-champion.png/revision/latest"),
        PreconCardTemplate("Barret Wallace", 1, "Criatura Lendária", 12.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/e/e8/Barret-VIIAC-2015.jpg/revision/latest"),
        PreconCardTemplate("Beatrix, Loyal General", 1, "Criatura Lendária", 10.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/f/ff/Beatrix_concept.jpg/revision/latest"),
        PreconCardTemplate("Adelbert Steiner", 2, "Criatura", 5.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/d/d4/Adelbert_Steiner_character.png/revision/latest"),
        PreconCardTemplate("Cloudbound Moogle", 4, "Criatura", 3.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/5/5d/Moogle-ffvii-render.png/revision/latest"),
        PreconCardTemplate("Tifa Lockhart", 1, "Criatura Lendária", 18.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/a/ad/Tifa-TradingCard.jpg/revision/latest"),
        PreconCardTemplate("Red XIII", 1, "Criatura Lendária", 12.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/4/47/Red_XIII_Remake_Render.png/revision/latest"),
        PreconCardTemplate("Buster Sword", 1, "Artefato - Equipamento", 22.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/a/a9/Buster_Sword_Remake_Render.png/revision/latest"),
        PreconCardTemplate("Samurai's Katana", 2, "Artefato - Equipamento", 6.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/e/e4/Mutsunokami-ffvii.png/revision/latest"),
        PreconCardTemplate("Warrior's Sword", 3, "Artefato - Equipamento", 4.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/6/61/Hardedge-ffvii.png/revision/latest"),
        PreconCardTemplate("Fate of the Sun-Cryst", 3, "Mágica Instantânea", 4.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/f/f3/Sun-cryst.jpg/revision/latest"),
        PreconCardTemplate("Rabanastre, Royal City", 1, "Terreno Lendário", 12.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/c/c5/Rabanastre-art.jpg/revision/latest"),
        PreconCardTemplate("Lightning Bolt", 3, "Mágica Instantânea", 4.0, imageUrl = "https://cards.scryfall.io/large/front/f/2/f29ba16f-c8fb-42fe-aabf-87089cb214a7.jpg"),
        PreconCardTemplate("Swords to Plowshares", 2, "Mágica Instantânea", 6.0, imageUrl = "https://cards.scryfall.io/large/front/7/c/7c85d424-8088-424f-9e49-920d993246dd.jpg"),
        PreconCardTemplate("Path to Exile", 1, "Mágica Instantânea", 6.0, imageUrl = "https://cards.scryfall.io/large/front/a/7/a7aed564-2d2d-42c4-bf11-812bc1a0284c.jpg"),
        PreconCardTemplate("Boros Signet", 2, "Artefato", 2.5, imageUrl = "https://cards.scryfall.io/large/front/a/2/a2be6024-18d6-4444-b20b-0361376e18f2.jpg"),
        PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0, imageUrl = "https://cards.scryfall.io/large/front/0/4/043ed4a3-7186-4f96-be60-49658d343460.jpg"),
        PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5, imageUrl = "https://cards.scryfall.io/large/front/a/a/aa677568-15fe-4f1e-9276-8d1e389d3434.jpg"),
        PreconCardTemplate("Wind-Scarred Crag", 4, "Terreno", 1.0, imageUrl = "https://cards.scryfall.io/large/front/f/5/f5394be5-d3e9-4e5b-b9f0-c5188812c6a0.jpg"),
        PreconCardTemplate("Boros Garrison", 2, "Terreno", 1.5, imageUrl = "https://cards.scryfall.io/large/front/0/6/067204eb-cfc2-48a3-a7a7-33f789ad87a1.jpg"),
        PreconCardTemplate("Command Tower", 1, "Terreno", 2.5, imageUrl = "https://cards.scryfall.io/large/front/9/5/95f27eeb-6f14-4db3-adb9-9be5ed76b34b.jpg"),
        PreconCardTemplate("Mountain", 10, "Terreno Básico", 0.5, imageUrl = "https://cards.scryfall.io/large/front/8/b/8bbcfb77-daa1-4ce5-b5f6-48405a722400.jpg"),
        PreconCardTemplate("Plains", 7, "Terreno Básico", 0.5, imageUrl = "https://cards.scryfall.io/large/front/d/e/de5da8eb-fb49-41e7-8b02-aa1e7dd1517f.jpg")
    )

    private val ffSephirothCards: List<PreconCardTemplate> = listOf(
        PreconCardTemplate("Sephiroth, Planet's Heir", 1, "Criatura Lendária / Vilão", 42.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/b/b1/Fin-115a-sephiroth-fabled-soldier.png/revision/latest"),
        PreconCardTemplate("Ultimecia, Temporal Threat", 1, "Criatura Lendária", 15.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/b/bc/Ultimecia.png/revision/latest"),
        PreconCardTemplate("Jenova, Ancient Calamity", 1, "Criatura Lendária", 16.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/4/4e/Jenova-ffvii-head.png/revision/latest"),
        PreconCardTemplate("Vincent Valentine", 1, "Criatura Lendária", 14.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/f/f6/Vincent-ccvii-art.png/revision/latest"),
        PreconCardTemplate("Black Mage of the Black Waltz", 4, "Criatura", 3.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/6/69/Black_Mage_FFIX_Art.png/revision/latest"),
        PreconCardTemplate("Shadowy Apparition", 3, "Criatura", 2.5, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/9/90/Shadow-ffvi-art.png/revision/latest"),
        PreconCardTemplate("Masamune", 2, "Artefato - Equipamento", 18.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/0/0b/Masamune-ffvii-art.png/revision/latest"),
        PreconCardTemplate("Dark Knight's Blade", 3, "Artefato - Equipamento", 4.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/b/ba/Murasame-ffvii.png/revision/latest"),
        PreconCardTemplate("Meteor", 1, "Feitiço Lendário", 15.0, imageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/f/f0/Black_Materia_Art_FFVII.png/revision/latest"),
        PreconCardTemplate("Counterspell", 3, "Mágica Instantânea", 5.0, imageUrl = "https://cards.scryfall.io/large/front/4/f/4f0ae978-44d8-4970-8aee-c03d1b8c838b.jpg"),
        PreconCardTemplate("Dismember", 2, "Mágica Instantânea", 8.0, imageUrl = "https://cards.scryfall.io/large/front/3/d/3d69a508-3729-459f-9dd3-39d6796c3d9a.jpg"),
        PreconCardTemplate("Doom Blade", 2, "Mágica Instantânea", 2.5, imageUrl = "https://cards.scryfall.io/large/front/7/7/7740e507-e636-4f11-9774-d4f339e65da4.jpg"),
        PreconCardTemplate("Dark Ritual", 2, "Mágica Instantânea", 4.0, imageUrl = "https://cards.scryfall.io/large/front/a/a/aa51458e-0f0e-436f-96a9-8356bb576ff9.jpg"),
        PreconCardTemplate("Dimir Signet", 2, "Artefato", 2.5, imageUrl = "https://cards.scryfall.io/large/front/3/4/348259e8-4665-4f3f-9176-0bf3d2010b93.jpg"),
        PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0, imageUrl = "https://cards.scryfall.io/large/front/0/4/043ed4a3-7186-4f96-be60-49658d343460.jpg"),
        PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5, imageUrl = "https://cards.scryfall.io/large/front/a/a/aa677568-15fe-4f1e-9276-8d1e389d3434.jpg"),
        PreconCardTemplate("Dismal Backwater", 4, "Terreno", 1.0, imageUrl = "https://cards.scryfall.io/large/front/7/8/78643ec1-1975-4700-be87-57ee68579d46.jpg"),
        PreconCardTemplate("Dimir Aqueduct", 2, "Terreno", 1.5, imageUrl = "https://cards.scryfall.io/large/front/d/a/da5b2fa0-6811-4775-926f-47a3e742e97a.jpg"),
        PreconCardTemplate("Command Tower", 1, "Terreno", 2.5, imageUrl = "https://cards.scryfall.io/large/front/9/5/95f27eeb-6f14-4db3-adb9-9be5ed76b34b.jpg"),
        PreconCardTemplate("Swamp", 10, "Terreno Básico", 0.5, imageUrl = "https://cards.scryfall.io/large/front/d/6/d67be074-cdd4-41d9-ac89-0a0456c4e4b2.jpg"),
        PreconCardTemplate("Island", 8, "Terreno Básico", 0.5, imageUrl = "https://cards.scryfall.io/large/front/7/7/77c6fa74-5543-42ac-9ead-0e890b188e99.jpg")
    )

    // Individual FF Deck 1: Cloud Strife
    val preconDeckCloud = PreconDeckTemplate(
        id = "mtg_ff_starter_cloud",
        name = "Final Fantasy Starter Kit: Deck do Cloud (D3854000)",
        game = "Magic: The Gathering",
        format = "Starter Kit (60 Cartas)",
        commanderOrLead = "Cloud, Planet's Champion",
        themeOrColors = "Boros (Vermelho / Branco) - Equipamentos e Heróis",
        releaseYear = 2025,
        estimatedTotalBrl = 150.0,
        coverImageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/0/01/Fin-482-cloud-planet-s-champion.png/revision/latest",
        description = "Baralho oficial de 60 cartas de Cloud, Planet's Champion que integra o Starter Kit D3854000 da Wizards of the Coast. Focado em equipamentos de combate rápido, aliados de Final Fantasy VII e IX, e remoções eficientes.",
        productCode = "D3854000",
        aliases = listOf("D3854000", "Cloud", "Final Fantasy VII", "Boros", "Wizards of the Coast", "Starter Kit"),
        strategyGuide = "Equipe Cloud rapidamente com Buster Sword e Katana para ativar habilidades de combate letal. Use Swords to Plowshares e Lightning Bolt para limpar o caminho.",
        winCondition = "Ataques maciços de Cloud equipado e acelerado por Tifa e Beatrix.",
        keyCombos = listOf(
            "Cloud + Buster Sword: Poder colossal e iniciativa",
            "Beatrix + Guerreiros: Bônus global +1/+1 e ganho de vida contínuo"
        ),
        cardList = ffCloudCards
    )

    // Individual FF Deck 2: Sephiroth
    val preconDeckSephiroth = PreconDeckTemplate(
        id = "mtg_ff_starter_sephiroth",
        name = "Final Fantasy Starter Kit: Deck do Sephiroth (D3854000)",
        game = "Magic: The Gathering",
        format = "Starter Kit (60 Cartas)",
        commanderOrLead = "Sephiroth, Planet's Heir",
        themeOrColors = "Dimir (Azul / Preto) - Controle e Calamidade",
        releaseYear = 2025,
        estimatedTotalBrl = 145.0,
        coverImageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/b/b1/Fin-115a-sephiroth-fabled-soldier.png/revision/latest",
        description = "Baralho oficial de 60 cartas de Sephiroth, Planet's Heir que integra o Starter Kit D3854000 da Wizards of the Coast. Focado em anulações, manipulação de cemitério, a lâmina Masamune e destruição com Meteoro.",
        productCode = "D3854000",
        aliases = listOf("D3854000", "Sephiroth", "Final Fantasy VII", "Dimir", "Wizards of the Coast", "Starter Kit"),
        strategyGuide = "Controle o jogo nos primeiros turnos com anulações e remoções baratas. Traga Sephiroth e equipe a Masamune para eliminar ameaças inimigas e liquide a partida com Meteoro.",
        winCondition = "Meteoro limpando o campo adversário e ataque mortal de Sephiroth com Masamune.",
        keyCombos = listOf(
            "Sephiroth + Masamune: Toque mortífero e quebra de indestrutível",
            "Meteoro + Sephiroth: Limpeza assimétrica inquestionável"
        ),
        cardList = ffSephirothCards
    )

    val allPrecons: List<PreconDeckTemplate> = listOf(
        // FF 1: PRODUTO COMPLETO STARTER KIT D3854000 (COM OPÇÃO DE IMPORTAR OS 2 DECKS SEPARADOS)
        PreconDeckTemplate(
            id = "mtg_ff_starter_kit_d3854000",
            name = "Final Fantasy Starter Kit (Kit com 2 Decks - D3854000)",
            game = "Magic: The Gathering",
            format = "Starter Kit (2 Decks Independentes de 60 Cartas)",
            commanderOrLead = "Cloud & Sephiroth (2 Baralhos)",
            themeOrColors = "Multicolorido • Boros (Cloud) + Dimir (Sephiroth)",
            releaseYear = 2025,
            estimatedTotalBrl = 295.0,
            coverImageUrl = "https://static.wikia.nocookie.net/finalfantasy/images/0/01/Fin-482-cloud-planet-s-champion.png/revision/latest",
            description = "Kit Oficial Wizards of the Coast (Código D3854000). A caixa original contém 2 baralhos completos e separados de 60 cartas prontos para jogar um contra o outro: Deck do Cloud (Vermelho/Branco) e Deck do Sephiroth (Azul/Preto). Ao adicionar, o app cria os DOIS baralhos de forma independente na sua coleção com fotos de cada carta!",
            productCode = "D3854000",
            aliases = listOf("D3854000", "Wizards Of The Coast D3854000", "Final Fantasy", "Kit Inicial", "Multicolorido", "Cloud", "Sephiroth", "Universes Beyond"),
            cardList = ffCloudCards + ffSephirothCards,
            isMultiDeckBox = true,
            boxDecks = listOf(preconDeckCloud, preconDeckSephiroth),
            strategyGuide = "O Kit vem com 2 decks rivais prontos para combate imediato: Cloud lidera o ataque agressivo com Equipamentos e Aliados, enquanto Sephiroth controla o jogo com Anulações, Remoções e o apocalíptico Meteoro.",
            winCondition = "Deck Cloud vence pela agressão veloz; Deck Sephiroth vence pelo controle e Meteoro.",
            keyCombos = listOf(
                "Deck 1 (Cloud): Cloud + Buster Sword (Iniciativa & Poder Máximo)",
                "Deck 2 (Sephiroth): Sephiroth + Masamune + Meteoro (Limpeza de campo irrecuperável)"
            )
        ),

        // FF 2: Deck do Cloud (60 Cartas)
        preconDeckCloud,

        // FF 3: Deck do Sephiroth (60 Cartas)
        preconDeckSephiroth,

        // FF 4: Commander Limit Break (Final Fantasy VII)
        PreconDeckTemplate(
            id = "mtg_ff_commander_limit_break",
            name = "Final Fantasy Commander: Limit Break (Final Fantasy VII)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Cloud Strife",
            themeOrColors = "Multicolorido (Jeskai - Limit Break & Equipamentos)",
            releaseYear = 2025,
            estimatedTotalBrl = 450.0,
            description = "Deck pré-construído oficial de 100 cartas de Commander de Final Fantasy VII com Cloud Strife, Tifa, Aerith, Barret, Buster Sword e a invocação lendária Knights of the Round.",
            productCode = "D3854000-CMD-1",
            aliases = listOf("Final Fantasy VII", "Limit Break", "Cloud Strife", "Tifa", "Aerith", "Multicolorido", "Commander"),
            cardList = listOf(
                PreconCardTemplate("Cloud Strife", 1, "Comandante / Criatura", 45.0),
                PreconCardTemplate("Tifa Lockhart", 1, "Criatura Lendária", 20.0),
                PreconCardTemplate("Aerith Gainsborough", 1, "Criatura Lendária", 18.0),
                PreconCardTemplate("Barret Wallace", 1, "Criatura Lendária", 12.0),
                PreconCardTemplate("Red XIII", 1, "Criatura Lendária", 10.0),
                PreconCardTemplate("Yuffie Kisaragi", 1, "Criatura Lendária", 12.0),
                PreconCardTemplate("Vincent Valentine", 1, "Criatura Lendária", 14.0),
                PreconCardTemplate("Cid Highwind", 1, "Criatura Lendária", 8.0),
                PreconCardTemplate("Zack Fair", 1, "Criatura Lendária", 16.0),
                PreconCardTemplate("Buster Sword", 1, "Artefato - Equipamento", 22.0),
                PreconCardTemplate("Materia Blade", 1, "Artefato - Equipamento", 8.0),
                PreconCardTemplate("Ribbon", 1, "Artefato - Equipamento", 15.0),
                PreconCardTemplate("Knights of the Round", 1, "Feitiço Lendário", 25.0),
                PreconCardTemplate("Omnislash", 1, "Mágica Instantânea", 12.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Commander's Sphere", 1, "Artefato", 2.0),
                PreconCardTemplate("Boros Signet", 1, "Artefato", 2.5),
                PreconCardTemplate("Izzet Signet", 1, "Artefato", 2.5),
                PreconCardTemplate("Azorius Signet", 1, "Artefato", 2.5),
                PreconCardTemplate("Swords to Plowshares", 1, "Mágica Instantânea", 6.0),
                PreconCardTemplate("Counterspell", 1, "Mágica Instantânea", 5.0),
                PreconCardTemplate("Chaos Warp", 1, "Mágica Instantânea", 4.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Exotic Orchard", 1, "Terreno", 2.0),
                PreconCardTemplate("Mystic Monastery", 1, "Terreno", 2.0),
                PreconCardTemplate("Plains", 12, "Terreno Básico", 0.5),
                PreconCardTemplate("Mountain", 12, "Terreno Básico", 0.5),
                PreconCardTemplate("Island", 10, "Terreno Básico", 0.5)
            )
        ),

        // FF 5: Commander Revival Trance (Final Fantasy VI)
        PreconDeckTemplate(
            id = "mtg_ff_commander_revival_trance",
            name = "Final Fantasy Commander: Revival Trance (Final Fantasy VI)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Terra Branford",
            themeOrColors = "Multicolorido (5 Cores / Temur - Espers & Transe)",
            releaseYear = 2025,
            estimatedTotalBrl = 470.0,
            description = "Deck pré-construído oficial de 100 cartas de Commander de Final Fantasy VI liderado por Terra Branford e Locke Cole convocando Espers ancestrais e desencadeando o poder supremo de Transe.",
            productCode = "D3854000-CMD-2",
            aliases = listOf("Final Fantasy VI", "Terra Branford", "Revival Trance", "Espers", "Multicolorido", "Commander"),
            cardList = listOf(
                PreconCardTemplate("Terra Branford", 1, "Comandante / Criatura", 42.0),
                PreconCardTemplate("Locke Cole", 1, "Criatura Lendária", 15.0),
                PreconCardTemplate("Celes Chere", 1, "Criatura Lendária", 18.0),
                PreconCardTemplate("Edgar Roni Figaro", 1, "Criatura Lendária", 12.0),
                PreconCardTemplate("Sabin Rene Figaro", 1, "Criatura Lendária", 10.0),
                PreconCardTemplate("Shadow the Assassin", 1, "Criatura Lendária", 12.0),
                PreconCardTemplate("Kefka, Court Mage", 1, "Criatura Lendária", 25.0),
                PreconCardTemplate("Bahamut, Dragon King", 1, "Criatura Lendária", 22.0),
                PreconCardTemplate("Maduin, Guardian Esper", 1, "Criatura Lendária", 14.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Chromatic Lantern", 1, "Artefato", 12.0),
                PreconCardTemplate("Cultivate", 1, "Feitiço", 3.0),
                PreconCardTemplate("Farseek", 1, "Feitiço", 4.0),
                PreconCardTemplate("Beast Within", 1, "Mágica Instantânea", 5.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Path of Ancestry", 1, "Terreno", 2.0),
                PreconCardTemplate("Forest", 8, "Terreno Básico", 0.5),
                PreconCardTemplate("Island", 8, "Terreno Básico", 0.5),
                PreconCardTemplate("Mountain", 8, "Terreno Básico", 0.5),
                PreconCardTemplate("Plains", 6, "Terreno Básico", 0.5),
                PreconCardTemplate("Swamp", 6, "Terreno Básico", 0.5)
            )
        ),

        // FF 6: Commander Counter Blitz (Final Fantasy X)
        PreconDeckTemplate(
            id = "mtg_ff_commander_counter_blitz",
            name = "Final Fantasy Commander: Counter Blitz (Final Fantasy X)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Tidus & Yuna",
            themeOrColors = "Multicolorido (Bant - Blitzball & Aeons)",
            releaseYear = 2025,
            estimatedTotalBrl = 420.0,
            description = "Deck pré-construído oficial de 100 cartas de Final Fantasy X com Tidus e Yuna invocando Aeons celestiais (Valefor, Ifrit, Shiva, Bahamut, Anima) e habilidades aceleradas de Blitzball.",
            productCode = "D3854000-CMD-3",
            aliases = listOf("Final Fantasy X", "Tidus", "Yuna", "Counter Blitz", "Aeons", "Multicolorido", "Commander"),
            cardList = listOf(
                PreconCardTemplate("Tidus, Star Player", 1, "Comandante / Criatura", 32.0),
                PreconCardTemplate("Yuna, High Summoner", 1, "Comandante Parceiro", 30.0),
                PreconCardTemplate("Auron, Legendary Guardian", 1, "Criatura Lendária", 16.0),
                PreconCardTemplate("Wakka, Blitzball Captain", 1, "Criatura Lendária", 8.0),
                PreconCardTemplate("Lulu, Black Mage", 1, "Criatura Lendária", 10.0),
                PreconCardTemplate("Rikku, Al Bhed Thief", 1, "Criatura Lendária", 12.0),
                PreconCardTemplate("Kimahri Ronso", 1, "Criatura Lendária", 8.0),
                PreconCardTemplate("Valefor, Aerial Aeon", 1, "Criatura Lendária", 12.0),
                PreconCardTemplate("Shiva, Ice Queen", 1, "Criatura Lendária", 14.0),
                PreconCardTemplate("Bahamut, Mega Flare", 1, "Criatura Lendária", 24.0),
                PreconCardTemplate("Blitzball", 1, "Artefato - Equipamento", 8.0),
                PreconCardTemplate("Caladbolg", 1, "Artefato - Equipamento", 15.0),
                PreconCardTemplate("Nirvana", 1, "Artefato - Equipamento", 14.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Simic Signet", 1, "Artefato", 2.5),
                PreconCardTemplate("Selesnya Signet", 1, "Artefato", 2.5),
                PreconCardTemplate("Heroic Intervention", 1, "Mágica Instantânea", 20.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Seaside Citadel", 1, "Terreno", 2.0),
                PreconCardTemplate("Plains", 10, "Terreno Básico", 0.5),
                PreconCardTemplate("Island", 10, "Terreno Básico", 0.5),
                PreconCardTemplate("Forest", 10, "Terreno Básico", 0.5)
            )
        ),

        // FF 7: Commander Scions & Spellcraft (Final Fantasy XIV)
        PreconDeckTemplate(
            id = "mtg_ff_commander_scions_spellcraft",
            name = "Final Fantasy Commander: Scions & Spellcraft (Final Fantasy XIV)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Y'shtola, Night's Blessed",
            themeOrColors = "Multicolorido (Esper - Mágicas e Scions)",
            releaseYear = 2025,
            estimatedTotalBrl = 440.0,
            description = "Deck pré-construído oficial de 100 cartas de Final Fantasy XIV liderado por Y'shtola, Alphinaud e os Scions of the Seventh Dawn com magias arcanas grandiosas de Eorzea.",
            productCode = "D3854000-CMD-4",
            aliases = listOf("Final Fantasy XIV", "Y'shtola", "Alphinaud", "Scions & Spellcraft", "Multicolorido", "Commander"),
            cardList = listOf(
                PreconCardTemplate("Y'shtola, Night's Blessed", 1, "Comandante / Criatura", 35.0),
                PreconCardTemplate("Alphinaud Leveilleur", 1, "Criatura Lendária", 14.0),
                PreconCardTemplate("Alisaie Leveilleur", 1, "Criatura Lendária", 14.0),
                PreconCardTemplate("Thancred Waters", 1, "Criatura Lendária", 12.0),
                PreconCardTemplate("Urianger Augurelt", 1, "Criatura Lendária", 10.0),
                PreconCardTemplate("Estinien Varline", 1, "Criatura Lendária", 16.0),
                PreconCardTemplate("Emet-Selch, Architect", 1, "Criatura Lendária", 28.0),
                PreconCardTemplate("Flow (Arcane Teleport)", 1, "Mágica Instantânea", 8.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Esper Panorama", 1, "Terreno", 1.5),
                PreconCardTemplate("Arcane Sanctum", 1, "Terreno", 2.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Plains", 10, "Terreno Básico", 0.5),
                PreconCardTemplate("Island", 12, "Terreno Básico", 0.5),
                PreconCardTemplate("Swamp", 10, "Terreno Básico", 0.5)
            )
        ),
        // MTG 1: Veloci-ramp-tor (Commander Ixalan)
        PreconDeckTemplate(
            id = "mtg_veloci_ramptor",
            name = "Veloci-ramp-tor (Commander Dinossauros)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Pantlaza, Sun-Favored",
            themeOrColors = "Naya (Vermelho / Verde / Branco)",
            releaseYear = 2023,
            estimatedTotalBrl = 480.0,
            description = "O renomado deck de Commander de Dinossauros de Cavernas Perdidas de Ixalan, focado em estampar ameaças gigantescas com a habilidade Descobrir.",
            cardList = listOf(
                PreconCardTemplate("Pantlaza, Sun-Favored", 1, "Comandante / Criatura", 45.0),
                PreconCardTemplate("Gishath, Sun's Avatar", 1, "Criatura", 38.0),
                PreconCardTemplate("Zacama, Apex Dinosaur", 1, "Criatura", 22.0),
                PreconCardTemplate("Etali, Primal Storm", 1, "Criatura", 8.0),
                PreconCardTemplate("Apex Altisaur", 1, "Criatura", 12.0),
                PreconCardTemplate("Regisaur Alpha", 1, "Criatura", 6.0),
                PreconCardTemplate("Ranging Raptors", 1, "Criatura", 4.0),
                PreconCardTemplate("Ripjaw Raptor", 1, "Criatura", 7.0),
                PreconCardTemplate("Kinjalli's Sunwing", 1, "Criatura", 5.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Commander's Sphere", 1, "Artefato", 2.0),
                PreconCardTemplate("Cultivate", 1, "Feitiço", 3.0),
                PreconCardTemplate("Kodama's Reach", 1, "Feitiço", 3.0),
                PreconCardTemplate("Farseek", 1, "Feitiço", 4.0),
                PreconCardTemplate("Rampant Growth", 1, "Feitiço", 2.5),
                PreconCardTemplate("Swords to Plowshares", 1, "Mágica Instantânea", 6.0),
                PreconCardTemplate("Path to Exile", 1, "Mágica Instantânea", 7.0),
                PreconCardTemplate("Beast Within", 1, "Mágica Instantânea", 5.0),
                PreconCardTemplate("Generous Gift", 1, "Mágica Instantânea", 4.0),
                PreconCardTemplate("Lightning Greaves", 1, "Artefato", 14.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Path of Ancestry", 1, "Terreno", 2.0),
                PreconCardTemplate("Exotic Orchard", 1, "Terreno", 2.0),
                PreconCardTemplate("Cinder Glade", 1, "Terreno", 3.0),
                PreconCardTemplate("Canopy Vista", 1, "Terreno", 3.0),
                PreconCardTemplate("Jungle Shrine", 1, "Terreno", 2.0),
                PreconCardTemplate("Forest", 12, "Terreno Básico", 0.5),
                PreconCardTemplate("Mountain", 10, "Terreno Básico", 0.5),
                PreconCardTemplate("Plains", 8, "Terreno Básico", 0.5)
            )
        ),

        // MTG 2: Squirreled Away (Bloomburrow)
        PreconDeckTemplate(
            id = "mtg_squirreled_away",
            name = "Squirreled Away (Commander Esquilos)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Hazel of the Rootbloom",
            themeOrColors = "Golgari (Preto / Verde)",
            releaseYear = 2024,
            estimatedTotalBrl = 450.0,
            description = "Deck temático de Bloomburrow liderado por Hazel e Camellia, gerando fichas de esquilo e comida em enxames irresistíveis.",
            cardList = listOf(
                PreconCardTemplate("Hazel of the Rootbloom", 1, "Comandante / Criatura", 35.0),
                PreconCardTemplate("Camellia, the Seedmiser", 1, "Criatura", 28.0),
                PreconCardTemplate("Chatterfang, Squirrel Sovereign", 1, "Criatura", 34.0),
                PreconCardTemplate("Deep Forest Hermit", 1, "Criatura", 6.0),
                PreconCardTemplate("Chitterspitter", 1, "Artefato", 8.0),
                PreconCardTemplate("Skullclamp", 1, "Artefato", 15.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Golgari Signet", 1, "Artefato", 2.5),
                PreconCardTemplate("Talisman of Resilience", 1, "Artefato", 4.0),
                PreconCardTemplate("Shamanic Revelation", 1, "Feitiço", 4.0),
                PreconCardTemplate("Culling Ritual", 1, "Feitiço", 5.0),
                PreconCardTemplate("Assassin's Trophy", 1, "Mágica Instantânea", 12.0),
                PreconCardTemplate("Putrefy", 1, "Mágica Instantânea", 2.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Llanowar Wastes", 1, "Terreno", 5.0),
                PreconCardTemplate("Swamp", 15, "Terreno Básico", 0.5),
                PreconCardTemplate("Forest", 16, "Terreno Básico", 0.5)
            )
        ),

        // MTG 3: Animated Army (Bloomburrow)
        PreconDeckTemplate(
            id = "mtg_animated_army",
            name = "Animated Army (Commander Encantamentos e Artefatos)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Bello, Bard of the Brambles",
            themeOrColors = "Gruul (Vermelho / Verde)",
            releaseYear = 2024,
            estimatedTotalBrl = 390.0,
            description = "Deck de Bloomburrow onde artefatos e encantamentos de custo 4 ou mais viram elementais 4/4 com Ímpeto e Indestrutível no seu turno.",
            cardList = listOf(
                PreconCardTemplate("Bello, Bard of the Brambles", 1, "Comandante / Criatura", 30.0),
                PreconCardTemplate("Greater Good", 1, "Encantamento", 15.0),
                PreconCardTemplate("Berserkers' Onslaught", 1, "Encantamento", 6.0),
                PreconCardTemplate("Gilded Lotus", 1, "Artefato", 6.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Gruul Signet", 1, "Artefato", 2.5),
                PreconCardTemplate("Chaos Warp", 1, "Mágica Instantânea", 4.0),
                PreconCardTemplate("Blasphemous Act", 1, "Feitiço", 8.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Karplusan Forest", 1, "Terreno", 8.0),
                PreconCardTemplate("Forest", 15, "Terreno Básico", 0.5),
                PreconCardTemplate("Mountain", 14, "Terreno Básico", 0.5)
            )
        ),

        // MTG 4: Eldrazi Incursion (Modern Horizons 3)
        PreconDeckTemplate(
            id = "mtg_eldrazi_incursion",
            name = "Eldrazi Incursion (Modern Horizons 3)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Ulalek, Fused Atrocity",
            themeOrColors = "5 Cores / Incolor Eldrazi",
            releaseYear = 2024,
            estimatedTotalBrl = 650.0,
            description = "Poder titânico Eldrazi de Modern Horizons 3 duplicando mágicas e habilidades descoloridas avassaladoras.",
            cardList = listOf(
                PreconCardTemplate("Ulalek, Fused Atrocity", 1, "Comandante / Criatura", 55.0),
                PreconCardTemplate("Eldrazi Temple", 1, "Terreno", 22.0),
                PreconCardTemplate("All Is Dust", 1, "Feitiço Tribal", 18.0),
                PreconCardTemplate("Kozilek's Return", 1, "Mágica Instantânea", 6.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Worn Powerstone", 1, "Artefato", 3.0),
                PreconCardTemplate("Thran Dynamo", 1, "Artefato", 10.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Wastes", 10, "Terreno Básico Incolor", 2.0)
            )
        ),

        // MTG 5: Blood Rites (Commander Vampiros)
        PreconDeckTemplate(
            id = "mtg_blood_rites",
            name = "Blood Rites (Commander Vampiros Orzhov)",
            game = "Magic: The Gathering",
            format = "Commander",
            commanderOrLead = "Clavileño, First of the Blessed",
            themeOrColors = "Orzhov (Preto / Branco)",
            releaseYear = 2023,
            estimatedTotalBrl = 410.0,
            description = "Sacrifício tribal de Vampiros criando demônios 4/3 voadores e drenando vida com sinergia aristocrata.",
            cardList = listOf(
                PreconCardTemplate("Clavileño, First of the Blessed", 1, "Comandante / Criatura", 25.0),
                PreconCardTemplate("Elenda, the Dusk Rose", 1, "Criatura", 18.0),
                PreconCardTemplate("Vito, Thorn of the Dusk Rose", 1, "Criatura", 14.0),
                PreconCardTemplate("Sol Ring", 1, "Artefato", 8.5),
                PreconCardTemplate("Arcane Signet", 1, "Artefato", 4.0),
                PreconCardTemplate("Orzhov Signet", 1, "Artefato", 2.5),
                PreconCardTemplate("Swords to Plowshares", 1, "Mágica Instantânea", 6.0),
                PreconCardTemplate("Command Tower", 1, "Terreno", 2.5),
                PreconCardTemplate("Plains", 14, "Terreno Básico", 0.5),
                PreconCardTemplate("Swamp", 15, "Terreno Básico", 0.5)
            )
        ),

        // POKÉMON 1: Baralho de Batalha de Liga: Charizard ex
        PreconDeckTemplate(
            id = "pkm_charizard_ex",
            name = "Baralho de Batalha de Liga: Charizard ex",
            game = "Pokémon TCG",
            format = "Standard",
            commanderOrLead = "Charizard ex",
            themeOrColors = "Fogo / Escuridão (Infernal Reign)",
            releaseYear = 2024,
            estimatedTotalBrl = 240.0,
            description = "Baralho competitivo completo nível 3 da Liga Pokémon, com Charizard ex de Escuridão, motor Pidgeot ex e aceleração massiva de energias.",
            cardList = listOf(
                PreconCardTemplate("Charizard ex", 3, "Pokémon", 85.0),
                PreconCardTemplate("Charmander", 4, "Pokémon", 1.5),
                PreconCardTemplate("Charmeleon", 1, "Pokémon", 2.0),
                PreconCardTemplate("Pidgeot ex", 2, "Pokémon", 35.0),
                PreconCardTemplate("Pidgey", 2, "Pokémon", 1.0),
                PreconCardTemplate("Bibarel", 2, "Pokémon", 4.0),
                PreconCardTemplate("Bidoof", 2, "Pokémon", 1.0),
                PreconCardTemplate("Lumineon V", 1, "Pokémon", 6.0),
                PreconCardTemplate("Raro Doce", 4, "Item / Treinador", 3.0),
                PreconCardTemplate("Ultra Bola", 4, "Item / Treinador", 2.5),
                PreconCardTemplate("Bola Ninho", 4, "Item / Treinador", 2.5),
                PreconCardTemplate("Poffin de Colega", 4, "Item / Treinador", 6.0),
                PreconCardTemplate("Ordem da Chefia", 3, "Apoiador", 3.0),
                PreconCardTemplate("Pesquisa de Professores", 3, "Apoiador", 1.5),
                PreconCardTemplate("Iono", 3, "Apoiador", 5.0),
                PreconCardTemplate("Super Bastão", 2, "Item / Treinador", 3.0),
                PreconCardTemplate("Energia Básica de Fogo", 16, "Energia Básica", 0.5)
            )
        ),

        // POKÉMON 2: Baralho de Batalha de Liga: Miraidon ex
        PreconDeckTemplate(
            id = "pkm_miraidon_ex",
            name = "Baralho de Batalha de Liga: Miraidon ex",
            game = "Pokémon TCG",
            format = "Standard",
            commanderOrLead = "Miraidon ex",
            themeOrColors = "Elétrico / Tandem Unit",
            releaseYear = 2024,
            estimatedTotalBrl = 190.0,
            description = "Baralho veloz que busca atacantes elétricos direto do banco no turno 1 e acelera dano com Gerador Elétrico e Raikou V.",
            cardList = listOf(
                PreconCardTemplate("Miraidon ex", 3, "Pokémon", 25.0),
                PreconCardTemplate("Raikou V", 2, "Pokémon", 12.0),
                PreconCardTemplate("Regieleki VMAX", 2, "Pokémon", 8.0),
                PreconCardTemplate("Regieleki V", 2, "Pokémon", 4.0),
                PreconCardTemplate("Zapdos", 1, "Pokémon", 3.0),
                PreconCardTemplate("Gerador Elétrico", 4, "Item / Treinador", 4.0),
                PreconCardTemplate("Ultra Bola", 4, "Item / Treinador", 2.5),
                PreconCardTemplate("Bola Ninho", 4, "Item / Treinador", 2.5),
                PreconCardTemplate("Corda de Fuga", 2, "Item / Treinador", 2.0),
                PreconCardTemplate("Ordem da Chefia", 3, "Apoiador", 3.0),
                PreconCardTemplate("Pesquisa de Professores", 4, "Apoiador", 1.5),
                PreconCardTemplate("Juiz", 2, "Apoiador", 1.0),
                PreconCardTemplate("Energia Básica Elétrica", 17, "Energia Básica", 0.5)
            )
        ),

        // POKÉMON 3: Baralho de Batalha de Liga: Gardevoir ex
        PreconDeckTemplate(
            id = "pkm_gardevoir_ex",
            name = "Baralho de Batalha de Liga: Gardevoir ex",
            game = "Pokémon TCG",
            format = "Standard",
            commanderOrLead = "Gardevoir ex",
            themeOrColors = "Psíquico (Abraço Psíquico)",
            releaseYear = 2024,
            estimatedTotalBrl = 210.0,
            description = "Baralho estratégico que anexa energias psíquicas da pilha de descarte quantas vezes quiser por turno para pulverizar o oponente.",
            cardList = listOf(
                PreconCardTemplate("Gardevoir ex", 3, "Pokémon", 30.0),
                PreconCardTemplate("Kirlia (Refinamento)", 4, "Pokémon", 4.0),
                PreconCardTemplate("Ralts", 4, "Pokémon", 1.0),
                PreconCardTemplate("Mew ex", 1, "Pokémon", 18.0),
                PreconCardTemplate("Greninja Radiante", 1, "Pokémon", 15.0),
                PreconCardTemplate("Raro Doce", 4, "Item / Treinador", 3.0),
                PreconCardTemplate("Pedaço de Névoa", 4, "Item / Treinador", 2.0),
                PreconCardTemplate("Nível Bola", 3, "Item / Treinador", 2.0),
                PreconCardTemplate("Iono", 3, "Apoiador", 5.0),
                PreconCardTemplate("Ordem da Chefia", 2, "Apoiador", 3.0),
                PreconCardTemplate("Energia Básica Psíquica", 14, "Energia Básica", 0.5)
            ),
            strategyGuide = "Energize seus atacantes psíquicos infinitamente direto do descarte com a habilidade Abraço Psíquico de Gardevoir ex, escalando dano e curando com Força Milagrosa.",
            winCondition = "Nocautear os atacantes inimigos com atacantes de 1 prêmio (Scream Tail, Drifloon) energizados com múltiplas energias psíquicas do descarte."
        ),

        // === YU-GI-OH! TCG ===
        // YGO 1: Structure Deck: Albaz Strike (Golpe de Albaz)
        PreconDeckTemplate(
            id = "ygo_albaz_strike",
            name = "Structure Deck: Albaz Strike (Golpe de Albaz)",
            game = "Yu-Gi-Oh!",
            format = "Avançado (TCG)",
            commanderOrLead = "Fallen of Albaz & Mirrorjade",
            themeOrColors = "Escuridão / Dragões de Fusão Despia",
            releaseYear = 2022,
            estimatedTotalBrl = 220.0,
            description = "Deck Estrutural oficial da Konami centrado na história de Albaz e Ecclesia. Invoca Dragões de Fusão lendários (Mirrorjade, Lubellion) utilizando monstros do oponente como matéria de fusão.",
            productCode = "SD-ALBAZ-01",
            aliases = listOf("Albaz", "Branded", "Mirrorjade", "Fallen of Albaz", "Despia", "Konami", "Fusão"),
            strategyGuide = "Ative Branded Fusion diretamente do seu Deck para enviar Fallen of Albaz e um monstro de Luz ao cemitério para invocar Lubellion the Searing Dragon. Lubellion então descarta 1 carta e embaralha as matérias de volta ao Deck para trazer Mirrorjade the Iceblade Dragon. No turno do oponente, use o efeito rápido de Mirrorjade para banir qualquer monstro perigoso sem precisar destruir.",
            winCondition = "Travar o campo adversário com o banimento rápido de Mirrorjade e finalizar a partida com mais de 8000 de dano conjunto de Mirrorjade (3000 ATK) e Lubellion (2500 ATK).",
            keyCombos = listOf(
                "Branded Fusion do Deck -> envia Albaz + Tragédia -> Lubellion -> invoca Mirrorjade",
                "Fallen of Albaz invocado normalmente usa o monstro chefe do oponente como matéria de fusão",
                "Efeito de Mirrorjade no turno adversário pune qualquer tentativa de destruição limpando a mesa na Fase Final"
            ),
            cardList = listOf(
                PreconCardTemplate("Fallen of Albaz", 3, "Monstro de Efeito", 18.0),
                PreconCardTemplate("Mirrorjade the Iceblade Dragon", 1, "Monstro de Fusão", 28.0),
                PreconCardTemplate("Lubellion the Searing Dragon", 1, "Monstro de Fusão", 16.0),
                PreconCardTemplate("Albion the Branded Dragon", 1, "Monstro de Fusão", 12.0),
                PreconCardTemplate("Branded Fusion", 3, "Card de Magia Normal", 15.0),
                PreconCardTemplate("Tri-Brigade Mercourier", 2, "Monstro de Efeito", 6.0),
                PreconCardTemplate("Springans Kitt", 2, "Monstro de Efeito", 5.0),
                PreconCardTemplate("Ash Blossom & Joyous Spring", 1, "Monstro Regulador / Efeito", 25.0),
                PreconCardTemplate("Effect Veiler", 2, "Monstro Regulador / Efeito", 8.0),
                PreconCardTemplate("Ghost Ogre & Snow Rabbit", 1, "Monstro Regulador / Efeito", 7.0),
                PreconCardTemplate("Dark Ruler No More", 2, "Card de Magia Normal", 8.0),
                PreconCardTemplate("Pot of Extravagance", 1, "Card de Magia Normal", 12.0),
                PreconCardTemplate("Called by the Grave", 1, "Card de Magia Rápida", 10.0),
                PreconCardTemplate("Branded in High Spirits", 2, "Card de Magia Rápida", 4.0),
                PreconCardTemplate("There Can Be Only One", 1, "Card de Armadilha Contínua", 6.0),
                PreconCardTemplate("Solemn Judgment", 1, "Card de Armadilha de Resposta", 10.0),
                PreconCardTemplate("Back to the Front", 2, "Card de Armadilha Normal", 3.0)
            )
        ),

        // YGO 2: Structure Deck: Fire Kings (Reis de Fogo)
        PreconDeckTemplate(
            id = "ygo_fire_kings",
            name = "Structure Deck: Fire Kings (Reis de Fogo)",
            game = "Yu-Gi-Oh!",
            format = "Avançado (TCG)",
            commanderOrLead = "Fire King High Avatar Garunix",
            themeOrColors = "FOGO / Bestas Aladas & Destruição Mútua",
            releaseYear = 2023,
            estimatedTotalBrl = 240.0,
            description = "Deck Estrutural que renasce das cinzas a cada rodada. Quanto mais seus próprios monstros são destruídos, mais efeitos de destruição e invocações especiais em massa são disparados.",
            productCode = "SD-FIRE-KINGS",
            aliases = listOf("Fire Kings", "Garunix", "Ponix", "Kirin", "Reis de Fogo", "Konami"),
            strategyGuide = "Comece com Legendary Fire King Ponix para buscar Fire King Sanctuary ou Island. Destrua Garunix da sua mão ou campo para ativar sua invocação durante a próxima Standby Phase. Garunix então limpa TODOS os outros monstros de ambos os campos.",
            winCondition = "Manter um ciclo constante de Garunix e Kirin limpando o campo do oponente todo turno enquanto suas bestas aladas de FOGO atacam diretamente os pontos de vida desprotegidos.",
            keyCombos = listOf(
                "Ponix busca Fire King Island -> Island destrói Garunix da mão para buscar Kirin -> Garunix renasce e destrói o campo",
                "Kirin ativa da mão no turno do oponente para destruir e interromper jogadas"
            ),
            cardList = listOf(
                PreconCardTemplate("Fire King High Avatar Garunix", 2, "Monstro de Efeito", 22.0),
                PreconCardTemplate("Legendary Fire King Ponix", 3, "Monstro de Efeito", 16.0),
                PreconCardTemplate("Fire King Avatar Arvata", 3, "Monstro de Efeito", 8.0),
                PreconCardTemplate("Fire King Avatar Barong", 2, "Monstro de Efeito", 5.0),
                PreconCardTemplate("Fire King Sanctuary", 3, "Card de Magia Contínua", 10.0),
                PreconCardTemplate("Fire King Island", 2, "Card de Magia de Campo", 6.0),
                PreconCardTemplate("Fire King Sky Burn", 2, "Card de Magia Rápida", 5.0),
                PreconCardTemplate("Circle of the Fire Kings", 2, "Card de Magia Rápida", 4.0),
                PreconCardTemplate("Ash Blossom & Joyous Spring", 1, "Monstro Regulador / Efeito", 25.0),
                PreconCardTemplate("Droll & Lock Bird", 1, "Monstro de Efeito", 14.0),
                PreconCardTemplate("Torrential Tribute", 2, "Card de Armadilha Normal", 5.0),
                PreconCardTemplate("Solemn Judgment", 1, "Card de Armadilha de Resposta", 10.0)
            )
        ),

        // YGO 3: Structure Deck: Dark World (Mundo Sombrio)
        PreconDeckTemplate(
            id = "ygo_dark_world",
            name = "Structure Deck: Dark World (Mundo Sombrio)",
            game = "Yu-Gi-Oh!",
            format = "Avançado (TCG)",
            commanderOrLead = "Grapha, Dragon Lord of Dark World",
            themeOrColors = "Escuridão / Demônios & Descarte Recursivo",
            releaseYear = 2022,
            estimatedTotalBrl = 210.0,
            description = "Deck focado em comprar cartas compulsivamente através de descartes sinérgicos que destroem o campo e a mão do oponente sem perder recursos.",
            productCode = "SD-DARK-WORLD",
            aliases = listOf("Dark World", "Grapha", "Mundo Sombrio", "Snoww", "Descarte", "Konami"),
            strategyGuide = "Ative The Gates of Dark World ou Dark World Archives para descartar Snoww, Broww ou Grapha. Snoww busca qualquer carta do arquétipo, Broww compra cartas adicionais, e Grapha destrói cartas do oponente ao ser descartado e pode retornar do cemitério infinitamente.",
            winCondition = "Esgotar os recursos e anulações do adversário com compras contínuas e encher o campo com Grapha (2700 ATK) para um ataque fulminante.",
            keyCombos = listOf(
                "Snoww descarta -> busca The Gates of Dark World -> compra e descarte de Grapha -> destrói carta e revive Grapha",
                "Card Destruction descarta toda a mão disparando todos os monstros Mundo Sombrio de uma vez"
            ),
            cardList = listOf(
                PreconCardTemplate("Grapha, Dragon Lord of Dark World", 3, "Monstro de Efeito", 15.0),
                PreconCardTemplate("Snoww, Unlight of Dark World", 3, "Monstro de Efeito", 8.0),
                PreconCardTemplate("Broww, Huntsman of Dark World", 3, "Monstro de Efeito", 6.0),
                PreconCardTemplate("Genta, Gateman of Dark World", 3, "Monstro de Efeito", 6.0),
                PreconCardTemplate("The Gates of Dark World", 3, "Card de Magia de Campo", 5.0),
                PreconCardTemplate("Dark World Archives", 2, "Card de Magia Contínua", 4.0),
                PreconCardTemplate("Dark World Accession", 2, "Card de Magia Rápida", 5.0),
                PreconCardTemplate("Card Destruction", 1, "Card de Magia Normal", 10.0),
                PreconCardTemplate("Allure of Darkness", 3, "Card de Magia Normal", 5.0),
                PreconCardTemplate("Skill Drain", 1, "Card de Armadilha Contínua", 8.0)
            )
        ),

        // === ONE PIECE CARD GAME ===
        // OP 1: ST-01 Straw Hat Crew (Bando do Chapéu de Palha)
        PreconDeckTemplate(
            id = "op_st01_straw_hat",
            name = "Starter Deck ST-01: Straw Hat Crew (Bando do Chapéu de Palha)",
            game = "One Piece Card Game",
            format = "Standard (50 Cartas + Líder + 10 DON!!)",
            commanderOrLead = "Monkey.D.Luffy (Líder Vermelho)",
            themeOrColors = "Vermelho / Agressivo (Rush & Buff de DON!!)",
            releaseYear = 2022,
            estimatedTotalBrl = 170.0,
            description = "O primeiro e mais clássico Starter Deck de One Piece Card Game, estrelando Luffy, Zoro, Nami, Sanji e Chopper com ataques relâmpago de <Rush> e pressão irresistível na Vida do oponente.",
            productCode = "ST-01",
            aliases = listOf("One Piece", "Luffy", "Straw Hat", "Chapéu de Palha", "Zoro", "Bandai", "ST01"),
            strategyGuide = "No turno 1, coloque Nami em campo para buscar seus melhores atacantes. No turno 2, jogue Roronoa Zoro (custo 3) que entra com <Rush> atacando imediatamente com 5000 de Poder. Use a habilidade do Líder Luffy para acoplar cartas de DON!! descansadas e aumentar o ataque dos seus piratas sem gastar recursos extras. Defenda com Chopper (Bloqueador de custo 1) e Guard Point.",
            winCondition = "Reduzir a Vida do Líder oponente para 0 rapidamente com ataques rápidos de Zoro e Luffy bufados com DON!!, finalizando com Diable Jambe (Sanji) para impedir que o oponente bloqueie o golpe final.",
            keyCombos = listOf(
                "Turno 1 Nami busca Zoro -> Turno 2 Zoro entra atacando com Rush",
                "Luffy Líder [Ativar: Principal] recupera DON!! descansado para o atacante",
                "Diable Jambe impede bloqueadores de defender a última Vida adversária"
            ),
            cardList = listOf(
                PreconCardTemplate("Monkey.D.Luffy", 1, "Líder / Personagem", 30.0),
                PreconCardTemplate("Roronoa Zoro", 4, "Personagem (Rush)", 25.0),
                PreconCardTemplate("Nami", 4, "Personagem (Buscadora)", 22.0),
                PreconCardTemplate("Tony Tony.Chopper", 4, "Personagem (Bloqueador)", 8.0),
                PreconCardTemplate("Sanji", 4, "Personagem (Rush)", 10.0),
                PreconCardTemplate("Usopp", 4, "Personagem", 4.0),
                PreconCardTemplate("Brook", 4, "Personagem", 4.0),
                PreconCardTemplate("Nico Robin", 4, "Personagem", 6.0),
                PreconCardTemplate("Franky", 4, "Personagem", 5.0),
                PreconCardTemplate("Jinbe", 4, "Personagem", 4.0),
                PreconCardTemplate("Gum-Gum Jet Pistol", 4, "Card de Evento", 12.0),
                PreconCardTemplate("Diable Jambe", 4, "Card de Evento", 6.0),
                PreconCardTemplate("Guard Point", 4, "Card de Evento", 5.0)
            )
        ),

        // OP 2: Ultra Deck ST-10: The Three Captains (Os Três Capitães)
        PreconDeckTemplate(
            id = "op_st10_three_captains",
            name = "Ultra Deck ST-10: The Three Captains (Os Três Capitães)",
            game = "One Piece Card Game",
            format = "Standard (50 Cartas + 3 Líderes + 10 DON!!)",
            commanderOrLead = "Luffy, Law & Kid (Líderes Duplos Vermelho/Roxo)",
            themeOrColors = "Vermelho / Roxo (Rampa e Manipulação de DON!!)",
            releaseYear = 2023,
            estimatedTotalBrl = 230.0,
            description = "O renomado Ultra Deck trazendo os três capitães da Pior Geração em Wano com três opções de Líderes duplos holo (Luffy Vermelho/Roxo, Law Vermelho/Roxo, Kid Vermelho/Roxo).",
            productCode = "ST-10",
            aliases = listOf("One Piece", "Law", "Kid", "Luffy", "Three Captains", "Três Capitães", "ST10", "Bandai"),
            strategyGuide = "Com Trafalgar Law como líder, utilize seu efeito de [DON!! -3] para enviar os personagens pequenos do adversário (3000 ou menos) para o fundo do deck e jogar imediatamente um personagem de custo 4 da sua mão sem pagar custo. Com Eustass Kid, use seu efeito agressivo para resetar o Líder e atacar duas vezes no mesmo turno.",
            winCondition = "Destruir a presença de mesa do oponente com o efeito de Law enquanto constrói um campo repleto de piratas de 5000+ de poder para sobrecarregar a mão e defesas do oponente.",
            keyCombos = listOf(
                "Law Líder efeito: remove ameaça pequena do oponente e joga personagem custo 4 da mão de graça",
                "Kid Líder duplo ataque com 7000 de Poder no mesmo turno",
                "Luffy Vermelho/Roxo acelerando DON!! adicional todo turno"
            ),
            cardList = listOf(
                PreconCardTemplate("Trafalgar Law", 1, "Líder Duplo Vermelho/Roxo", 28.0),
                PreconCardTemplate("Monkey.D.Luffy", 1, "Líder Duplo Vermelho/Roxo", 30.0),
                PreconCardTemplate("Eustass \"Captain\" Kid", 1, "Líder Duplo Vermelho/Roxo", 24.0),
                PreconCardTemplate("Roronoa Zoro", 4, "Personagem", 20.0),
                PreconCardTemplate("Bepo", 4, "Personagem", 5.0),
                PreconCardTemplate("Killer", 4, "Personagem (Bloqueador)", 8.0),
                PreconCardTemplate("Jean Bart", 4, "Personagem (Rampa DON)", 8.0),
                PreconCardTemplate("Shachi & Penguin", 4, "Personagem", 4.0),
                PreconCardTemplate("Paulie", 2, "Personagem (Custo 5)", 10.0),
                PreconCardTemplate("Radical Beam", 4, "Card de Evento", 8.0),
                PreconCardTemplate("Gum-Gum Jet Pistol", 4, "Card de Evento", 12.0)
            )
        ),

        // OP 3: Ultra Deck ST-13: The Three Brothers (Os Três Irmãos)
        PreconDeckTemplate(
            id = "op_st13_three_brothers",
            name = "Ultra Deck ST-13: The Three Brothers (Os Três Irmãos)",
            game = "One Piece Card Game",
            format = "Standard (50 Cartas + 3 Líderes + 10 DON!!)",
            commanderOrLead = "Portgas.D.Ace, Sabo & Monkey.D.Luffy",
            themeOrColors = "Amarelo / Gatilhos de Vida & Irmãos",
            releaseYear = 2024,
            estimatedTotalBrl = 250.0,
            description = "Ultra Deck temático celebrando os laços inquebráveis dos três irmãos Ace, Sabo e Luffy, manipulando as cartas de Vida com face para cima e ativando Gatilhos de alta potência.",
            productCode = "ST-13",
            aliases = listOf("One Piece", "Ace", "Sabo", "Luffy", "Three Brothers", "Três Irmãos", "ST13", "Bandai"),
            strategyGuide = "Coloque personagens com habilidades defensivas em campo (como Sabo Bloqueador de custo 5). Use a habilidade do Líder Ace para manipular a ordem dos seus cartões de Vida, garantindo que gatilhos destrutivos sejam acionados quando o oponente atacar.",
            winCondition = "Garantir controle total de Vida com Sabo protegendo suas peças contra nocaute e finalizando com Ace (Custo 7) com ataques velozes imbloqueáveis.",
            keyCombos = listOf(
                "Sabo Bloqueador protege todo o campo contra efeitos de K.O. e compra 2 cartas",
                "Ace Líder reordena a Vida para garantir gatilhos imediatos nos ataques inimigos"
            ),
            cardList = listOf(
                PreconCardTemplate("Portgas.D.Ace", 1, "Líder Amarelo", 35.0),
                PreconCardTemplate("Sabo", 1, "Líder Amarelo", 26.0),
                PreconCardTemplate("Monkey.D.Luffy", 1, "Líder Amarelo", 28.0),
                PreconCardTemplate("Sabo", 4, "Personagem (Bloqueador)", 26.0),
                PreconCardTemplate("Makino", 4, "Personagem", 6.0),
                PreconCardTemplate("Curly Dadan", 4, "Personagem", 8.0),
                PreconCardTemplate("Monkey.D.Garp", 4, "Personagem", 8.0),
                PreconCardTemplate("Flame Dragon King", 4, "Card de Evento", 10.0),
                PreconCardTemplate("Fire Fist", 4, "Card de Evento", 10.0)
            )
        )
    )
}
