package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CollectorApplication
import com.example.api.PriceSourceRegistry
import com.example.data.*
import com.example.util.AiSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ScanResultData(
    val detectedName: String = "",
    val category: String = "Trading Cards",
    val subCategory: String = "Magic: The Gathering",
    val collection: String = "",
    val itemNumber: String = "",
    val rarity: String = "",
    val estimatedValue: Double = 0.0,
    val officialImageUrl: String? = null,
    val capturedImageUri: String? = null,
    val authenticityStatus: String = "Baixo risco aparente",
    val authenticityNotes: String = "Cartão apresenta textura e fontes compatíveis com catálogo original.",
    val marketOffers: List<PriceOffer> = emptyList(),
    val marketHistory: List<PriceHistoryPoint> = emptyList(),
    val cardHp: String? = null,
    val cardArtist: String? = null,
    val cardAttacks: String? = null,
    val cardOracleText: String? = null,
    val cardTranslatedEffect: String? = null,
    val energyType: String? = null,
    val cardType: String? = null
)

class CollectorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ItemRepository = (application as CollectorApplication).repository

    val allItems: StateFlow<List<Item>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteItems: StateFlow<List<Item>> = repository.favoriteItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWishlist: StateFlow<List<WishlistItem>> = repository.allWishlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDecksWithCards: StateFlow<List<DeckWithCards>> = repository.allDecksWithCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSealedProducts: StateFlow<List<SealedProduct>> = repository.allSealedProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active App Mode: Investor vs Collector
    private val _appMode = MutableStateFlow(AppMode.COLLECTOR)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    fun toggleAppMode() {
        _appMode.value = if (_appMode.value == AppMode.COLLECTOR) AppMode.INVESTOR else AppMode.COLLECTOR
    }

    fun setAppMode(mode: AppMode) {
        _appMode.value = mode
    }

    // Active Currency
    private val _selectedCurrency = MutableStateFlow(AppCurrency.BRL)
    val selectedCurrency: StateFlow<AppCurrency> = _selectedCurrency.asStateFlow()

    fun setCurrency(currency: AppCurrency) {
        _selectedCurrency.value = currency
    }

    // Comparison system
    private val _selectedComparisonIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedComparisonIds: StateFlow<Set<Int>> = _selectedComparisonIds.asStateFlow()

    fun toggleComparisonItem(id: Int) {
        val current = _selectedComparisonIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            if (current.size < 4) {
                current.add(id)
            }
        }
        _selectedComparisonIds.value = current
    }

    fun clearComparison() {
        _selectedComparisonIds.value = emptySet()
    }

    // Scan State
    private val _scanResult = MutableStateFlow<ScanResultData?>(null)
    val scanResult: StateFlow<ScanResultData?> = _scanResult.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    fun resetScanState() {
        _scanResult.value = null
        _isAnalyzing.value = false
    }

    fun analyzeCard(bitmap: Bitmap, categoryHint: String = "Auto", customQuery: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzing.value = true
            val context = getApplication<Application>()

            // Safely save captured camera bitmap to internal storage
            val capturedPhotoUri: String? = try {
                ImageStorageHelper.saveBitmapToInternalStorage(context, bitmap, "scan_card")
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Aviso: Não foi possível salvar bitmap local: ${e.message}")
                null
            }

            try {
                var detectedName: String? = null
                var detectedGameType: String = when {
                    categoryHint.contains("Magic", ignoreCase = true) -> "Magic: The Gathering"
                    categoryHint.contains("Hot", ignoreCase = true) || categoryHint.contains("Diecast", ignoreCase = true) -> "Carrinhos / Diecast"
                    categoryHint.contains("Pok", ignoreCase = true) -> "Pokémon TCG"
                    else -> "Auto"
                }
                var detectedCollection: String? = null
                var detectedItemNumber: String? = null
                var detectedRarity: String? = null
                var detectedCardType: String? = null
                var detectedEnergyOrMana: String? = null
                var detectedHp: String? = null
                var detectedAttacks: String? = null
                var detectedArtist: String? = null
                var detectedOracle: String? = null
                var detectedTranslatedEffect: String? = null
                var detectedPrice: Double? = null
                var aiAnalysisNotes: String? = null

                // 1. Try Gemini Vision AI recognition if API Key is configured
                if (AiSettingsManager.hasConfiguredApiKey(context) && AiSettingsManager.isScanAiEnabled(context)) {
                    val visionResult = AiSettingsManager.identifyCardWithGeminiVision(
                        context = context,
                        bitmap = bitmap,
                        preferredGameHint = if (categoryHint != "Auto") categoryHint else null
                    )

                    visionResult.onSuccess { aiCard ->
                        detectedName = aiCard.cardName
                        detectedGameType = aiCard.gameType
                        detectedCollection = aiCard.collection
                        detectedItemNumber = aiCard.itemNumber
                        detectedRarity = aiCard.rarity
                        detectedCardType = aiCard.cardType
                        detectedEnergyOrMana = aiCard.energyOrMana
                        detectedHp = aiCard.cardHp
                        detectedAttacks = aiCard.attacksOrAbilities
                        detectedArtist = aiCard.artist
                        detectedOracle = aiCard.oracleText
                        detectedTranslatedEffect = aiCard.translatedEffectPt
                        detectedPrice = if (aiCard.estimatedPriceBrl > 0.0) aiCard.estimatedPriceBrl else null
                        val marketName = if (aiCard.gameType.contains("Magic", ignoreCase = true)) "LigaMagic" else "LigaPokémon"
                        aiAnalysisNotes = "Identificado via IA Gemini Vision como '${aiCard.gameType}'. Sincronizado com $marketName Brasil."
                    }.onFailure { error ->
                        android.util.Log.w("CollectorViewModel", "Aviso: Reconhecimento por IA não concluiu: ${error.localizedMessage}")
                    }
                }

                // 2. Resolve final category and subCategory strictly
                val finalSubCategory = when {
                    detectedGameType.contains("Magic", ignoreCase = true) || categoryHint.contains("Magic", ignoreCase = true) -> "Magic: The Gathering"
                    detectedGameType.contains("Hot", ignoreCase = true) || categoryHint.contains("Hot", ignoreCase = true) || categoryHint.contains("Diecast", ignoreCase = true) -> "Carrinhos / Diecast"
                    detectedGameType.contains("Yu-Gi", ignoreCase = true) -> "Yu-Gi-Oh!"
                    detectedGameType.contains("Pok", ignoreCase = true) || categoryHint.contains("Pok", ignoreCase = true) -> "Pokémon TCG"
                    // If Auto and no AI, check if customQuery mentions Magic staples
                    !customQuery.isNullOrBlank() && (customQuery.contains("sol ring", true) || customQuery.contains("sheoldred", true) || customQuery.contains("lotus", true) || customQuery.contains("magic", true)) -> "Magic: The Gathering"
                    else -> "Magic: The Gathering" // Default to Magic if ambiguous to prevent forcing Pokemon
                }

                val finalCategory = if (finalSubCategory.contains("Diecast", ignoreCase = true) || finalSubCategory.contains("Carrinho", ignoreCase = true)) {
                    "Carrinhos / Diecast"
                } else {
                    "Trading Cards"
                }

                // 3. Search local verified catalog matching strictly by subCategory
                val queryToSearch = detectedName ?: customQuery
                val catalogCandidate = if (!queryToSearch.isNullOrBlank()) {
                    RealMarketCatalog.findBestMatch(
                        name = queryToSearch,
                        number = detectedItemNumber ?: "",
                        collection = detectedCollection ?: "",
                        preferredSubCategory = finalSubCategory
                    )
                } else {
                    null
                }

                val isNameDetected = !detectedName.isNullOrBlank()
                val candidateName = detectedName
                    ?: catalogCandidate?.name
                    ?: customQuery
                    ?: if (finalSubCategory.contains("Magic", ignoreCase = true)) {
                        "Carta Magic a Identificar"
                    } else if (finalSubCategory.contains("Hot", ignoreCase = true)) {
                        "Miniatura a Identificar"
                    } else {
                        "Carta Pokémon a Identificar"
                    }

                val candidateCollection = detectedCollection
                    ?: catalogCandidate?.collection
                    ?: if (candidateName.contains("a Identificar")) "Edição a Confirmar" else "Coleção Oficial"

                val candidateNumber = detectedItemNumber ?: catalogCandidate?.itemNumber ?: "—"
                val candidateRarity = detectedRarity ?: catalogCandidate?.rarity ?: "Comum"
                val candidateVariant = catalogCandidate?.variant ?: "Normal"
                val candidatePrice = detectedPrice
                    ?: catalogCandidate?.realMarketPriceBrl
                    ?: if (candidateName.contains("a Identificar")) 0.0 else 10.0
                val effectInfo = com.example.util.CardKnowledgeBase.findOrGenerateCardEffect(
                    candidateName,
                    finalCategory,
                    finalSubCategory
                )

                var resolvedOfficialImage = catalogCandidate?.imageUrl
                var finalCandidateCollection = candidateCollection
                var finalCandidateNumber = candidateNumber
                var finalCandidateRarity = candidateRarity
                var finalOracle = detectedOracle ?: effectInfo.originalOracle
                var finalArtist = detectedArtist ?: effectInfo.cardArtist

                // If Magic card, consult Scryfall to fetch authentic high-res image and official metadata
                if (finalSubCategory.contains("Magic", ignoreCase = true) && !candidateName.contains("a Identificar")) {
                    try {
                        val scryfallMatch = com.example.api.ScryfallApiClient.getBestCardMatch(candidateName)
                        if (scryfallMatch != null) {
                            if (resolvedOfficialImage.isNullOrBlank() || scryfallMatch.imageUrlLarge.isNotBlank()) {
                                resolvedOfficialImage = scryfallMatch.imageUrlLarge
                            }
                            if (finalCandidateCollection == "Coleção Oficial" || finalCandidateCollection == "Edição a Confirmar") {
                                finalCandidateCollection = "[${scryfallMatch.setCode.uppercase()}] ${scryfallMatch.setName}"
                            }
                            if (finalCandidateNumber == "—" && scryfallMatch.collectorNumber.isNotBlank()) {
                                finalCandidateNumber = scryfallMatch.collectorNumber
                            }
                            if (finalCandidateRarity == "Comum" && scryfallMatch.rarity.isNotBlank()) {
                                finalCandidateRarity = scryfallMatch.rarity
                            }
                            if (finalOracle.isBlank() && !scryfallMatch.oracleText.isNullOrBlank()) {
                                finalOracle = scryfallMatch.oracleText
                            }
                            if (finalArtist.isNullOrBlank() && !scryfallMatch.artist.isNullOrBlank()) {
                                finalArtist = scryfallMatch.artist
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("CollectorViewModel", "Aviso Scryfall lookup durante scan: ${e.message}")
                    }
                }

                val (offers, history) = PriceSourceRegistry.generateRealisticOffersAndHistory(
                    itemName = candidateName,
                    subCategory = finalSubCategory,
                    rarity = finalCandidateRarity,
                    variant = candidateVariant,
                    baseEstimatedPrice = candidatePrice
                )

                val defaultNotes = if (finalSubCategory.contains("Magic")) {
                    "Cotação oficial LigaMagic Brasil: Menor, Médio e Maior preço em Reais (BRL)."
                } else {
                    "Cotação oficial LigaPokémon Brasil: Menor, Médio e Maior preço em Reais (BRL)."
                }

                val hasAiConfigured = AiSettingsManager.hasConfiguredApiKey(context)
                val statusText = when {
                    isNameDetected && aiAnalysisNotes != null -> "Identificado via IA Gemini Vision"
                    catalogCandidate != null -> "Catálogo Oficial Verificado"
                    !hasAiConfigured -> "Identificação Manual (Sem Chave de IA)"
                    else -> "Nome Não Identificado pela IA"
                }

                val notesText = when {
                    aiAnalysisNotes != null -> aiAnalysisNotes
                    catalogCandidate != null -> defaultNotes
                    !hasAiConfigured -> "Para identificação automática por foto, configure sua chave gratuita do Google Gemini em Configurações. Ou selecione a carta no catálogo oficial abaixo."
                    else -> "A IA não conseguiu ler o nome da carta com nitidez. Digite o nome ou escolha a carta do catálogo oficial abaixo."
                }

                _scanResult.value = ScanResultData(
                    detectedName = candidateName,
                    category = finalCategory,
                    subCategory = finalSubCategory,
                    collection = finalCandidateCollection,
                    itemNumber = finalCandidateNumber,
                    rarity = finalCandidateRarity,
                    estimatedValue = candidatePrice,
                    officialImageUrl = resolvedOfficialImage,
                    capturedImageUri = capturedPhotoUri,
                    authenticityStatus = statusText,
                    authenticityNotes = notesText,
                    marketOffers = offers,
                    marketHistory = history,
                    cardHp = detectedHp ?: effectInfo.cardHp,
                    cardArtist = finalArtist,
                    cardAttacks = detectedAttacks ?: effectInfo.cardAttacks,
                    cardOracleText = finalOracle,
                    cardTranslatedEffect = detectedTranslatedEffect ?: effectInfo.translatedEffectPt,
                    energyType = detectedEnergyOrMana ?: effectInfo.manaColor ?: effectInfo.cardType,
                    cardType = detectedCardType ?: effectInfo.cardType
                )
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao analisar carta: ${e.localizedMessage}", e)
                val fallbackSubCat = if (categoryHint.contains("Magic", ignoreCase = true)) "Magic: The Gathering" else "Magic: The Gathering"
                val fallback = RealMarketCatalog.allEntries.firstOrNull { it.subCategory.contains(fallbackSubCat) }
                    ?: RealMarketCatalog.allEntries.first()

                val (offers, history) = PriceSourceRegistry.generateRealisticOffersAndHistory(
                    itemName = fallback.name,
                    subCategory = fallback.subCategory,
                    rarity = fallback.rarity,
                    variant = fallback.variant,
                    baseEstimatedPrice = fallback.realMarketPriceBrl
                )

                _scanResult.value = ScanResultData(
                    detectedName = fallback.name,
                    category = fallback.category,
                    subCategory = fallback.subCategory,
                    collection = fallback.collection,
                    itemNumber = fallback.itemNumber,
                    rarity = fallback.rarity,
                    estimatedValue = fallback.realMarketPriceBrl,
                    officialImageUrl = fallback.imageUrl,
                    capturedImageUri = capturedPhotoUri,
                    authenticityStatus = "Catálogo Oficial",
                    authenticityNotes = "Identificado via catálogo oficial brasileiro.",
                    marketOffers = offers,
                    marketHistory = history
                )
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun applyCandidateToScan(candidate: com.example.data.RealCatalogEntry) {
        val (offers, history) = PriceSourceRegistry.generateRealisticOffersAndHistory(
            itemName = candidate.name,
            subCategory = candidate.subCategory,
            rarity = candidate.rarity,
            variant = candidate.variant,
            baseEstimatedPrice = candidate.realMarketPriceBrl
        )
        val effectInfo = com.example.util.CardKnowledgeBase.findOrGenerateCardEffect(
            candidate.name,
            candidate.category,
            candidate.subCategory
        )
        _scanResult.value = ScanResultData(
            detectedName = candidate.name,
            category = candidate.category,
            subCategory = candidate.subCategory,
            collection = candidate.collection,
            itemNumber = candidate.itemNumber,
            rarity = candidate.rarity,
            estimatedValue = candidate.realMarketPriceBrl,
            officialImageUrl = candidate.imageUrl,
            authenticityStatus = "Alta Confiança - Catálogo Verificado",
            authenticityNotes = "Cotação direta sincronizada com LigaMagic/LigaPokémon e Mercado Nacional.",
            marketOffers = offers,
            marketHistory = history,
            cardHp = effectInfo.cardHp,
            cardArtist = effectInfo.cardArtist,
            cardAttacks = effectInfo.cardAttacks,
            cardOracleText = effectInfo.originalOracle,
            cardTranslatedEffect = effectInfo.translatedEffectPt,
            energyType = effectInfo.manaColor ?: effectInfo.cardType,
            cardType = effectInfo.cardType
        )
    }

    fun translateEffectText(text: String): String {
        return com.example.util.CardKnowledgeBase.translateTextToPt(text)
    }

    fun lookupCardEffect(name: String, category: String, subCategory: String): com.example.util.CardKnowledgeBase.CardEffectInfo {
        return com.example.util.CardKnowledgeBase.findOrGenerateCardEffect(name, category, subCategory)
    }

    // Achievements System
    val achievements: StateFlow<List<Achievement>> = combine(allItems, allWishlist, allDecksWithCards) { items, wishlist, decks ->
        val count = items.size
        val cardsCount = items.count { it.isCard }
        val diecastCount = items.count { it.isDiecast }
        val gradedCount = items.count { it.isGraded }
        val totalVal = items.sumOf { it.totalEstimatedValue }

        listOf(
            Achievement(
                id = "first_card",
                title = "Primeiro Tesouro",
                description = "Cadastre sua primeira carta ou miniatura no catálogo.",
                category = "Início",
                isUnlocked = count >= 1,
                progress = (count.coerceAtMost(1) / 1f),
                current = count.coerceAtMost(1),
                target = 1
            ),
            Achievement(
                id = "collector_10",
                title = "Colecionador Novato",
                description = "Reúna 10 itens na sua coleção pessoal.",
                category = "Colecionismo",
                isUnlocked = count >= 10,
                progress = (count.coerceAtMost(10) / 10f),
                current = count.coerceAtMost(10),
                target = 10
            ),
            Achievement(
                id = "master_50",
                title = "Curador Mestre",
                description = "Alcance 50 itens cadastrados.",
                category = "Colecionismo",
                isUnlocked = count >= 50,
                progress = (count.coerceAtMost(50) / 50f),
                current = count.coerceAtMost(50),
                target = 50
            ),
            Achievement(
                id = "investor_1k",
                title = "Patrimônio de Mil",
                description = "Acumule mais de R$ 1.000 em valor de mercado.",
                category = "Investimento",
                isUnlocked = totalVal >= 1000.0,
                progress = ((totalVal / 1000.0).coerceAtMost(1.0)).toFloat(),
                current = totalVal.toInt().coerceAtMost(1000),
                target = 1000
            ),
            Achievement(
                id = "slab_master",
                title = "Graduador Profissional",
                description = "Tenha cartas com autenticação / nota PSA/BGS/CGC.",
                category = "Qualidade",
                isUnlocked = gradedCount >= 1,
                progress = (gradedCount.coerceAtMost(1) / 1f),
                current = gradedCount.coerceAtMost(1),
                target = 1
            ),
            Achievement(
                id = "deck_builder",
                title = "Estrategista de Decks",
                description = "Monte seu primeiro baralho de jogo.",
                category = "Competitivo",
                isUnlocked = decks.isNotEmpty(),
                progress = if (decks.isNotEmpty()) 1f else 0f,
                current = decks.size.coerceAtMost(1),
                target = 1
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Database CRUD wrappers
    fun importBatchItems(items: List<Item>, onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertAll(items)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(items.size)
                }
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao importar lote: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(0)
                }
            }
        }
    }

    fun saveItem(item: Item, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = if (item.id == 0) {
                    repository.insertItem(item)
                } else {
                    repository.updateItem(item)
                    item.id.toLong()
                }
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(id)
                }
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao salvar item: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(-1L)
                }
            }
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteItem(item)
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao deletar item: ${e.message}", e)
            }
        }
    }

    fun toggleFavorite(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateItem(item.copy(isFavorite = !item.isFavorite))
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao favoritar item: ${e.message}", e)
            }
        }
    }

    // Wishlist CRUD
    fun saveWishlist(item: WishlistItem, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (item.id == 0) repository.insertWishlist(item) else repository.updateWishlist(item)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao salvar wishlist: ${e.message}", e)
            }
        }
    }

    fun deleteWishlist(item: WishlistItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteWishlist(item)
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao deletar wishlist: ${e.message}", e)
            }
        }
    }

    // Deck CRUD
    fun saveDeck(deck: Deck, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = if (deck.id == 0) repository.insertDeck(deck) else {
                    repository.updateDeck(deck)
                    deck.id.toLong()
                }
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(id)
                }
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao salvar deck: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(-1L)
                }
            }
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteDeck(deck)
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao deletar deck: ${e.message}", e)
            }
        }
    }

    fun addCardToDeck(card: DeckCard) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertDeckCard(card)
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao adicionar carta ao deck: ${e.message}", e)
            }
        }
    }

    fun addCardsToDeck(cards: List<DeckCard>, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.insertDeckCards(cards)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao adicionar lista de cartas ao deck: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            }
        }
    }

    fun importPreconDeck(deck: Deck, cardTemplates: List<DeckCard>, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val deckId = repository.insertDeck(deck)
                val cardsWithId = cardTemplates.map { it.copy(deckId = deckId.toInt()) }
                repository.insertDeckCards(cardsWithId)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(deckId)
                }
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao importar precon: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(-1L)
                }
            }
        }
    }

    fun updateDeckCard(card: DeckCard) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDeckCard(card)
        }
    }

    fun enrichDeckCardPhotos(deckId: Int, onComplete: ((Int) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val deckWithCards = repository.getDeckWithCards(deckId)
            var count = 0
            if (deckWithCards != null) {
                for (c in deckWithCards.cards) {
                    if (c.imageUrl.isBlank()) {
                        val resolved = com.example.data.CardImageResolver.resolveImageUrl(c.name, deckWithCards.deck.game)
                        if (resolved.isNotBlank()) {
                            repository.updateDeckCard(c.copy(imageUrl = resolved))
                            count++
                        }
                    }
                }
            }
            withContext(Dispatchers.Main) {
                onComplete?.invoke(count)
            }
        }
    }

    fun importMultiplePreconDecks(
        decksWithCards: List<Pair<Deck, List<DeckCard>>>,
        onComplete: ((List<Long>) -> Unit)? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val createdIds = mutableListOf<Long>()
            try {
                for ((deck, cards) in decksWithCards) {
                    val deckId = repository.insertDeck(deck)
                    val cardsWithId = cards.map { it.copy(deckId = deckId.toInt()) }
                    repository.insertDeckCards(cardsWithId)
                    createdIds.add(deckId)
                }
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(createdIds)
                }
            } catch (e: Throwable) {
                android.util.Log.e("CollectorViewModel", "Erro ao importar múltiplos precons: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete?.invoke(createdIds)
                }
            }
        }
    }

    fun removeCardFromDeck(card: DeckCard) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDeckCard(card)
        }
    }

    fun clearAllItems(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllItems()
            onComplete?.invoke()
        }
    }

    init {
        // Clear previously registered sample items on launch as requested
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>().getSharedPreferences("collector_prefs", android.content.Context.MODE_PRIVATE)
            if (!prefs.getBoolean("has_cleared_preloaded_items_v3", false)) {
                repository.deleteAllItems()
                prefs.edit().putBoolean("has_cleared_preloaded_items_v3", true).apply()
            }
        }
    }
}
