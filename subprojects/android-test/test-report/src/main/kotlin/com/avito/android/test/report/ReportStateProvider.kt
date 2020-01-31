package com.avito.android.test.report

/**
 * Интерфейс для интеграционного тестирования репорта.
 * Все реализации репорта, которые тестируются, должны его имплеменировать,
 * чтобы мы могли взять снепшот текущего стейта.
 */
interface ReportStateProvider {
    fun getCurrentState(): ReportState
}
