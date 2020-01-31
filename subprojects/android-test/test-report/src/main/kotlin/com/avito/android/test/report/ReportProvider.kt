package com.avito.android.test.report

/**
 * Нужно чтобы InstrumentationTestRunner'ы из зависимых модулей могли провайдить репорты
 */
interface ReportProvider {
    val report: Report
}
