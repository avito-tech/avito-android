package com.avito.instrumentation.internal.executing

import java.io.Serializable

public data class ExecutionParameters(
    val applicationPackageName: String,
    val applicationTestPackageName: String,
    val testRunner: String,
    val namespace: String,
    val logcatTags: Collection<String>,
    val enableDeviceDebug: Boolean
) : Serializable {
    public companion object
}
