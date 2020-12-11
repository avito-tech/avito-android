package com.avito.instrumentation.reservation.client.kubernetes

fun String.kubernetesName(): String = replace("_", "-").toLowerCase()
