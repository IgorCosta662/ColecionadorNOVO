package com.example.data

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val isUnlocked: Boolean,
    val progress: Float,
    val current: Int,
    val target: Int
)
