package com.avito.android.tech_budget.internal.warnings.log

internal interface LogReader {

    fun getAll(): List<LogEntry>
}
