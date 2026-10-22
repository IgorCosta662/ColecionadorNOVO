package com.example.data

import kotlinx.coroutines.flow.Flow

class ItemRepository(private val database: AppDatabase) {
    val allItems: Flow<List<Item>> = database.itemDao().getAllItems()
    val favoriteItems: Flow<List<Item>> = database.itemDao().getFavoriteItems()
    val allWishlist: Flow<List<WishlistItem>> = database.wishlistDao().getAllWishlist()
    val allDecksWithCards: Flow<List<DeckWithCards>> = database.deckDao().getAllDecksWithCards()
    val allSealedProducts: Flow<List<SealedProduct>> = database.deckDao().getAllSealedProducts()

    suspend fun getItemById(id: Int): Item? = database.itemDao().getItemById(id)
    suspend fun insertItem(item: Item): Long = database.itemDao().insertItem(item)
    suspend fun insertAll(items: List<Item>) = database.itemDao().insertAll(items)
    suspend fun updateItem(item: Item) = database.itemDao().updateItem(item)
    suspend fun deleteItem(item: Item) = database.itemDao().deleteItem(item)
    suspend fun deleteAllItems() = database.itemDao().deleteAll()

    // Wishlist
    suspend fun insertWishlist(item: WishlistItem): Long = database.wishlistDao().insertWishlist(item)
    suspend fun updateWishlist(item: WishlistItem) = database.wishlistDao().updateWishlist(item)
    suspend fun deleteWishlist(item: WishlistItem) = database.wishlistDao().deleteWishlist(item)

    // Decks
    suspend fun getDeckWithCards(deckId: Int): DeckWithCards? = database.deckDao().getDeckWithCards(deckId)
    suspend fun insertDeck(deck: Deck): Long = database.deckDao().insertDeck(deck)
    suspend fun updateDeck(deck: Deck) = database.deckDao().updateDeck(deck)
    suspend fun deleteDeck(deck: Deck) = database.deckDao().deleteDeck(deck)
    suspend fun insertDeckCard(card: DeckCard): Long = database.deckDao().insertDeckCard(card)
    suspend fun insertDeckCards(cards: List<DeckCard>) = database.deckDao().insertDeckCards(cards)
    suspend fun updateDeckCard(card: DeckCard) = database.deckDao().updateDeckCard(card)
    suspend fun deleteDeckCard(card: DeckCard) = database.deckDao().deleteDeckCard(card)

    // Sealed Products
    suspend fun insertSealedProduct(p: SealedProduct): Long = database.deckDao().insertSealedProduct(p)
    suspend fun updateSealedProduct(p: SealedProduct) = database.deckDao().updateSealedProduct(p)
    suspend fun deleteSealedProduct(p: SealedProduct) = database.deckDao().deleteSealedProduct(p)
}
