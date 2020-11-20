package com.avito.bytecode.report

class CompositeReporter(
    private val reporters: List<DataReporter>
) : DataReporter {

    override fun <T> report(data: T) {
        reporters.forEach { it.report(data) }
    }
}
