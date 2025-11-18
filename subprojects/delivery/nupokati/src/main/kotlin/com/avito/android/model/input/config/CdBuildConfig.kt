package com.avito.android.model.input.config

internal sealed interface CdBuildConfig {

    val schemaVersion: Long

    val project: String

    val releaseVersion: String
}
