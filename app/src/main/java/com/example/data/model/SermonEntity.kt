package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sermons")
data class SermonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val thero: String,
    val category: String, // "මහා පිරිත්", "ධර්ම දේශනා", "භාවනා", "සෙත් පිරිත් සහ ගාථා"
    val audioUrl: String,
    val videoUrl: String = "",
    val durationSeconds: Int = 1800,
    val description: String = "",
    val paliVerses: String = "",
    val sinhalaMeaning: String = "",
    val isFeatured: Boolean = false,
    val listenCount: Int = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
