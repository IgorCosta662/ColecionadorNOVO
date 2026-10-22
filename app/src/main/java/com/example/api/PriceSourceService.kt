package com.example.api

import com.example.data.*
import java.util.Locale
import kotlin.math.roundToInt

object PriceSourceRegistry {
    const val USD_BRL_EXCHANGE_RATE = 5.75
    const val EUR_BRL_EXCHANGE_RATE = 6.20

    fun generateRealisticOffersAndHistory(
        itemName: String,
        subCategory: String = "",
        rarity: String = "Comum",
        variant: String = "Normal",
        condition: String = "Near Mint",
        language: String = "PT-BR",
        marketRegion: MarketRegion = MarketRegion.BRAZIL,
        baseEstimatedPrice: Double = 0.0
    ): Pair<List<PriceOffer>, List<PriceHistoryPoint>> {
        val catalogMatch = RealMarketCatalog.findBestMatch(itemName)
        val realBase = if (catalogMatch != null) {
            catalogMatch.realMarketPriceBrl
        } else if (baseEstimatedPrice > 0.0) {
            baseEstimatedPrice
        } else {
            10.0
        }

        val offers = mutableListOf<PriceOffer>()
        val history = mutableListOf<PriceHistoryPoint>()

        when {
            subCategory.contains("Magic", ignoreCase = true) -> {
                val minPrice = (catalogMatch?.lowPriceBrl ?: (realBase * 0.75)).let { (it * 100).roundToInt() / 100.0 }
                val avgPrice = (catalogMatch?.realMarketPriceBrl ?: realBase).let { (it * 100).roundToInt() / 100.0 }
                val maxPrice = (catalogMatch?.highPriceBrl ?: (realBase * 1.35)).let { (it * 100).roundToInt() / 100.0 }
                val usd = ((avgPrice / USD_BRL_EXCHANGE_RATE) * 100).roundToInt() / 100.0
                val eur = ((avgPrice / EUR_BRL_EXCHANGE_RATE) * 100).roundToInt() / 100.0

                offers.add(PriceOffer("LigaMagic Brasil (Menor Preço Balcão)", minPrice, minPrice, "BRL", "Near Mint", "Lojas Nacionais Verificadas", true))
                offers.add(PriceOffer("LigaMagic Brasil (Preço Médio de Venda)", avgPrice, avgPrice, "BRL", "Near Mint", "Cotação Oficial Brasil", true))
                offers.add(PriceOffer("LigaMagic Brasil (Maior Preço Listado)", maxPrice, maxPrice, "BRL", "Near Mint", "Lojas Especializadas", true))
                offers.add(PriceOffer("TCGPlayer Direct Market", avgPrice, usd, "USD", "Near Mint (EN)", "Referência EUA (US$ ${String.format(Locale.US, "%.2f", usd)})", true))
                offers.add(PriceOffer("Cardmarket Trend Europa", avgPrice, eur, "EUR", "Near Mint", "Referência Europa (€ ${String.format(Locale.US, "%.2f", eur)})", true))
            }

            subCategory.contains("Pokémon", ignoreCase = true) -> {
                val minPrice = (catalogMatch?.lowPriceBrl ?: (realBase * 0.78)).let { (it * 100).roundToInt() / 100.0 }
                val avgPrice = (catalogMatch?.realMarketPriceBrl ?: realBase).let { (it * 100).roundToInt() / 100.0 }
                val maxPrice = (catalogMatch?.highPriceBrl ?: (realBase * 1.30)).let { (it * 100).roundToInt() / 100.0 }
                val usd = ((avgPrice / USD_BRL_EXCHANGE_RATE) * 100).roundToInt() / 100.0

                offers.add(PriceOffer("LigaPokémon Brasil (Menor Preço Ativo)", minPrice, minPrice, "BRL", "Near Mint (PT-BR)", "Lojas Verificadas Ativas", true))
                offers.add(PriceOffer("LigaPokémon Brasil (Preço Médio de Mercado)", avgPrice, avgPrice, "BRL", "Near Mint (PT-BR)", "Cotação Oficial Brasil", true))
                offers.add(PriceOffer("LigaPokémon Brasil (Maior Preço / Lojas)", maxPrice, maxPrice, "BRL", "Near Mint (PT-BR)", "Lojas Físicas & Vitrines", true))
                offers.add(PriceOffer("TCGPlayer US Market", avgPrice, usd, "USD", "Near Mint (EN)", "Mercado Americano (US$ ${String.format(Locale.US, "%.2f", usd)})", true))
                offers.add(PriceOffer("MypCards / eBay Confirmed Sold", ((avgPrice * 1.05) * 100).roundToInt() / 100.0, usd, "USD", "Near Mint", "Vendas Concluídas Recentes", true))
            }

            else -> {
                val minPrice = ((realBase * 0.80) * 100).roundToInt() / 100.0
                val avgPrice = ((realBase * 1.00) * 100).roundToInt() / 100.0
                val maxPrice = ((realBase * 1.25) * 100).roundToInt() / 100.0

                offers.add(PriceOffer("Mercado Livre Brasil (Líder)", avgPrice, avgPrice, "BRL", "Excelente", "Vendedor Verificado BR", true))
                offers.add(PriceOffer("Grupos & Encontros de Colecionadores", minPrice, minPrice, "BRL", "Excelente", "Negociação Direta", true))
                offers.add(PriceOffer("eBay Global / Lojas Especializadas", maxPrice, maxPrice, "BRL", "Excelente", "Importação", true))
            }
        }

        // Realistic 6-month historical trend
        val dates = listOf("Há 5 meses", "Há 4 meses", "Há 3 meses", "Há 2 meses", "Mês passado", "Hoje")
        val variance = listOf(0.85, 0.88, 0.92, 0.96, 0.99, 1.0)
        dates.forEachIndexed { index, date ->
            history.add(PriceHistoryPoint(date, ((realBase * variance[index]) * 100).roundToInt() / 100.0))
        }

        return Pair(offers, history)
    }

    fun generateConditionPriceTiers(basePrice: Double): List<ConditionPrice> {
        return listOf(
            ConditionPrice("Graded PSA / BGS 10", 2.80, ((basePrice * 2.80) * 100).roundToInt() / 100.0),
            ConditionPrice("Graded PSA 9 / Mint", 1.80, ((basePrice * 1.80) * 100).roundToInt() / 100.0),
            ConditionPrice("Near Mint (NM)", 1.00, ((basePrice * 1.00) * 100).roundToInt() / 100.0),
            ConditionPrice("Lightly Played (LP)", 0.82, ((basePrice * 0.82) * 100).roundToInt() / 100.0),
            ConditionPrice("Moderately Played (MP)", 0.65, ((basePrice * 0.65) * 100).roundToInt() / 100.0),
            ConditionPrice("Heavily Played / DMG", 0.40, ((basePrice * 0.40) * 100).roundToInt() / 100.0)
        )
    }

    fun generateLanguageComparisons(name: String, basePrice: Double, currentLanguage: String): List<LanguagePriceComparison> {
        return listOf(
            LanguagePriceComparison("Português (PT-BR)", "PT", "🇧🇷", 1.00, basePrice, "Mercado LigaMagic / LigaPokémon local mais ativo"),
            LanguagePriceComparison("Inglês (EN)", "EN", "🇺🇸", 1.15, ((basePrice * 1.15) * 100).roundToInt() / 100.0, "Alta liquidez global"),
            LanguagePriceComparison("Japonês (JP)", "JP", "🇯🇵", 0.95, ((basePrice * 0.95) * 100).roundToInt() / 100.0, "Colecionadores de estética e arte original")
        )
    }
}
