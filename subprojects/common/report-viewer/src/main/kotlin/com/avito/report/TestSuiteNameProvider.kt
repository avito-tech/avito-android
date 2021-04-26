package com.avito.report

interface TestSuiteNameProvider {

    fun getName(): String

    object NoOp : TestSuiteNameProvider {

        override fun getName(): String = "Unknown"
    }
}
