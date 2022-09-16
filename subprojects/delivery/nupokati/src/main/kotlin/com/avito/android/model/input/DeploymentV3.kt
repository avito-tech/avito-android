package com.avito.android.model.input

import java.io.File
import java.io.Serializable

public sealed interface DeploymentV3 : Deployment, Serializable {

    public data class AppBinary(
        val store: String,
        val buildConfiguration: String,
        override val file: File
    ) : DeploymentV3

    public data class Artifact(
        val kind: String,
        override val file: File
    ) : DeploymentV3

    public data class QApps(
        val store: String,
        val buildConfiguration: String,
        override val file: File
    ) : DeploymentV3
}
