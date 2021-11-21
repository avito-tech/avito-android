package com.avito.android.signer.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.android.signer.DOCUMENTATION_URL
import com.avito.android.signer.PLUGIN_ID
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.provider.Property

internal class UrlResolver {

    fun resolveServiceUrl(url: Property<String>, taskPath: String): String {
        val value = url
            .orElse("")
            .get()

        return try {
            validateUrl(value)
        } catch (e: IllegalArgumentException) {
            throw Problem(
                shortDescription = "Failed to configure '$taskPath'",
                context = "Configuration of '$PLUGIN_ID' plugin",
                because = "serviceUrl is invalid",
                possibleSolutions = listOf(
                    "Check if serviceUrl value is set and it's a valid URL"
                ),
                documentedAt = DOCUMENTATION_URL
            ).asRuntimeException()
        }
    }
}

// used in tests
internal fun validateUrl(url: String): String {
    return try {
        url.toHttpUrl()
            .newBuilder()
            .addEncodedPathSegment("sign")
            .build()
            .toString()
    } catch (e: Throwable) {
        throw IllegalArgumentException("Invalid signer url value: '$url'", e)
    }
}
