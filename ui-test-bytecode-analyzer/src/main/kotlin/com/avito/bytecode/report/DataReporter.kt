package com.avito.bytecode.report

interface DataReporter {
    fun <T> report(data: T)
}
