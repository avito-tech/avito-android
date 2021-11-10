package com.avito.logger

import java.io.Serializable

public data class GradleLoggerCoordinates(
    val projectPath: String,
    val taskName: String? = null
) : Serializable
