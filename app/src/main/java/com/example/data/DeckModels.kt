package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val game: String, // "Magic: The Gathering", "Pokémon TCG", "Yu-Gi-Oh!"
    val format: String, // "Commander (EDH)", "Standard", "Modern", "Pauper", "GLC"
    val commanderOrLeader: String = "",
    val description: String = "",
    val coverImageUrl: String = "",
    val dateModified: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "deck_cards",
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class DeckCard(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deckId: Int,
    val name: String,
    val quantity: Int = 1,
    val category: String = "Mainboard", // "Commander", "Mainboard", "Sideboard"
    val cardType: String = "", // "Criatura", "Mágica Instantânea", "Terreno"
    val manaCost: String = "",
    val manaValue: Double = 0.0,
    val oracleText: String = "",
    val translatedEffectPt: String = "",
    val estimatedPriceBrl: Double = 0.0,
    val imageUrl: String = "",
    val isOwnedInCollection: Boolean = false
)

data class DeckWithCards(
    @Embedded val deck: Deck,
    @Relation(
        parentColumn = "id",
        entityColumn = "deckId"
    )
    val cards: List<DeckCard>
)

@Entity(tableName = "sealed_products")
data class SealedProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val game: String, // "Magic: The Gathering", "Pokémon TCG"
    val setOrEdition: String,
    val productType: String, // "Commander Deck", "Booster Box", "Bundle", "Elite Trainer Box"
    val releaseYear: Int = 2024,
    val purchasePrice: Double = 0.0,
    val currentMarketEstimate: Double = 0.0,
    val decklistCount: Int = 100,
    val isOpened: Boolean = false,
    val notes: String = "",
    val imageUrl: String = ""
)

@Dao
interface DeckDao {
    @Transaction
    @Query("SELECT * FROM decks ORDER BY dateModified DESC")
    fun getAllDecksWithCards(): Flow<List<DeckWithCards>>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :deckId LIMIT 1")
    suspend fun getDeckWithCards(deckId: Int): DeckWithCards?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck): Long

    @Update
    suspend fun updateDeck(deck: Deck)

    @Delete
    suspend fun deleteDeck(deck: Deck)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeckCard(card: DeckCard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeckCards(cards: List<DeckCard>)

    @Update
    suspend fun updateDeckCard(card: DeckCard)

    @Delete
    suspend fun deleteDeckCard(card: DeckCard)

    @Query("DELETE FROM deck_cards WHERE deckId = :deckId")
    suspend fun deleteCardsForDeck(deckId: Int)

    // Sealed Products
    @Query("SELECT * FROM sealed_products ORDER BY id DESC")
    fun getAllSealedProducts(): Flow<List<SealedProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSealedProduct(product: SealedProduct): Long

    @Update
    suspend fun updateSealedProduct(product: SealedProduct)

    @Delete
    suspend fun deleteSealedProduct(product: SealedProduct)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist ORDER BY id DESC")
    fun getAllWishlist(): Flow<List<WishlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistItem): Long

    @Update
    suspend fun updateWishlist(item: WishlistItem)

    @Delete
    suspend fun deleteWishlist(item: WishlistItem)
}
