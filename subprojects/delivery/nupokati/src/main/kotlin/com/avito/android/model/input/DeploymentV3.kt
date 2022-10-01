package com.avito.android.model.input

import java.io.File
import java.io.Serializable

internal sealed interface DeploymentV3 : Deployment, Serializable {

    data class AppBinary(
        val store: String,
        val buildConfiguration: String,
        override val file: File
    ) : DeploymentV3

    data class Artifact(
        val kind: String,
        override val file: File
    ) : DeploymentV3

    data class QApps(
        val store: String,
        val buildConfiguration: String,
        override val file: File
    ) : DeploymentV3
}
