package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class PriceOffer(
    val storeName: String,
    val priceInBRL: Double,
    val originalPrice: Double,
    val originalCurrency: String,
    val condition: String,
    val listingType: String,
    val isVerified: Boolean = true
)

@Serializable
data class PriceHistoryPoint(
    val date: String,
    val priceInBRL: Double
)

data class ConditionAssessment(
    val estimatedCondition: String,
    val confidencePercent: Int,
    val scratches: String,
    val edges: String,
    val corners: String,
    val centering: String,
    val bends: String,
    val notes: String
)

data class ConditionPrice(
    val condition: String,
    val multiplier: Double,
    val priceInBrl: Double
)

data class LanguagePriceComparison(
    val language: String,
    val code: String,
    val flag: String,
    val multiplier: Double,
    val priceInBrl: Double,
    val liquidityNote: String
)

data class MarketSourceQuote(
    val sourceName: String,
    val price: Double,
    val currency: String,
    val priceInBrl: Double,
    val weightPercent: Int,
    val condition: String = "Near Mint",
    val isDomestic: Boolean = true,
    val countryCode: String = "BR"
)

data class CrossReferencedPriceReport(
    val primaryConsensusPriceBrl: Double,
    val lowestVerifiedOfferBrl: Double,
    val highestVerifiedOfferBrl: Double,
    val internationalAvgUsd: Double,
    val internationalAvgBrl: Double,
    val domesticLigaAvgBrl: Double,
    val parityDifferenceBrl: Double,
    val parityDifferencePercent: Double,
    val domesticLiquidityStatus: String,
    val quotesUsed: List<MarketSourceQuote>,
    val discrepancyNote: String = "Paridade equilibrada entre LigaMagic / LigaPokémon e mercados internacionais."
)

enum class MarketRegion(val code: String, val displayName: String, val flag: String, val currencyCode: String) {
    BRAZIL("BR", "Brasil (LigaMagic / LigaPokémon)", "🇧🇷", "BRL"),
    USA("US", "Estados Unidos (TCGPlayer)", "🇺🇸", "USD"),
    EUROPE("EU", "Europa (Cardmarket)", "🇪🇺", "EUR"),
    JAPAN("JP", "Japão (Yuyu-tei)", "🇯🇵", "JPY"),
    GLOBAL("GLOBAL", "Mercado Global Consolidado", "🌐", "USD")
}
