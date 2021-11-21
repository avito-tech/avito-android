package com.avito.plugin.internal

import com.avito.plugin.SignExtension
import okhttp3.HttpUrl.Companion.toHttpUrl

internal class UrlResolver(
    private val extension: SignExtension,
) {

    fun resolveServiceUrl(onFailure: (Throwable) -> Nothing): String {
        val value = extension.url
            .orElse("")
            .get()

        return validateUrl(value, onFailure)
    }
}

// used in tests
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
