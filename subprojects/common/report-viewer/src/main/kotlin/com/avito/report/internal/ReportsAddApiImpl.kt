package com.avito.report.internal

import com.avito.android.Result
import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority
import com.avito.report.ReportsAddApi
import com.avito.report.internal.model.Incident
import com.avito.report.internal.model.IncidentElement
import com.avito.report.internal.model.RfcRpcRequest
import com.avito.report.internal.model.RpcResult
import com.avito.report.internal.model.Step
import com.avito.report.internal.model.TestStatus
import com.avito.report.internal.model.Video
import com.avito.report.model.AndroidTest
import com.avito.report.model.Flakiness
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.Status
import com.avito.report.model.team

internal class ReportsAddApiImpl(private val client: JsonRpcClient) : ReportsAddApi {

    /**
     * status
     * buildId null означает локальную сборку, значения в create недостаточно, потому что новые билды могут писать
     *         в тот же отчет и нужно сохранить знание о новых  buildId
     *         todo продумать способ не гонять лишние байты с каждым тестом
     *
     * @return todo id вместо string
     */
    override fun addTests(
        reportCoordinates: ReportCoordinates,
        buildId: String?,
        tests: Collection<AndroidTest>
    ): Result<List<String>> {
        return Result.tryCatch {
            val requests = tests.map { test ->
                when (test) {
                    is AndroidTest.Skipped -> createAddFullRequest(
                        reportCoordinates = reportCoordinates,
                        buildId = buildId,
                        test = test,
                        status = Status.Skipped(test.skipReason),
                        logcat = "",
                        incident = null,
                        video = null,
                        startTime = null,
                        endTime = null,
                        dataSetData = null,
                        preconditionList = emptyList(), // todo в теории можем достать проанализировав код теста,
                        stepList = emptyList() // todo в теории можем достать проанализировав код теста
                    )
                    is AndroidTest.Lost -> createAddFullRequest(
                        reportCoordinates = reportCoordinates,
                        buildId = buildId,
                        test = test,
                        // if incident available let backend decide status based on incident type
                        status = if (test.incident == null) Status.Lost else null,
                        logcat = test.logcat,
                        incident = test.incident?.toInternal(),
                        video = null,
                        startTime = test.startTime,
                        endTime = test.lastSignalTime,
                        dataSetData = null,
                        preconditionList = emptyList(), // todo в теории можем достать проанализировав код теста,
                        stepList = emptyList() // todo в теории можем достать проанализировав код теста
                    )
                    is AndroidTest.Completed -> createAddFullRequest(
                        reportCoordinates = reportCoordinates,
                        buildId = buildId,
                        test = test,
                        // определяется на бэке для success/fail по наличию incident,
                        // отправляем остальные статусы самостоятельно
                        status = null,
                        logcat = test.logcat,
                        incident = test.incident?.toInternal(),
                        video = test.video?.let { Video(it.fileAddress, it.format) },
                        startTime = test.startTime,
                        endTime = test.endTime,
                        dataSetData = test.dataSetData,
                        preconditionList = test.preconditions.map { it.toInternal() },
                        stepList = test.steps.map { it.toInternal() }
                    )
                }
            }

            when {
                requests.size == 1 -> listOf(client.jsonRpcRequest<RpcResult<String>>(requests[0]).result)
                requests.size > 1 -> client.batchRequest<List<RpcResult<String>>>(requests).map { it.result }
                else -> emptyList()
            }
        }
    }

    override fun addTest(reportCoordinates: ReportCoordinates, buildId: String?, test: AndroidTest): Result<String> {
        return addTests(reportCoordinates, buildId, listOf(test)).map { it.first() }
    }

    private fun createAddFullRequest(
        reportCoordinates: ReportCoordinates,
        buildId: String?,
        test: AndroidTest,
        status: Status?,
        logcat: String,
        incident: Incident?,
        video: Video?,
        startTime: Long?,
        endTime: Long?,
        dataSetData: Map<String, String>?,
        preconditionList: List<Step>,
        stepList: List<Step>
    ): RfcRpcRequest {
        val preparedData = mutableMapOf<String, Any>()
        val messageList = mutableListOf<String>()
        val groupList = mutableListOf(test.name.team.name)
        val reportData = mutableMapOf<String, Any>()

        val kind = test.kind
        groupList.add(kind.tmsId)

        val report = mutableMapOf(
            "test_class" to test.name.className,
            "test_name" to if (test.dataSetNumber != null) {
                "${test.name.methodName}#${test.dataSetNumber}"
            } else {
                test.name.methodName
            },
            "message_list" to messageList,
            // собираем из всех нужных данных список лейблов(тэгов), по которым можно будет фильтровать тесты в RV
            "group_list" to groupList,
            "precondition_step_list" to preconditionList,
            "test_case_step_list" to stepList
        )

        if (test.testCaseId != null) report["test_case_id"] = test.testCaseId.toString()

        val description = test.description
        if (!description.isNullOrBlank()) report["description"] = description

        if (video != null) report["video"] = video
        if (startTime != null) report["start_time"] = startTime
        if (endTime != null) report["end_time"] = endTime

        /**
         * Обычно тесты с датасетами группируются в ReportViewer по testCaseId,
         * но также нужно группировать тесты с датасетами без testCaseId
         * Для них введено специальное поле grouping_key, в андроиде это всегда название класса
         */
        if (test.dataSetNumber != null) {
            if (test.testCaseId == null) {
                report["grouping_key"] = test.name.className
            }
            // Для консистентности можно также посылать здесь testCaseId, но бэкенд умеет обрабатывать это
        }

        require(!(test.dataSetNumber == null && !dataSetData.isNullOrEmpty())) {
            "DataSet data without DataSetNumber doesn't make sense!"
        }

        if (test.dataSetNumber != null) {
            report["data_set_number"] = test.dataSetNumber.toString()
            if (!dataSetData.isNullOrEmpty()) {
                report["data_set"] = dataSetData
            }
        }

        if (!buildId.isNullOrBlank()) preparedData["tc_build"] = buildId

        val externalId = test.externalId
        if (!externalId.isNullOrBlank()) {
            preparedData["external_id"] = if (test.dataSetNumber != null) {
                "${externalId}_${test.dataSetNumber}"
            } else {
                externalId
            }
        }
        if (test.tagIds.isNotEmpty()) preparedData["tag_id"] = test.tagIds
        if (test.featureIds.isNotEmpty()) preparedData["feature_id"] = test.featureIds

        val priority = test.priority
        preparedData["priority_id"] = priority?.tmsValue ?: TestCasePriority.NORMAL.tmsValue

        val behavior = test.behavior
        preparedData["behavior_id"] = behavior?.tmsValue ?: TestCaseBehavior.UNDEFINED.tmsValue

        if (status is Status.Skipped) {
            // посылаем в 2 места skipReason, message_list нужен для отображения,
            // а prepared_data, чтобы получить с помощью RunTest.List
            preparedData["skip_reason"] = status.reason
            messageList.add(status.reason)
        }

        if (!buildId.isNullOrBlank()) {
            reportData["build_id_set"] = mapOf("\$fillSet" to listOf(buildId))
        }

        when (val flakiness = test.flakiness) {
            is Flakiness.Flaky -> {
                preparedData["is_flaky"] = true
                preparedData["flaky_reason"] = flakiness.reason
            }
            is Flakiness.Stable ->
                preparedData["is_flaky"] = false
        }

        val params = mutableMapOf(
            "plan_slug" to reportCoordinates.planSlug,
            "job_slug" to reportCoordinates.jobSlug,
            "run_id" to reportCoordinates.runId,
            "environment" to test.device.name,
            "report" to report,
            // тут происходит магия "с помощью оператора добавляется в массив новое значение билда"
            "report_data" to reportData,
            // todo onlyIf present
            "console" to mapOf(
                "stdout" to logcat,
                "stderr" to ""
            ),
            "prepared_data" to preparedData,
            "kind" to kind.tmsId
        )

        if (incident != null) params["incident"] = incident
        if (status != null) params["status"] = serializeStatus(status).intValue

        return RfcRpcRequest(
            method = "RunTest.AddFull",
            params = params
        )
    }

    private fun serializeStatus(status: Status): TestStatus {
        return when (status) {
            Status.Success,
            is Status.Failure -> throw IllegalArgumentException(
                "Should not send Success/Failure test status explicitly, " +
                    "it will be calculated on report backend"
            )

            is Status.Skipped -> TestStatus.SKIP
            Status.Manual -> TestStatus.MANUAL
            Status.Lost -> TestStatus.LOST
        }
    }

    private fun com.avito.report.model.Incident.toInternal(): Incident {
        return Incident(
            type = type.toInternal(),
            timestamp = timestamp,
            trace = trace,
            chain = chain.map { it.toInternal() },
            entryList = entryList
        )
    }

    private fun com.avito.report.model.Incident.Type.toInternal(): Incident.Type {
        return when (this) {
            com.avito.report.model.Incident.Type.INFRASTRUCTURE_ERROR -> Incident.Type.INFRASTRUCTURE_ERROR
            com.avito.report.model.Incident.Type.ASSERTION_FAILED -> Incident.Type.ASSERTION_FAILED
        }
    }

    private fun com.avito.report.model.IncidentElement.toInternal(): IncidentElement {
        return IncidentElement(
            message = message,
            code = code,
            type = type,
            origin = origin,
            className = className,
        )
    }

    private fun com.avito.report.model.Step.toInternal(): Step {
        return Step(
            timestamp = timestamp,
            number = number,
            title = title,
            entryList = entryList
        )
    }
}
