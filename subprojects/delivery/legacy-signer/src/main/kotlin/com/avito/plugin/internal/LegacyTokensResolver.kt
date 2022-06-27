package com.avito.plugin.internal

import com.android.build.api.variant.ApplicationVariant
import com.avito.android.Result
import com.avito.plugin.SignExtension
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

internal class LegacyTokensResolver(
    private val extension: SignExtension,
    private val variant: ApplicationVariant,
    private val signTokens: Map<String, Provider<String>>,
) {

    /**
     * Check only key instead of value to distinguish registered but empty (missing) tokens
     */
    private val hasTokenRegistered: Boolean = signTokens.containsKey(variant.name)

    private val token: Result<String> by lazy {
        val token: String? = signTokens[variant.name]?.orNull

        if (token.hasContent()) {
            Result.Success(token)
        } else {
            Result.Failure(IllegalArgumentException("Can't sign variant: '${variant.name}'; token is not set"))
        }
    }

    val isCustomSigningEnabled: Boolean by lazy {
        val isEnabled = extension.enabled.getOrElse(true)

        isEnabled && hasTokenRegistered
    }

    fun resolveToken(onFailure: (Throwable) -> Nothing): Provider<String> {
        return if (isCustomSigningEnabled) {
            try {
                Providers.of(token.getOrThrow())
            } catch (e: Throwable) {
                onFailure.invoke(e)
            }
        } else {
            Providers.notDefined()
        }
    }
}
