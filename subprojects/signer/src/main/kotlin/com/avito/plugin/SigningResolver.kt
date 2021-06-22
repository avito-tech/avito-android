package com.avito.plugin

import com.android.build.api.variant.Variant
import com.avito.android.Result
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

@Suppress("UnstableApiUsage")
internal class SigningResolver(
    private val extension: SignExtension,
    private val variant: Variant<*>,
    private val signTokensMap: Map<String, String?>,
) {

    private val buildTypeName: String
        get() = requireNotNull(variant.buildType) {
            "${variant.name}.buildType is null, shouldn't happen"
        }

    private val hasTokenRegistered: Boolean = signTokensMap.containsKey(buildTypeName)

    private val token: Result<String> by lazy {
        val token: String? = signTokensMap[buildTypeName]

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
