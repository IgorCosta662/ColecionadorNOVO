package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wishlist")
data class WishlistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val subCategory: String = "",
    val collection: String = "",
    val itemNumber: String = "",
    val targetMaxPrice: Double = 0.0,
    val currentMarketEstimate: Double = 0.0,
    val priority: String = "Alta", // "Alta", "Média", "Baixa", "Santo Graal"
    val isFound: Boolean = false,
    val notes: String = "",
    val imageUrl: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)
