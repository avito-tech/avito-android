package com.avito.instrumentation.executing

import java.io.Serializable

data class ExecutionParameters(
    val applicationPackageName: String,
    val applicationTestPackageName: String,
    val testRunner: String,
    val namespace: String,
    val logcatTags: Collection<String>
) : Serializable {
    companion object
}
