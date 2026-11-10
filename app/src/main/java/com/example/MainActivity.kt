package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.SermonEntity
import com.example.ui.components.AdminAddEditDialog
import com.example.ui.components.MiniPlayer
import com.example.ui.components.TopDhammaBar
import com.example.ui.screens.AdminManagementScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FullScreenPlayerScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.SermonViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BuddhistSermonApp()
            }
        }
    }
}

@Composable
fun BuddhistSermonApp(viewModel: SermonViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val allSermons by viewModel.allSermons.collectAsState()
    val filteredSermons by viewModel.filteredSermons.collectAsState()
    val favoriteSermons by viewModel.favoriteSermons.collectAsState()
    val featuredSermon by viewModel.featuredSermon.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSermon by viewModel.currentSermon.collectAsState()

    // Player state
    val playerState by viewModel.audioPlayer.playerState.collectAsState()
    val currentPosMs by viewModel.audioPlayer.currentPositionMs.collectAsState()
    val durationMs by viewModel.audioPlayer.durationMs.collectAsState()
    val playbackSpeed by viewModel.audioPlayer.playbackSpeed.collectAsState()
    val sleepTimerSeconds by viewModel.audioPlayer.sleepTimerSeconds.collectAsState()

    // Admin state
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsState()
    val sermonToEdit by viewModel.sermonToEdit.collectAsState()
    val showAddEditDialog by viewModel.showAddEditDialog.collectAsState()
    val adminPinInput by viewModel.adminPinInput.collectAsState()
    val adminPinError by viewModel.adminPinError.collectAsState()
    val userFeedbackMessage by viewModel.userFeedbackMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isCloudSyncing by viewModel.isCloudSyncing.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        com.example.auth.AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { viewModel.syncFromCloud() },
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userFeedbackMessage) {
        userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (currentScreen == AppScreen.LIBRARY || currentScreen == AppScreen.FAVORITES) {
                TopDhammaBar(
                    currentScreen = currentScreen,
                    isAdminUnlocked = isAdminUnlocked,
                    favoritesCount = favoriteSermons.size,
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.updateSearchQuery(it) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onOpenAdmin = { viewModel.navigateTo(AppScreen.ADMIN) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.LIBRARY -> {
                    LibraryScreen(
                        featuredSermon = featuredSermon,
                        sermons = filteredSermons,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        isCloudSyncing = isCloudSyncing,
                        currentSermon = currentSermon,
                        playerState = playerState,
                        isAdminUnlocked = isAdminUnlocked,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onPlaySermon = { viewModel.playSermon(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onEditSermon = { viewModel.openEditSermonDialog(it) },
                        onDeleteSermon = { viewModel.deleteSermon(it) },
                        onSyncCloud = { viewModel.syncFromCloud() }
                    )
                }

                AppScreen.FAVORITES -> {
                    FavoritesScreen(
                        favoriteSermons = favoriteSermons,
                        currentSermon = currentSermon,
                        playerState = playerState,
                        isAdminUnlocked = isAdminUnlocked,
                        onPlaySermon = { viewModel.playSermon(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onEditSermon = { viewModel.openEditSermonDialog(it) },
                        onDeleteSermon = { viewModel.deleteSermon(it) },
                        onBack = { viewModel.navigateTo(AppScreen.LIBRARY) }
                    )
                }

                AppScreen.ADMIN -> {
                    AdminManagementScreen(
                        isAdminUnlocked = isAdminUnlocked,
                        currentUser = currentUser,
                        sermons = allSermons,
                        pinInput = adminPinInput,
                        pinError = adminPinError,
                        onPinChange = { viewModel.setAdminPinInput(it) },
                        onVerifyPin = { viewModel.verifyAdminPin(it) },
                        onLockAdmin = { viewModel.lockAdmin() },
                        onAddNewSermon = { viewModel.openAddSermonDialog() },
                        onEditSermon = { viewModel.openEditSermonDialog(it) },
                        onDeleteSermon = { viewModel.deleteSermon(it) },
                        onRestoreDefaults = { viewModel.restoreDefaults() },
                        onSignOut = {
                            com.example.auth.AuthManager.signOut(credentialManager, coroutineScope)
                        },
                        onBack = { viewModel.navigateTo(AppScreen.LIBRARY) }
                    )
                }

                AppScreen.PLAYER -> {
                    currentSermon?.let { sermon ->
                        FullScreenPlayerScreen(
                            sermon = sermon,
                            playerState = playerState,
                            currentPosMs = currentPosMs,
                            durationMs = durationMs,
                            playbackSpeed = playbackSpeed,
                            sleepTimerSeconds = sleepTimerSeconds,
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onSeekTo = { viewModel.audioPlayer.seekTo(it) },
                            onSeekRelative = { viewModel.audioPlayer.seekRelative(it) },
                            onSetSpeed = { viewModel.audioPlayer.setPlaybackSpeed(it) },
                            onSetSleepTimer = { viewModel.audioPlayer.setSleepTimer(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(sermon) },
                            onBack = { viewModel.navigateTo(AppScreen.LIBRARY) }
                        )
                    } ?: run {
                        // Fallback if no sermon is selected
                        LaunchedEffect(Unit) {
                            viewModel.navigateTo(AppScreen.LIBRARY)
                        }
                    }
                }
            }

            // Floating MiniPlayer at bottom of Library or Favorites screens
            if ((currentScreen == AppScreen.LIBRARY || currentScreen == AppScreen.FAVORITES) && currentSermon != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                ) {
                    MiniPlayer(
                        currentSermon = currentSermon,
                        playerState = playerState,
                        currentPosMs = currentPosMs,
                        durationMs = durationMs,
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onExpandPlayer = { viewModel.navigateTo(AppScreen.PLAYER) }
                    )
                }
            }
        }
    }

    // Admin Add/Edit Dialog
    if (showAddEditDialog) {
        AdminAddEditDialog(
            sermonToEdit = sermonToEdit,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { title, thero, category, audioUrl, videoUrl, durationMin, desc, pali, sinhala, isFeatured ->
                viewModel.saveSermon(
                    title = title,
                    thero = thero,
                    category = category,
                    audioUrl = audioUrl,
                    videoUrl = videoUrl,
                    durationMinutes = durationMin,
                    description = desc,
                    paliVerses = pali,
                    sinhalaMeaning = sinhala,
                    isFeatured = isFeatured
                )
            }
        )
    }
}
