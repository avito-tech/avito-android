package com.avito.plugin

import com.android.build.api.variant.Variant
import com.avito.android.Result
import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider

@Suppress("UnstableApiUsage")
internal class SigningResolver(
    private val project: Project,
    private val extension: SignExtension,
    private val variant: Variant,
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

        // todo use extension enabled in avito and disableSignService property read after "2021.21" version
        if (project.getBooleanProperty("disableSignService")) {
            false
        } else {
            isEnabled && hasTokenRegistered
        }
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
