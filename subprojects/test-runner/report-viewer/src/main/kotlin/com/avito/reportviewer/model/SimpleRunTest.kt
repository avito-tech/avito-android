package com.avito.reportviewer.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.report.model.Flakiness
import com.avito.report.model.Kind
import com.avito.report.model.Stability
import com.avito.test.model.TestName
import com.avito.test.model.TestStatus

/**
 * Один тест в пределах репорта, включая все перезапуски, но на одном устройстве
 * Упрощенная версия, доступная при запросе RunTest.List
 * Аналог неразвернутой инфы по тестам в ReportViewer
 *
 * @see RunTest подробная версия
 *
 * @param id идентификатор теста, по нему можно составить url до report viewer конкретно с этим тестом
 *
 * @param name имя теста вида: className.methodName, такой формат используется везде в instrumentation plugin
 *             в mongo же хранится className::methodName
 */
public data class SimpleRunTest(
    val id: String,
    val reportId: String,
    val name: TestName,
    val testCaseId: Int?,
    val deviceName: String,
    val status: TestStatus,
    val stability: Stability,
    val buildId: String?,
    val groupList: List<String>,
    val startTime: Long,
    val endTime: Long,
    val skipReason: String?,
    val isFinished: Boolean,
    val lastAttemptDurationInSeconds: Int,
    val externalId: String?,
    val description: String?,
    val dataSetNumber: Int?,
    val features: List<String>,
    val featureIds: List<Int>,
    val tagIds: List<Int>,
    val priority: TestCasePriority?,
    val behavior: TestCaseBehavior?,
    val kind: Kind,
    val flakiness: Flakiness
) {

    override fun toString(): String = "$name#$deviceName"

    public companion object
}
