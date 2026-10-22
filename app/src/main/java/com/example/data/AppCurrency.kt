package com.example.data

import java.text.NumberFormat
import java.util.Locale

enum class AppCurrency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val locale: Locale,
    val exchangeRateFromBrl: Double
) {
    BRL("BRL", "R$", "Real (R$ - LigaMagic / LigaPokémon)", Locale("pt", "BR"), 1.0),
    USD("USD", "US$", "Dólar Americano (US$ - TCGPlayer)", Locale.US, 0.175),
    EUR("EUR", "€", "Euro (€ - Cardmarket)", Locale.GERMANY, 0.160),
    JPY("JPY", "¥", "Iene Japonês (¥)", Locale.JAPAN, 26.5);

    fun formatValue(valueInBrl: Double): String {
        val converted = valueInBrl * exchangeRateFromBrl
        val formatter = NumberFormat.getCurrencyInstance(locale)
        return try {
            formatter.format(converted)
        } catch (_: Exception) {
            "$symbol ${String.format(Locale.US, "%.2f", converted)}"
        }
    }
}
