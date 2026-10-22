package com.example.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val COLLECTION = "collection"
    const val SCANNER = "scanner"
    const val SCAN_RESULT = "scan_result"
    const val STATISTICS = "statistics"
    const val ADD_ITEM = "add_item"
    const val ITEM_DETAIL = "item_detail/{itemId}"
    const val EDIT_ITEM = "edit_item/{itemId}"
    const val DECKS_LIST = "decks_list"
    const val DECK_DETAIL = "deck_detail/{deckId}"
    const val WISHLIST = "wishlist"
    const val MARKETPLACE = "marketplace"
    const val COMPARE = "compare"
    const val ACHIEVEMENTS = "achievements"

    // Novas Ferramentas solicitadas das Screenshots:
    const val MISSING_CARDS = "missing_cards" // O Que Falta? (Checklist por Set)
    const val PHYSICAL_INVENTORY = "physical_inventory" // Inventário Físico (Pastas & Caixas)
    const val DUPLICATES = "duplicates" // Duplicatas (Controle de Cópias)
    const val AI_ASSISTANT = "ai_assistant" // Assistente IA (Perguntas da Coleção)
    const val PRICE_ALERTS = "price_alerts" // Alertas de Preço (Detector Suspeito)
    const val PATRIMONY_BACKUP = "patrimony_backup" // Patrimônio & Backup (PIN, CSV & PDF)
    const val SEALED_PRODUCTS = "sealed_products" // Produtos Selados (Decks MTG & Preços)
    const val EFFECTS_GUIDE = "effects_guide" // Guia de Efeitos (Traduzir Tudo PT-BR)
    const val IMPORT_CATALOG = "import_catalog" // Importar Catálogo (PDF, Excel, CSV)
    const val OFFICIAL_CATALOG = "official_catalog" // Catálogo Oficial (Preços & Sets)
    const val SETTINGS = "settings" // Configurações & Chave de IA

    fun itemDetail(itemId: Int) = "item_detail/$itemId"
    fun editItem(itemId: Int) = "edit_item/$itemId"
    fun deckDetail(deckId: Int) = "deck_detail/$deckId"
}
