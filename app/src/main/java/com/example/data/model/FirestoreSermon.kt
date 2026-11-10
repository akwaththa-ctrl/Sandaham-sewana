package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreSermon(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val thero: String = "",
    val category: String = "මහා පිරිත්",
    val audioUrl: String = "",
    val videoUrl: String = "",
    val durationSeconds: Long = 1800L,
    val description: String = "",
    val paliVerses: String = "",
    val sinhalaMeaning: String = "",
    val isFeatured: Boolean = false,
    val listenCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
