package com.avito.android.model.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class ArtifactV3 : Artifact() {
    abstract val fileType: String
    abstract val name: String
    abstract val uri: String

    @Serializable
    @SerialName("app-binary")
    data class AppBinary(
        val store: String,
        @SerialName("file_type") override val fileType: String,
        override val name: String,
        override val uri: String,
        @SerialName("build_configuration") val buildConfiguration: String,
    ) : ArtifactV3()

    @Serializable
    @SerialName("artifact")
    data class FileArtifact(
        @SerialName("file_type") override val fileType: String,
        override val name: String,
        override val uri: String,
        val kind: String,
    ) : ArtifactV3()

    @Serializable
    @SerialName("qapps")
    data class QApps(
        @SerialName("file_type") override val fileType: String,
        override val name: String,
        override val uri: String,
        @SerialName("is_release") val isRelease: Boolean,
    ) : ArtifactV3()
}
