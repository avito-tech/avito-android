package com.avito.instrumentation.internal.reservation.client.kubernetes

interface DeploymentNameGenerator {

    fun generateName(namespace: String): String
}
