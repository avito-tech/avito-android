package com.avito.plugin

import okhttp3.HttpUrl.Companion.toHttpUrl

internal class UrlResolver(
    private val extension: SignExtension,
) {

    @Suppress("DEPRECATION", "UnstableApiUsage")
    fun resolveServiceUrl(onFailure: (Throwable) -> Nothing): String {
        val value = extension.url
            .orElse(extension.host.orEmpty())
            .get()

        return validateUrl(value, onFailure)
    }

    private fun validateUrl(url: String, onFailure: (Throwable) -> Nothing): String {
        return try {
            url.toHttpUrl()
            url
        } catch (e: Throwable) {
            onFailure(IllegalArgumentException("Invalid signer url value: '$url'", e))
        }
    }
}
