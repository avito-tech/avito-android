package com.avito.runner.service.model.intention

import com.avito.test.model.TestCase

public data class InstrumentationTestRunAction(
    public val test: TestCase,
    public val testPackage: String,
    public val targetPackage: String,
    public val testRunner: String,
    public val instrumentationParams: Map<String, String>,
    public val executionNumber: Int,
    public val timeoutMinutes: Long,
    public val enableDeviceDebug: Boolean
) {

    override fun toString(): String = "$test, execution=$executionNumber"

    internal companion object
}
