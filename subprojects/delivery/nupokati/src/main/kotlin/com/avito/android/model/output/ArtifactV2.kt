package com.avito.android.model.output

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class ArtifactV2 : Artifact() {
    abstract val type: String
    abstract val name: String
    abstract val uri: String

    @Serializable
    @SerialName("binary")
    data class AndroidBinary(
        override val type: String,
        override val name: String,
        override val uri: String,
        @SerialName("build_variant") val buildVariant: String
    ) : ArtifactV2()

    @Serializable
    @SerialName("file")
    data class FileArtifact(
        override val type: String,
        override val name: String,
        override val uri: String
    ) : ArtifactV2()
}
