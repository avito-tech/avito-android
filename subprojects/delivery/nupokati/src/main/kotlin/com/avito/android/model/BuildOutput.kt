package com.avito.android.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public class BuildOutput {

    public var artifacts: List<CdBuildResult.Artifact> = emptyList()

    override fun toString(): String {
        return artifacts.toString()
    }
}
