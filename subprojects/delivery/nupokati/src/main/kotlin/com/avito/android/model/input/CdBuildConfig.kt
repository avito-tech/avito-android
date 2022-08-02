package com.avito.android.model.input

public sealed interface CdBuildConfig {

    public val schemaVersion: Long

    public val releaseVersion: String

    public val outputDescriptor: OutputDescriptor
}
