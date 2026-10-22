package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY dateAdded DESC")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Int): Item?

    @Query("SELECT * FROM items WHERE category = :category ORDER BY dateAdded DESC")
    fun getItemsByCategory(category: String): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<Item>)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
