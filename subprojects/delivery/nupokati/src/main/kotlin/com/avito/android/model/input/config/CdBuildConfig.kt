package com.avito.android.model.input.config

public sealed interface CdBuildConfig {

    public val schemaVersion: Long

    public val project: String

    public val releaseVersion: String
}
