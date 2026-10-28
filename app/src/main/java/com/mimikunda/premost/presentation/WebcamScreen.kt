package com.mimikunda.premost.presentation

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mimikunda.premost.presentation.components.ErrorOverlay
import com.mimikunda.premost.presentation.components.LoadingOverlay
import com.mimikunda.premost.presentation.components.PlayerSurface
import com.mimikunda.premost.presentation.components.SettingsFloatingActionButton
import com.mimikunda.premost.presentation.components.SetupDialog
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebcamScreen(
    modifier: Modifier = Modifier,
    viewModel: WebcamViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()

    var showSetupDialog by remember { mutableStateOf(false) }
    var isUiVisible by remember { mutableStateOf(true) }

    val isUnconfigured = uiState is WebcamUiState.Unconfigured
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }

    LaunchedEffect(isUiVisible, uiState, showSetupDialog) {
        if (isUiVisible && uiState is WebcamUiState.Playing && !showSetupDialog) {
            delay(3500L)
            isUiVisible = false
        }
    }

    val shouldShowUi = isUiVisible || isUnconfigured || showSetupDialog || uiState is WebcamUiState.Error

    DisposableEffect(shouldShowUi, activity) {
        val window = activity?.window
        if (window != null) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowInsetsControllerCompat(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            if (shouldShowUi) {
                insetsController.show(WindowInsetsCompat.Type.systemBars())
            } else {
                insetsController.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {}
    }

    Box(modifier = modifier.fillMaxSize()) {
        PlayerSurface(
            player = viewModel.exoPlayer,
            onTap = {
                isUiVisible = !isUiVisible
            }
        )

        SettingsFloatingActionButton(
            visible = shouldShowUi,
            onClick = {
                isUiVisible = true
                showSetupDialog = true
            }
        )

        LoadingOverlay(visible = uiState is WebcamUiState.Loading)

        (uiState as? WebcamUiState.Error)?.let { errorState ->
            ErrorOverlay(
                state = errorState,
                onRetry = {
                    isUiVisible = true
                    viewModel.retryStream()
                },
                onEditConfig = {
                    isUiVisible = true
                    showSetupDialog = true
                }
            )
        }

        if (isUnconfigured || showSetupDialog) {
            SetupDialog(
                initialConfig = currentConfig,
                onSave = { url, referer, origin ->
                    viewModel.updateStream(url, referer, origin)
                    showSetupDialog = false
                    isUiVisible = true
                },
                onDismiss = {
                    if (!isUnconfigured) {
                        showSetupDialog = false
                    }
                }
            )
        }
    }
}
