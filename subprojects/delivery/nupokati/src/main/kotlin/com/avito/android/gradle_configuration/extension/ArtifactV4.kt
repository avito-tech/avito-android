package com.avito.android.gradle_configuration.extension

import org.gradle.api.provider.Provider
import java.io.Serializable
import java.nio.file.Path

public sealed interface ArtifactV4 : Serializable {
    public val file: Provider<Path>

    public data class AppBinary(
        val store: Provider<String>,
        override val file: Provider<Path>,
    ) : ArtifactV4

    public data class Artifact(
        override val file: Provider<Path>,
    ) : ArtifactV4
}
