package com.avito.android

import com.avito.capitalize

internal const val CD_TASK_GROUP: String = "cd"

internal const val DEFAULT_RELEASE_VARIANT: String = "release"

internal const val DEFAULT_CHUNKED_UPLOAD_THRESHOLD: Long = 400 * 1024L * 1024L // 400 MiB
internal const val DEFAULT_NUPOKATI_CLIENT_CONNECTION_TIMEOUT: Long = 30L
internal const val DEFAULT_NUPOKATI_CLIENT_READ_TIMEOUT: Long = 60L
internal const val DEFAULT_NUPOKATI_CLIENT_WRITE_TIMEOUT: Long = 60L

@Suppress("VariableNaming")
internal val Int.MiB: Long
    get() = this * 1024L * 1024L

internal fun uploadCdBuildResultTaskName(variantSlug: String): String = "uploadCdBuildResult$variantSlug"

internal fun uploadArtifactsTaskName(specName: String): String = "uploadNupokatiArtifacts${specName.capitalize()}"

internal fun sendTestResultsTaskName(specName: String): String = "sendTestResults${specName.capitalize()}"
