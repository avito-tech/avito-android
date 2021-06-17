package com.avito.plugin

import com.android.build.api.variant.Variant
import com.avito.android.Result
import com.avito.kotlin.dsl.getBooleanProperty
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.register
import java.util.Objects.requireNonNull

@Suppress("UnstableApiUsage")
internal inline fun <reified T : SignArtifactTask> registerTask(
    tasks: TaskContainer,
    variant: Variant<*>,
    taskName: String,
    signTokensMap: Map<String, String?>,
    extension: SignExtension,
) {
    tasks.register<T>(taskName) {
        group = CI_TASK_GROUP
        description = "Sign ${variant.name} with in-house service"

        val token = resolveToken(signTokensMap, variant)

        val isCustomSigningEnabled = isCustomSigningEnabled(project, extension)

        serviceUrl.set(resolveServiceUrl(extension))
        tokenProperty.set(token.toPropertyOrThrow(isCustomSigningEnabled))
        readWriteTimeoutSec.set(extension.readWriteTimeoutSec.convention(DEFAULT_TIMEOUT_SEC))

        onlyIf { isCustomSigningEnabled }
    }
}

private fun Result<String>.toPropertyOrThrow(isCustomSigningEnabled: Boolean): Provider<String> {
    return if (isCustomSigningEnabled) {
        Providers.of(getOrThrow())
    } else {
        Providers.notDefined()
    }
}

@Suppress("UnstableApiUsage")
private fun resolveToken(signTokensMap: Map<String, String?>, variant: Variant<*>): Result<String> {
    val buildTypeName = requireNonNull(variant.buildType)
    val token: String? = signTokensMap[buildTypeName]

    return if (token.hasContent()) {
        Result.Success(token)
    } else {
        Result.Failure(IllegalArgumentException("Can't sign variant: '${variant.name}'; token is not set"))
    }
}

private fun isCustomSigningEnabled(project: Project, extension: SignExtension): Boolean {
    val isEnabled = extension.enabled.getOrElse(true)

    // todo use extension enabled in avito and disableSignService property read after "2021.21" version
    return if (project.getBooleanProperty("disableSignService")) {
        false
    } else {
        isEnabled
    }
}

@Suppress("DEPRECATION", "UnstableApiUsage")
private fun resolveServiceUrl(extension: SignExtension): String {
    val value = extension.url
        .orElse(extension.host.orEmpty())
        .get()

    return validateUrl(value)
}

private fun validateUrl(url: String): String {
    return try {
        url.toHttpUrl()
        url
    } catch (e: Throwable) {
        throw IllegalArgumentException("Invalid signer url value: '$url'", e)
    }
}

private const val DEFAULT_TIMEOUT_SEC = 40L

private const val CI_TASK_GROUP = "ci"
