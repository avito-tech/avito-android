package com.avito.android.runner.devices.internal.kubernetes

internal interface DeploymentNameGenerator {

    fun generateName(namespace: String): String
}
