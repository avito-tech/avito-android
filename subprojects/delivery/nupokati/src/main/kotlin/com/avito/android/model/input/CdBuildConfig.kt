package com.avito.android.model.input

internal sealed interface CdBuildConfig {

    val schemaVersion: Long

    val releaseVersion: String

    val outputDescriptor: OutputDescriptor
}
