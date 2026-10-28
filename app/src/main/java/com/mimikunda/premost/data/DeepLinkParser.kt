package com.mimikunda.premost.data

import android.content.Intent
import android.net.Uri

object DeepLinkParser {

    fun parseIntent(intent: Intent?): StreamConfig? {
        if (intent?.action != Intent.ACTION_VIEW) return null
        val uri = intent.data ?: return null
        val type = intent.type?.lowercase()

        val parsed = parseUri(uri)
        if (parsed != null) return parsed

        if (type?.contains("mpegurl") == true || uri.toString().contains(".m3u8", ignoreCase = true)) {
            val urlStr = uri.toString().trim()
            if (urlStr.startsWith("http://", ignoreCase = true) || urlStr.startsWith("https://", ignoreCase = true)) {
                return StreamConfig(url = urlStr)
            }
        }

        return null
    }

    fun parseUri(uri: Uri?): StreamConfig? {
        if (uri == null) return null
        val scheme = uri.scheme?.lowercase() ?: return null

        var streamUrl: String? = null
        var referer = ""
        var origin = ""

        if (scheme == "premost") {
            streamUrl = uri.getQueryParameter("url")
            referer = uri.getQueryParameter("referer") ?: ""
            origin = uri.getQueryParameter("origin") ?: ""

            if (streamUrl.isNullOrBlank() && uri.schemeSpecificPart.isNotBlank()) {
                val candidate = uri.schemeSpecificPart.trimStart('/')
                if (candidate.startsWith("http://", ignoreCase = true) || candidate.startsWith("https://", ignoreCase = true)) {
                    streamUrl = candidate
                }
            }
        } else if (scheme == "http" || scheme == "https") {
            val urlParam = uri.getQueryParameter("url")
            if (!urlParam.isNullOrBlank()) {
                streamUrl = urlParam
                referer = uri.getQueryParameter("referer") ?: ""
                origin = uri.getQueryParameter("origin") ?: ""
            } else {
                streamUrl = uri.toString()
                referer = uri.getQueryParameter("referer") ?: ""
                origin = uri.getQueryParameter("origin") ?: ""
            }
        }

        val trimmedUrl = streamUrl?.trim() ?: return null
        if (trimmedUrl.startsWith("http://", ignoreCase = true) || trimmedUrl.startsWith("https://", ignoreCase = true)) {
            return StreamConfig(
                url = trimmedUrl,
                referer = referer.trim(),
                origin = origin.trim()
            )
        }
        return null
    }
}
