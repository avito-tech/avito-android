package com.avito.instrumentation.reservation.client.kubernetes

interface DeploymentNameGenerator {

    fun generateName(namespace: String): String
}
