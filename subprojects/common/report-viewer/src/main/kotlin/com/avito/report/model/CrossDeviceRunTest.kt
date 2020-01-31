package com.avito.report.model

/**
 * Один тест но в разрезе запуска на всех доступных девайсах, здесь меняется понятия status
 */
data class CrossDeviceRunTest(
    val name: TestName,
    val status: CrossDeviceStatus
) {
    companion object
}
