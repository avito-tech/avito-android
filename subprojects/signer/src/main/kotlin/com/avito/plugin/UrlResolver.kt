package com.avito.plugin

import okhttp3.HttpUrl.Companion.toHttpUrl

internal class UrlResolver(
    private val extension: SignExtension,
) {

    @Suppress("UnstableApiUsage")
    fun resolveServiceUrl(onFailure: (Throwable) -> Nothing): String {
        val value = extension.url
            .orElse("")
            .get()

        return validateUrl(value, onFailure)
    }
}

internal fun validateUrl(url: String, onFailure: (Throwable) -> Nothing): String {
    return try {
        url.toHttpUrl()
            .newBuilder()
            .addEncodedPathSegment("sign")
            .build()
            .toString()
    } catch (e: Throwable) {
        onFailure(IllegalArgumentException("Invalid signer url value: '$url'", e))
    }
}
