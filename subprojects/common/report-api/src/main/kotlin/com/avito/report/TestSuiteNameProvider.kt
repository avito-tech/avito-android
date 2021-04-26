package com.avito.report

public interface TestSuiteNameProvider {

    public fun getName(): String

    public class NoOp(private val name: String = "Unknown") : TestSuiteNameProvider {

        override fun getName(): String = name
    }
}
