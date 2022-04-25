package com.avito.emcee.queue

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class BuildMetadata(
    val artifacts: BuildArtifacts,
    val runnerClass: String,
)
