package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SermonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SermonDao {
    @Query("SELECT * FROM sermons ORDER BY isFeatured DESC, createdAt DESC")
    fun getAllSermons(): Flow<List<SermonEntity>>

    @Query("SELECT * FROM sermons WHERE category = :category ORDER BY isFeatured DESC, createdAt DESC")
    fun getSermonsByCategory(category: String): Flow<List<SermonEntity>>

    @Query("SELECT * FROM sermons WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteSermons(): Flow<List<SermonEntity>>

    @Query("SELECT * FROM sermons WHERE isFeatured = 1 LIMIT 1")
    fun getFeaturedSermon(): Flow<SermonEntity?>

    @Query("SELECT * FROM sermons WHERE id = :id LIMIT 1")
    fun getSermonById(id: Long): Flow<SermonEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSermon(sermon: SermonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sermons: List<SermonEntity>)

    @Update
    suspend fun updateSermon(sermon: SermonEntity)

    @Delete
    suspend fun deleteSermon(sermon: SermonEntity)

    @Query("DELETE FROM sermons WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE sermons SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE sermons SET listenCount = listenCount + 1 WHERE id = :id")
    suspend fun incrementListenCount(id: Long)

    @Query("SELECT COUNT(*) FROM sermons")
    suspend fun getCount(): Int

    @Query("SELECT * FROM sermons WHERE title LIKE '%' || :query || '%' OR thero LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchSermons(query: String): Flow<List<SermonEntity>>
}
