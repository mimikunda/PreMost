package com.mimikunda.premost.data

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource

@OptIn(UnstableApi::class)
class StreamMediaSourceFactory(private val context: Context) {

    companion object {
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
    }

    fun createMediaSource(config: StreamConfig): MediaSource {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(DEFAULT_USER_AGENT)
            .setAllowCrossProtocolRedirects(true)

        val headers = mutableMapOf<String, String>()
        if (config.referer.isNotBlank()) {
            headers["Referer"] = config.referer
        }
        if (config.origin.isNotBlank()) {
            headers["Origin"] = config.origin
        }
        if (headers.isNotEmpty()) {
            httpDataSourceFactory.setDefaultRequestProperties(headers)
        }

        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        val hlsMediaSourceFactory = HlsMediaSource.Factory(dataSourceFactory)
            .setAllowChunklessPreparation(true)

        val mediaItem = MediaItem.fromUri(config.url)
        return hlsMediaSourceFactory.createMediaSource(mediaItem)
    }
}
