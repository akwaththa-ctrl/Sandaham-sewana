package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.model.FirestoreSermon
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreSermonRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth
    private val sermonsCollection = db.collection("sermons")

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeSermons(): Flow<List<FirestoreSermon>> {
        val path = "sermons"
        return sermonsCollection
            .snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreSermon::class.java) }
            .catch { error ->
                if (error is CancellationException) throw error
                handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .flowOn(Dispatchers.IO)
    }

    suspend fun addSermon(sermon: FirestoreSermon): String = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = sermonsCollection.document()
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "title" to sermon.title,
            "thero" to sermon.thero,
            "category" to sermon.category,
            "audioUrl" to sermon.audioUrl,
            "videoUrl" to sermon.videoUrl,
            "durationSeconds" to sermon.durationSeconds,
            "description" to sermon.description,
            "paliVerses" to sermon.paliVerses,
            "sinhalaMeaning" to sermon.sinhalaMeaning,
            "isFeatured" to sermon.isFeatured,
            "listenCount" to sermon.listenCount,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateSermon(sermon: FirestoreSermon) = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        require(sermon.id.isNotBlank()) { "Sermon ID must not be blank for update" }
        val docRef = sermonsCollection.document(sermon.id)
        val payload = hashMapOf<String, Any>(
            "userId" to uid,
            "title" to sermon.title,
            "thero" to sermon.thero,
            "category" to sermon.category,
            "audioUrl" to sermon.audioUrl,
            "videoUrl" to sermon.videoUrl,
            "durationSeconds" to sermon.durationSeconds,
            "description" to sermon.description,
            "paliVerses" to sermon.paliVerses,
            "sinhalaMeaning" to sermon.sinhalaMeaning,
            "isFeatured" to sermon.isFeatured,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.update(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteSermon(sermonId: String) = withContext(Dispatchers.IO) {
        val docRef = sermonsCollection.document(sermonId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun incrementListen(sermonId: String) = withContext(Dispatchers.IO) {
        val docRef = sermonsCollection.document(sermonId)
        try {
            docRef.update("listenCount", FieldValue.increment(1)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            // Non-critical, do not rethrow
        }
    }
}
