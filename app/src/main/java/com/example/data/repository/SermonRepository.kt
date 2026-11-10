package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.DefaultSermons
import com.example.data.local.SermonDao
import com.example.data.model.SermonEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SermonRepository(private val database: AppDatabase) {
    private val dao: SermonDao = database.sermonDao()

    suspend fun checkAndSeedDefaults() {
        withContext(Dispatchers.IO) {
            if (dao.getCount() == 0) {
                dao.insertAll(DefaultSermons.getList())
            }
        }
    }

    fun getAllSermons(): Flow<List<SermonEntity>> = dao.getAllSermons()

    fun getSermonsByCategory(category: String): Flow<List<SermonEntity>> =
        if (category == "සියල්ල" || category.isBlank()) dao.getAllSermons()
        else dao.getSermonsByCategory(category)

    fun getFavoriteSermons(): Flow<List<SermonEntity>> = dao.getFavoriteSermons()

    fun getFeaturedSermon(): Flow<SermonEntity?> = dao.getFeaturedSermon()

    fun getSermonById(id: Long): Flow<SermonEntity?> = dao.getSermonById(id)

    fun searchSermons(query: String): Flow<List<SermonEntity>> = dao.searchSermons(query)

    suspend fun addSermon(sermon: SermonEntity): Long = withContext(Dispatchers.IO) {
        dao.insertSermon(sermon)
    }

    suspend fun updateSermon(sermon: SermonEntity) = withContext(Dispatchers.IO) {
        dao.updateSermon(sermon)
    }

    suspend fun deleteSermon(sermon: SermonEntity) = withContext(Dispatchers.IO) {
        dao.deleteSermon(sermon)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    suspend fun toggleFavorite(id: Long, currentFav: Boolean) = withContext(Dispatchers.IO) {
        dao.setFavorite(id, !currentFav)
    }

    suspend fun incrementListen(id: Long) = withContext(Dispatchers.IO) {
        dao.incrementListenCount(id)
    }

    suspend fun restoreDefaults() = withContext(Dispatchers.IO) {
        dao.insertAll(DefaultSermons.getList())
    }
}
