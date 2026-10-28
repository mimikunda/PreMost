package com.mimikunda.premost.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mimikunda.premost.presentation.components.ErrorOverlay
import com.mimikunda.premost.presentation.components.LoadingOverlay
import com.mimikunda.premost.presentation.components.PlayerSurface
import com.mimikunda.premost.presentation.components.SettingsFloatingActionButton
import com.mimikunda.premost.presentation.components.SetupDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebcamScreen(
    modifier: Modifier = Modifier,
    viewModel: WebcamViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()

    var showSetupDialog by remember { mutableStateOf(false) }

    val isUnconfigured = uiState is WebcamUiState.Unconfigured

    Box(modifier = modifier.fillMaxSize()) {
        PlayerSurface(player = viewModel.exoPlayer)

        SettingsFloatingActionButton(
            onClick = { showSetupDialog = true }
        )

        LoadingOverlay(visible = uiState is WebcamUiState.Loading)

        (uiState as? WebcamUiState.Error)?.let { errorState ->
            ErrorOverlay(
                state = errorState,
                onRetry = { viewModel.retryStream() },
                onEditConfig = { showSetupDialog = true }
            )
        }

        if (isUnconfigured || showSetupDialog) {
            SetupDialog(
                initialConfig = currentConfig,
                onSave = { url, referer, origin ->
                    viewModel.updateStream(url, referer, origin)
                    showSetupDialog = false
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
