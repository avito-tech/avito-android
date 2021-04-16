package com.avito.report

interface TestSuiteNameProvider {

    fun getName(): String

    object Stub : TestSuiteNameProvider {

        override fun getName(): String = ""
    }
}
