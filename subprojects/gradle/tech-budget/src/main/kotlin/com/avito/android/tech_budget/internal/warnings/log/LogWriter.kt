package com.avito.android.tech_budget.internal.warnings.log

internal interface LogWriter {

    fun save(logMessage: String)
}
