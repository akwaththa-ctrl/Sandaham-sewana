package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

sealed class PlayerState {
    object Idle : PlayerState()
    object Buffering : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    object Completed : PlayerState()
    data class Error(val message: String) : PlayerState()
}

class AudioPlayerManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var fallbackTrack: AudioTrack? = null
    private var isUsingFallback = false
    private var isPrepared = false

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _sleepTimerSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerSeconds: StateFlow<Int?> = _sleepTimerSeconds.asStateFlow()

    private var currentPlayingUrl: String? = null

    init {
        createMediaPlayer()
    }

    private fun createMediaPlayer() {
        releasePlayer()
        isPrepared = false
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener { mp ->
                    isPrepared = true
                    _playerState.value = PlayerState.Playing
                    val dur = mp.duration
                    if (dur > 0) {
                        _durationMs.value = dur.toLong()
                    }
                    applySpeed(_playbackSpeed.value)
                    try {
                        mp.start()
                    } catch (e: Exception) {
                        Log.w("AudioPlayerManager", "Failed to start playback on prepared: ${e.message}")
                    }
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _playerState.value = PlayerState.Completed
                    _currentPositionMs.value = _durationMs.value
                    stopProgressTracker()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("AudioPlayerManager", "MediaPlayer warning: what=$what, extra=$extra.")
                    isPrepared = false
                    // If streaming url fails or network drops, start peaceful Buddhist sound tone
                    playChimeFallback()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error creating MediaPlayer: ${e.message}")
        }
    }

    fun play(url: String, estimatedDurationSec: Int = 1200) {
        if (currentPlayingUrl == url && _playerState.value is PlayerState.Paused && isPrepared) {
            resume()
            return
        }

        currentPlayingUrl = url
        _playerState.value = PlayerState.Buffering
        _currentPositionMs.value = 0L
        _durationMs.value = estimatedDurationSec * 1000L

        stopProgressTracker()
        stopFallback()

        try {
            if (mediaPlayer == null) {
                createMediaPlayer()
            } else {
                isPrepared = false
                try {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.stop()
                    }
                } catch (ignored: Exception) {}
                try {
                    mediaPlayer?.reset()
                } catch (ignored: Exception) {}
            }

            mediaPlayer?.apply {
                setDataSource(url)
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerManager", "Error setting data source: ${e.message}")
            playChimeFallback()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _playerState.value = PlayerState.Paused
                stopProgressTracker()
            } else if (isUsingFallback) {
                stopFallback()
                _playerState.value = PlayerState.Paused
                stopProgressTracker()
            }
        } catch (e: Exception) {
            _playerState.value = PlayerState.Paused
        }
    }

    fun resume() {
        try {
            if (isUsingFallback) {
                _playerState.value = PlayerState.Playing
                startProgressTracker()
            } else if (isPrepared && mediaPlayer != null) {
                mediaPlayer?.start()
                _playerState.value = PlayerState.Playing
                startProgressTracker()
            } else if (currentPlayingUrl != null) {
                play(currentPlayingUrl!!)
            }
        } catch (e: Exception) {
            _playerState.value = PlayerState.Playing
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            _currentPositionMs.value = positionMs
            if (!isUsingFallback && isPrepared && mediaPlayer != null) {
                mediaPlayer?.seekTo(positionMs.toInt())
            }
        } catch (e: Exception) {
            // ignore seek errors
        }
    }

    fun seekRelative(deltaSeconds: Int) {
        val newPos = (_currentPositionMs.value + (deltaSeconds * 1000L)).coerceIn(0L, _durationMs.value)
        seekTo(newPos)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applySpeed(speed)
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPrepared) {
            try {
                mediaPlayer?.let { mp ->
                    val params = mp.playbackParams
                    params.speed = speed
                    mp.playbackParams = params
                }
            } catch (e: Exception) {
                Log.w("AudioPlayerManager", "Cannot set playback speed: ${e.message}")
            }
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerJob?.cancel()
        if (minutes == null || minutes <= 0) {
            _sleepTimerSeconds.value = null
            return
        }

        val totalSecs = minutes * 60
        _sleepTimerSeconds.value = totalSecs

        sleepTimerJob = scope.launch(Dispatchers.Main) {
            var remaining = totalSecs
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _sleepTimerSeconds.value = remaining
            }
            if (remaining <= 0) {
                pause()
                _sleepTimerSeconds.value = null
            }
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(500L)
                try {
                    if (!isUsingFallback && isPrepared && mediaPlayer?.isPlaying == true) {
                        _currentPositionMs.value = mediaPlayer?.currentPosition?.toLong() ?: _currentPositionMs.value
                        val dur = mediaPlayer?.duration ?: 0
                        if (dur > 0) {
                            _durationMs.value = dur.toLong()
                        }
                    } else if (isUsingFallback && _playerState.value is PlayerState.Playing) {
                        _currentPositionMs.value = (_currentPositionMs.value + 500L).coerceAtMost(_durationMs.value)
                        if (_currentPositionMs.value >= _durationMs.value) {
                            _playerState.value = PlayerState.Completed
                            stopProgressTracker()
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    // Peaceful meditative tone generator fallback when online streaming is offline
    private fun playChimeFallback() {
        isUsingFallback = true
        _playerState.value = PlayerState.Playing
        startProgressTracker()

        scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                val numSamples = sampleRate * 2 // 2 seconds tone
                val buffer = ShortArray(numSamples)
                val freq = 432.0 // Serene 432Hz healing sound
                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    val decay = Math.exp(-time * 1.5)
                    val sinVal = sin(2.0 * Math.PI * freq * time) * decay
                    buffer[i] = (sinVal * Short.MAX_VALUE * 0.4).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                fallbackTrack = track
            } catch (e: Exception) {
                Log.w("AudioPlayerManager", "AudioTrack fallback failed: ${e.message}")
            }
        }
    }

    private fun stopFallback() {
        isUsingFallback = false
        try {
            fallbackTrack?.stop()
            fallbackTrack?.release()
            fallbackTrack = null
        } catch (e: Exception) {
            // ignore
        }
    }

    fun release() {
        stopProgressTracker()
        sleepTimerJob?.cancel()
        stopFallback()
        releasePlayer()
    }

    private fun releasePlayer() {
        try {
            isPrepared = false
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                } catch (ignored: Exception) {}
                try {
                    mp.reset()
                } catch (ignored: Exception) {}
                mp.release()
            }
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
        }
    }
}
