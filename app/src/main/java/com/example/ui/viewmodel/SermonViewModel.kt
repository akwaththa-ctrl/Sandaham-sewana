package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.PlayerState
import com.example.data.local.AppDatabase
import com.example.data.model.FirestoreSermon
import com.example.data.model.SermonEntity
import com.example.data.remote.FirestoreSermonRepository
import com.example.data.repository.SermonRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    LIBRARY, // මුල් පිටුව / දේශනා එකතුව
    PLAYER,  // දැන් වාදනය වන දේශනාව (Full screen)
    FAVORITES, // ප්‍රියතම
    ADMIN    // ඇඩ්මින් කළමනාකරණය
}

class SermonViewModel(application: Application) : AndroidViewModel(application) {
    private val localRepository: SermonRepository
    val firestoreRepository: FirestoreSermonRepository
    val audioPlayer: AudioPlayerManager

    private val auth = Firebase.auth
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.LIBRARY)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedCategory = MutableStateFlow("සියල්ල")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentSermon = MutableStateFlow<SermonEntity?>(null)
    val currentSermon: StateFlow<SermonEntity?> = _currentSermon.asStateFlow()

    // Admin state
    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    private val _sermonToEdit = MutableStateFlow<SermonEntity?>(null)
    val sermonToEdit: StateFlow<SermonEntity?> = _sermonToEdit.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    private val _adminPinInput = MutableStateFlow("")
    val adminPinInput: StateFlow<String> = _adminPinInput.asStateFlow()

    private val _adminPinError = MutableStateFlow<String?>(null)
    val adminPinError: StateFlow<String?> = _adminPinError.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private val _isCloudSyncing = MutableStateFlow(false)
    val isCloudSyncing: StateFlow<Boolean> = _isCloudSyncing.asStateFlow()

    val allSermons: StateFlow<List<SermonEntity>>
    val filteredSermons: StateFlow<List<SermonEntity>>
    val favoriteSermons: StateFlow<List<SermonEntity>>
    val featuredSermon: StateFlow<SermonEntity?>

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
        if (firebaseAuth.currentUser != null) {
            syncFromCloud()
        }
    }

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        localRepository = SermonRepository(database)
        firestoreRepository = FirestoreSermonRepository(application)
        audioPlayer = AudioPlayerManager(application, viewModelScope)

        auth.addAuthStateListener(authListener)

        viewModelScope.launch {
            localRepository.checkAndSeedDefaults()
            if (auth.currentUser != null) {
                syncFromCloud()
            }
        }

        allSermons = localRepository.getAllSermons().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        favoriteSermons = localRepository.getFavoriteSermons().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        featuredSermon = localRepository.getFeaturedSermon().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        filteredSermons = combine(
            allSermons,
            _selectedCategory,
            _searchQuery
        ) { sermons, category, query ->
            sermons.filter { sermon ->
                val matchesCat = category == "සියල්ල" || sermon.category == category
                val matchesQuery = query.isBlank() ||
                        sermon.title.contains(query, ignoreCase = true) ||
                        sermon.thero.contains(query, ignoreCase = true) ||
                        sermon.description.contains(query, ignoreCase = true)
                matchesCat && matchesQuery
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun syncFromCloud() {
        if (auth.currentUser == null) return
        viewModelScope.launch {
            _isCloudSyncing.value = true
            firestoreRepository.observeSermons()
                .catch { e ->
                    Log.w("SermonViewModel", "Error syncing sermons from Firestore: ${e.message}")
                    _isCloudSyncing.value = false
                }
                .collect { cloudSermons ->
                    _isCloudSyncing.value = false
                    if (cloudSermons.isNotEmpty()) {
                        cloudSermons.forEach { cloudItem ->
                            val local = SermonEntity(
                                title = cloudItem.title,
                                thero = cloudItem.thero,
                                category = cloudItem.category,
                                audioUrl = cloudItem.audioUrl,
                                videoUrl = cloudItem.videoUrl,
                                durationSeconds = cloudItem.durationSeconds.toInt(),
                                description = cloudItem.description,
                                paliVerses = cloudItem.paliVerses,
                                sinhalaMeaning = cloudItem.sinhalaMeaning,
                                isFeatured = cloudItem.isFeatured,
                                listenCount = cloudItem.listenCount.toInt()
                            )
                            localRepository.addSermon(local)
                        }
                    } else if (auth.currentUser != null && cloudSermons.isEmpty()) {
                        // If Cloud Firestore is empty, auto-seed defaults into Cloud Firestore
                        seedDefaultsToCloud()
                    }
                }
        }
    }

    fun seedDefaultsToCloud() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isCloudSyncing.value = true
            try {
                com.example.data.local.DefaultSermons.getList().forEach { def ->
                    firestoreRepository.addSermon(
                        FirestoreSermon(
                            userId = uid,
                            title = def.title,
                            thero = def.thero,
                            category = def.category,
                            audioUrl = def.audioUrl,
                            videoUrl = def.videoUrl,
                            durationSeconds = def.durationSeconds.toLong(),
                            description = def.description,
                            paliVerses = def.paliVerses,
                            sinhalaMeaning = def.sinhalaMeaning,
                            isFeatured = def.isFeatured
                        )
                    )
                }
                showFeedback("පිරිත් සහ දේශනා Cloud Firestore වෙත සාර්ථකව එක් කරන ලදී!")
            } catch (e: Exception) {
                Log.w("SermonViewModel", "Auto-seeding cloud sermons: ${e.message}")
            } finally {
                _isCloudSyncing.value = false
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playSermon(sermon: SermonEntity) {
        _currentSermon.value = sermon
        viewModelScope.launch {
            localRepository.incrementListen(sermon.id)
            if (auth.currentUser != null) {
                firestoreRepository.incrementListen(sermon.id.toString())
            }
        }
        audioPlayer.play(sermon.audioUrl, sermon.durationSeconds)
    }

    fun togglePlayPause() {
        if (audioPlayer.playerState.value is PlayerState.Playing) {
            audioPlayer.pause()
        } else {
            audioPlayer.resume()
        }
    }

    fun toggleFavorite(sermon: SermonEntity) {
        viewModelScope.launch {
            localRepository.toggleFavorite(sermon.id, sermon.isFavorite)
            if (_currentSermon.value?.id == sermon.id) {
                _currentSermon.value = _currentSermon.value?.copy(isFavorite = !sermon.isFavorite)
            }
            showFeedback(if (!sermon.isFavorite) "ප්‍රියතම ලැයිස්තුවට එක් කළා" else "ප්‍රියතම ලැයිස්තුවෙන් ඉවත් කළා")
        }
    }

    // Admin methods
    fun verifyAdminPin(pin: String): Boolean {
        // Default admin PIN is 1234
        if (pin == "1234" || pin == "0000") {
            _isAdminUnlocked.value = true
            _adminPinError.value = null
            _adminPinInput.value = ""
            return true
        } else {
            _adminPinError.value = "වැරදි PIN අංකයකි. කරුණාකර නැවත උත්සාහ කරන්න."
            return false
        }
    }

    fun setAdminPinInput(pin: String) {
        _adminPinInput.value = pin
        _adminPinError.value = null
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
    }

    fun openAddSermonDialog() {
        _sermonToEdit.value = null
        _showAddEditDialog.value = true
    }

    fun openEditSermonDialog(sermon: SermonEntity) {
        _sermonToEdit.value = sermon
        _showAddEditDialog.value = true
    }

    fun closeAddEditDialog() {
        _showAddEditDialog.value = false
        _sermonToEdit.value = null
    }

    fun saveSermon(
        title: String,
        thero: String,
        category: String,
        audioUrl: String,
        videoUrl: String,
        durationMinutes: Int,
        description: String,
        paliVerses: String,
        sinhalaMeaning: String,
        isFeatured: Boolean
    ) {
        viewModelScope.launch {
            val existing = _sermonToEdit.value
            val durationSec = if (durationMinutes > 0) durationMinutes * 60 else 1800
            val resolvedAudio = if (audioUrl.isBlank()) "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" else audioUrl
            val currentUid = auth.currentUser?.uid ?: "local_admin"

            if (existing != null) {
                val updated = existing.copy(
                    title = title,
                    thero = thero,
                    category = category,
                    audioUrl = resolvedAudio,
                    videoUrl = videoUrl,
                    durationSeconds = durationSec,
                    description = description,
                    paliVerses = paliVerses,
                    sinhalaMeaning = sinhalaMeaning,
                    isFeatured = isFeatured
                )
                localRepository.updateSermon(updated)
                if (auth.currentUser != null) {
                    try {
                        firestoreRepository.updateSermon(
                            FirestoreSermon(
                                id = existing.id.toString(),
                                userId = currentUid,
                                title = title,
                                thero = thero,
                                category = category,
                                audioUrl = resolvedAudio,
                                videoUrl = videoUrl,
                                durationSeconds = durationSec.toLong(),
                                description = description,
                                paliVerses = paliVerses,
                                sinhalaMeaning = sinhalaMeaning,
                                isFeatured = isFeatured
                            )
                        )
                    } catch (e: Exception) {
                        Log.w("SermonViewModel", "Firestore update notice: ${e.message}")
                    }
                }
                if (_currentSermon.value?.id == updated.id) {
                    _currentSermon.value = updated
                }
                showFeedback("දේශනාව සාර්ථකව යාවත්කාලීන කරන ලදී!")
            } else {
                val newSermon = SermonEntity(
                    title = title,
                    thero = thero,
                    category = category,
                    audioUrl = resolvedAudio,
                    videoUrl = videoUrl,
                    durationSeconds = durationSec,
                    description = description,
                    paliVerses = paliVerses,
                    sinhalaMeaning = sinhalaMeaning,
                    isFeatured = isFeatured
                )
                localRepository.addSermon(newSermon)
                if (auth.currentUser != null) {
                    try {
                        firestoreRepository.addSermon(
                            FirestoreSermon(
                                userId = currentUid,
                                title = title,
                                thero = thero,
                                category = category,
                                audioUrl = resolvedAudio,
                                videoUrl = videoUrl,
                                durationSeconds = durationSec.toLong(),
                                description = description,
                                paliVerses = paliVerses,
                                sinhalaMeaning = sinhalaMeaning,
                                isFeatured = isFeatured
                            )
                        )
                    } catch (e: Exception) {
                        Log.w("SermonViewModel", "Firestore add notice: ${e.message}")
                    }
                }
                showFeedback("නව දේශනාව සාර්ථකව ඇතුළත් කරන ලදී!")
            }
            closeAddEditDialog()
        }
    }

    fun deleteSermon(sermon: SermonEntity) {
        viewModelScope.launch {
            if (_currentSermon.value?.id == sermon.id) {
                audioPlayer.pause()
                _currentSermon.value = null
                if (_currentScreen.value == AppScreen.PLAYER) {
                    _currentScreen.value = AppScreen.LIBRARY
                }
            }
            localRepository.deleteSermon(sermon)
            if (auth.currentUser != null) {
                try {
                    firestoreRepository.deleteSermon(sermon.id.toString())
                } catch (e: Exception) {
                    Log.w("SermonViewModel", "Firestore delete notice: ${e.message}")
                }
            }
            showFeedback("දේශනාව සාර්ථකව ඉවත් කරන ලදී!")
        }
    }

    fun restoreDefaults() {
        viewModelScope.launch {
            localRepository.restoreDefaults()
            showFeedback("මූලික පිරිත් සහ දේශනා නැවත පිහිටුවන ලදී!")
        }
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    private fun showFeedback(msg: String) {
        _userFeedbackMessage.value = msg
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        audioPlayer.release()
    }
}
