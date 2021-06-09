package com.avito.report

public class NoOpTestSuiteNameProvider(private val name: String = "Unknown") : TestSuiteNameProvider {

    override fun getName(): String = name
}
