package com.avito.report.model

//todo extends CompletedTest?
class PerformanceTest(
    val testName: String,
    val id: String,
    val series: Map<String, List<Double>>
)
