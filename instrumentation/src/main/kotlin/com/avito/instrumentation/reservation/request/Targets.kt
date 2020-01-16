package com.avito.instrumentation.reservation.request

import java.io.Serializable

data class Targets(
    val testingTargets: List<TestingTarget>
) : Serializable
