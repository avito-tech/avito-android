package com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks

internal class RequestedTasksMetadata(
    val userName: String,
    commitHashProvider: Lazy<String>,
    val isInvokedFromIde: Boolean,
) {
    val commitHash: String by commitHashProvider
}
