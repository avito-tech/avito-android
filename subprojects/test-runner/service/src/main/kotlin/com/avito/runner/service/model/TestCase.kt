package com.avito.runner.service.model

import java.io.Serializable

data class TestCase(
    val className: String,
    val methodName: String,
    val deviceName: String
) : Serializable {

    val testName: String = "$className.$methodName.$deviceName"

    companion object
}
