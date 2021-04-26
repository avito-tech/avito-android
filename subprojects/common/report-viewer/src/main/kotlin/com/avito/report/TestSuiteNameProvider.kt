package com.avito.report

interface TestSuiteNameProvider {

    fun getName(): String

    class NoOp(private val name: String = "Unknown") : TestSuiteNameProvider {

        override fun getName(): String = name
    }
}
