package com.avito.android.tech_budget.ab_tests

import java.io.File

public interface ABTestsFileParser {
    public fun parse(file: File): List<ABTest>
}
