package com.avito.android.model.input.config

import com.avito.android.model.input.AndroidArtifactType
import com.avito.android.model.input.OutputDescriptor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CdBuildConfigV3(
    @SerialName("schema_version") override val schemaVersion: Long,
    @SerialName("project") override val project: String,
    @SerialName("release_version") override val releaseVersion: String,
    @SerialName("output_descriptor") val outputDescriptor: OutputDescriptor,
    @SerialName("deployments") val deployments: List<Deployment>,
) : CdBuildConfig {

    @Serializable
    public sealed class Deployment {

        @Serializable
        @SerialName("app-binary")
        public data class AppBinary(
            val store: String,
            @SerialName("file_type") val fileType: AndroidArtifactType,
            @SerialName("build_configuration") val buildConfiguration: String,
        ) : Deployment()

        @Serializable
        @SerialName("artifact")
        public data class Artifact(
            val kind: String,
            @SerialName("file_type") val fileType: String,
        ) : Deployment()

        /**
         * @param isRelease Send artifacts as release versions. Non-release artifacts are stored for a limited time.
         */
        @Serializable
        @SerialName("qapps")
        public data class Qapps(
            @SerialName("is_release") val isRelease: Boolean,
        ) : Deployment()
    }
}
