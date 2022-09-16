package com.avito.android.model.input

import java.io.File

public data class DeploymentV2(
    override val file: File,
    val buildVariant: String
) : Deployment
