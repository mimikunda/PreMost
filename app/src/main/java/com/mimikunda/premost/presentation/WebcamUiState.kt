package com.mimikunda.premost.presentation

import androidx.media3.common.PlaybackException

sealed interface WebcamUiState {
    data object Unconfigured : WebcamUiState
    data class Loading(val streamUrl: String) : WebcamUiState
    data class Playing(val streamUrl: String) : WebcamUiState
    data class Error(val streamUrl: String, val cause: PlaybackException?) : WebcamUiState
}
