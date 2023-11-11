package com.avito.instrumentation

import com.avito.capitalize

internal fun instrumentationTaskName(
    configuration: String,
    environment: String,
    flavor: String?
): String =
    buildString {
        append("instrumentation")
        if (!flavor.isNullOrBlank()) {
            append(flavor.capitalize())
        }
        append(configuration.capitalize())
        if (environment.isNotBlank()) {
            append(environment.capitalize())
        }
    }

public const val instrumentationPluginId: String = "com.avito.android.instrumentation-tests"

internal const val dumpDirName: String = "input-args-dump"

internal const val CI_TASK_GROUP = "ci"
