package com.avito.android.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CdBuildConfigV3(
    @SerialName("schema_version") override val schemaVersion: Long,
    val project: NupokatiProject,
    @SerialName("output_descriptor") override val outputDescriptor: OutputDescriptor,
    @SerialName("release_version") override val releaseVersion: String,
    val deployments: List<Deployment>
) : CdBuildConfig {

    @Serializable
    sealed class Deployment {

        @Serializable
        @SerialName("app-binary")
        data class AppBinary(
            val store: String,
            @SerialName("file_type") val fileType: AndroidArtifactType,
            @SerialName("build_configuration") val buildConfiguration: String
        ) : Deployment()

        @Serializable
        @SerialName("artifact")
        data class Artifact(
            val kind: String,
            @SerialName("file_type") val fileType: String
        ) : Deployment()

        /**
         * @param isRelease Send artifacts as release versions. Non-release artifacts are stored for a limited time.
         */
        @Serializable
        @SerialName("qapps")
        data class Qapps(
            @SerialName("is_release") val isRelease: Boolean
        ) : Deployment()
    }
}
