package com.avito.android.gradle_configuration.extension

import org.gradle.api.provider.Provider
import java.io.File
import java.io.Serializable

public sealed interface ArtifactV4 : Serializable {
    public val file: Provider<File>

    public data class AppBinary(
        val storeName: String?,
        override val file: Provider<File>,
    ) : ArtifactV4

    public data class Artifact(
        override val file: Provider<File>,
    ) : ArtifactV4
}
