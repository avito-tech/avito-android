package com.avito.report.model

public data class Step(
    val timestamp: Long,
    val number: Int,
    val title: String,
    val entryList: List<Entry>
)
