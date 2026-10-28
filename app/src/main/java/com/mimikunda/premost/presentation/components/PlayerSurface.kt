package com.mimikunda.premost.presentation.components

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun PlayerSurface(
    player: Player,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val animScale = remember { Animatable(1f) }
    val animOffsetX = remember { Animatable(0f) }
    val animOffsetY = remember { Animatable(0f) }

    fun animateSnapBack() {
        coroutineScope.launch {
            val targetScale = if (scale < 1.0f) 1.0f else scale.coerceIn(1.0f, 5.0f)
            val targetOffset = if (targetScale == 1.0f) Offset.Zero else {
                val maxX = (size.width * (targetScale - 1f)) / 2f
                val maxY = (size.height * (targetScale - 1f)) / 2f
                Offset(
                    x = offset.x.coerceIn(-maxX, maxX),
                    y = offset.y.coerceIn(-maxY, maxY)
                )
            }

            launch { animScale.animateTo(targetScale, spring(stiffness = 300f)) }
            launch { animOffsetX.animateTo(targetOffset.x, spring(stiffness = 300f)) }
            launch { animOffsetY.animateTo(targetOffset.y, spring(stiffness = 300f)) }
            scale = targetScale
            offset = targetOffset
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1.1f) {
                            scale = 1.0f
                            offset = Offset.Zero
                            animateSnapBack()
                        } else {
                            scale = 2.5f
                            offset = Offset.Zero
                            animateSnapBack()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.7f, 6.0f)
                        val maxOffsetX = ((size.width * (scale - 1f)) / 2f).coerceAtLeast(0f)
                        val maxOffsetY = ((size.height * (scale - 1f)) / 2f).coerceAtLeast(0f)

                        offset = if (scale <= 1.0f) {
                            Offset.Zero
                        } else {
                            Offset(
                                x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        }

                        coroutineScope.launch {
                            animScale.snapTo(scale)
                            animOffsetX.snapTo(offset.x)
                            animOffsetY.snapTo(offset.y)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val allPointersUp = event.changes.all { !it.pressed }
                        if (allPointersUp && scale < 1.0f) {
                            animateSnapBack()
                        }
                    }
                }
            }
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    keepScreenOn = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    this.player = player
                }
            },
            update = { playerView ->
                playerView.player = player
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = animScale.value
                    scaleY = animScale.value
                    translationX = animOffsetX.value
                    translationY = animOffsetY.value
                }
        )
    }
}
