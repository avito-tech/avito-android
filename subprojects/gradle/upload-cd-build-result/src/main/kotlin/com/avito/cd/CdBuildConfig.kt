package com.avito.cd

import com.google.gson.annotations.SerializedName

enum class AndroidArtifactType {
    @SerializedName("bundle")
    BUNDLE,
    @SerializedName("apk")
    APK
}

enum class BuildVariant {
    @SerializedName("release")
    RELEASE,
    @SerializedName("staging")
    STAGING,
    @SerializedName("debug")
    DEBUG
}

enum class NupokatiProject(val id: String) {
    @SerializedName("avito")
    AVITO("avito"),
    @SerializedName("avito_test")
    AVITO_TEST("avito_test")
}

data class CdBuildConfig(
    val schemaVersion: Long,
    val project: NupokatiProject,
    val outputDescriptor: OutputDescriptor,
    val releaseVersion: String,
    val deployments: List<Deployment>
) {
    data class OutputDescriptor(
        val path: String,
        val skipUpload: Boolean
    )

    sealed class Deployment {
        data class GooglePlay(
            val artifactType: AndroidArtifactType,
            val buildVariant: BuildVariant,
            val track: Track
        ) : Deployment()

        data class Unknown(val type: String): Deployment()

        enum class Track {
            @SerializedName("alpha")
            ALPHA,
            @SerializedName("internal")
            INTERNAL,
            @SerializedName("beta")
            BETA,
            @SerializedName("production")
            PRODUCTION
        }
    }
}






