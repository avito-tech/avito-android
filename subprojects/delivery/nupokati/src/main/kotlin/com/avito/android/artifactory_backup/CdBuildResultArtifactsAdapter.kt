package com.avito.android.artifactory_backup

import com.avito.android.model.BuildOutput
import com.avito.android.model.CdBuildResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import okhttp3.HttpUrl
import java.io.File

internal class CdBuildResultArtifactsAdapter {

    private val moshi: Moshi = Moshi.Builder()
        .add(
            PolymorphicJsonAdapterFactory.of(CdBuildResult.Artifact::class.java, "artifact")
                .withSubtype(CdBuildResult.Artifact.FileArtifact::class.java, "file")
                .withSubtype(CdBuildResult.Artifact.AndroidBinary::class.java, "binary")
        )
        .build()

    @OptIn(ExperimentalStdlibApi::class)
    private val buildOutputAdapter = moshi.adapter<BuildOutput>()

    fun create(file: File, url: HttpUrl, buildVariant: String): CdBuildResult.Artifact {
        return if (file.isAndroidBinary()) {
            CdBuildResult.Artifact.AndroidBinary(
                type = file.type(),
                name = file.name,
                uri = url.toString(),
                buildVariant = buildVariant
            )
        } else {
            CdBuildResult.Artifact.FileArtifact(
                type = file.type(),
                name = file.name,
                uri = url.toString()
            )
        }
    }

    fun toJson(artifacts: List<CdBuildResult.Artifact>): String {
        val buildOutput = BuildOutput().also { it.artifacts = artifacts }
        return buildOutputAdapter.toJson(buildOutput)
    }

    fun fromJson(json: String): BuildOutput {
        return requireNotNull(buildOutputAdapter.fromJson(json))
    }

    private fun File.isAndroidBinary(): Boolean = extension == "aab" || extension == "apk"

    private fun File.type(): String = when (extension) {
        "aab" -> "bundle"
        "apk" -> "apk"
        else -> throw IllegalArgumentException("Unknown file type with extension: $extension")
    }
}
