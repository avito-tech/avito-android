package com.avito.runner.scheduler.runner.model

import java.io.Serializable

public data class ExecutionParameters(
    val applicationPackageName: String,
    val applicationTestPackageName: String,
    val testRunner: String,
    val logcatTags: Collection<String>,
) : Serializable {

    public companion object
}
