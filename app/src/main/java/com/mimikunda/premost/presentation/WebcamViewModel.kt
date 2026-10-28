package com.mimikunda.premost.presentation

import android.app.Application
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.mimikunda.premost.data.StreamConfig
import com.mimikunda.premost.data.StreamMediaSourceFactory
import com.mimikunda.premost.data.StreamPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class WebcamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StreamPreferencesRepository(application)
    private val mediaSourceFactory = StreamMediaSourceFactory(application)

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _uiState = MutableStateFlow<WebcamUiState>(WebcamUiState.Unconfigured)
    val uiState: StateFlow<WebcamUiState> = _uiState.asStateFlow()

    private val _currentConfig = MutableStateFlow<StreamConfig?>(null)
    val currentConfig: StateFlow<StreamConfig?> = _currentConfig.asStateFlow()

    private var retryCount = 0
    private val maxAutoRetries = 3
    private var autoRetryJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val config = _currentConfig.value ?: return
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    if (_uiState.value !is WebcamUiState.Error) {
                        _uiState.value = WebcamUiState.Loading(config.url)
                    }
                }
                Player.STATE_READY -> {
                    retryCount = 0
                    _uiState.value = WebcamUiState.Playing(config.url)
                }
                Player.STATE_ENDED -> {
                    retryStream()
                }
                Player.STATE_IDLE -> {
                    // Handled elsewhere
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val config = _currentConfig.value ?: return
            if (isPlaying) {
                retryCount = 0
                _uiState.value = WebcamUiState.Playing(config.url)
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val config = _currentConfig.value ?: return
            if (retryCount < maxAutoRetries) {
                retryCount++
                _uiState.value = WebcamUiState.Loading(config.url)
                autoRetryJob?.cancel()
                autoRetryJob = viewModelScope.launch {
                    delay(1500L * retryCount)
                    if (_currentConfig.value?.url == config.url) {
                        preparePlayer(config)
                    }
                }
            } else {
                _uiState.value = WebcamUiState.Error(config.url, error)
            }
        }
    }

    init {
        exoPlayer.addListener(playerListener)
        observeStreamConfig()
    }

    private fun observeStreamConfig() {
        viewModelScope.launch {
            repository.streamConfigFlow.collectLatest { config ->
                _currentConfig.value = config
                autoRetryJob?.cancel()
                retryCount = 0

                if (config == null) {
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                    _uiState.value = WebcamUiState.Unconfigured
                } else {
                    _uiState.value = WebcamUiState.Loading(config.url)
                    preparePlayer(config)
                }
            }
        }
    }

    private fun preparePlayer(config: StreamConfig) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        val mediaSource = mediaSourceFactory.createMediaSource(config)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    fun retryStream() {
        val config = _currentConfig.value ?: return
        retryCount = 0
        _uiState.value = WebcamUiState.Loading(config.url)
        preparePlayer(config)
    }

    fun updateStream(url: String, referer: String = "", origin: String = "") {
        viewModelScope.launch {
            repository.saveStreamConfig(url, referer, origin)
        }
    }

    fun resetConfiguration() {
        viewModelScope.launch {
            repository.clearStreamConfig()
        }
    }

    override fun onCleared() {
        autoRetryJob?.cancel()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}
