package com.mimikunda.premost

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mimikunda.premost.presentation.WebcamScreen
import com.mimikunda.premost.presentation.WebcamViewModel
import com.mimikunda.premost.ui.theme.PreMostTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WebcamViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        viewModel.handleDeepLink(intent)

        setContent {
            PreMostTheme {
                WebcamScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleDeepLink(intent)
    }
}
