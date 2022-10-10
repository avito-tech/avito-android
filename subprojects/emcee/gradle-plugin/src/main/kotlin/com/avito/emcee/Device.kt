package com.avito.emcee

import org.gradle.api.tasks.Input
import java.io.Serializable

public data class Device(
    @get:Input
    val sdk: Int,
    @get:Input
    val type: String,
) : Serializable
