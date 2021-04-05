package com.avito.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority

sealed class AndroidTest : TestStaticData {

    /**
     * Тест должен быть запущен, или даже запускался,
     * но мы не смогли собрать его артефакты или упали во время их парсинга.
     * Такой тест ближе всего к Failed, но мы хотим различать их
     */
    class Lost(
        override val name: TestName,
        override val device: DeviceName,
        override val description: String?,
        override val testCaseId: Int?,
        override val dataSetNumber: Int?,
        override val externalId: String?,
        override val featureIds: List<Int>,
        override val tagIds: List<Int>,
        override val priority: TestCasePriority?,
        override val behavior: TestCaseBehavior?,
        override val kind: Kind,
        override val flakiness: Flakiness,
        val startTime: Long,
        val lastSignalTime: Long,
        val stdout: String,
        val stderr: String,
        val incident: Incident?
    ) : AndroidTest() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Lost) return false

            if (name != other.name) return false
            if (device != other.device) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + device.hashCode()
            return result
        }

        companion object {
            fun fromTestMetadata(
                testStaticData: TestStaticData,
                startTime: Long,
                lastSignalTime: Long,
                stdout: String,
                stderr: String,
                incident: Incident?
            ) = Lost(
                name = testStaticData.name,
                device = testStaticData.device,
                description = testStaticData.description,
                testCaseId = testStaticData.testCaseId,
                dataSetNumber = testStaticData.dataSetNumber,
                externalId = testStaticData.externalId,
                featureIds = testStaticData.featureIds,
                tagIds = testStaticData.tagIds,
                priority = testStaticData.priority,
                behavior = testStaticData.behavior,
                kind = testStaticData.kind,
                flakiness = testStaticData.flakiness,
                startTime = startTime,
                lastSignalTime = lastSignalTime,
                stdout = stdout,
                stderr = stderr,
                incident = incident
            )
        }
    }

    /**
     * Тест который мы сознательно решили не запускать: импакт анализ или @Ignore или еще какая-то причина
     */
    class Skipped(
        override val name: TestName,
        override val device: DeviceName,
        override val description: String?,
        override val testCaseId: Int?,
        override val dataSetNumber: Int?,
        override val externalId: String?,
        override val featureIds: List<Int>,
        override val tagIds: List<Int>,
        override val priority: TestCasePriority?,
        override val behavior: TestCaseBehavior?,
        override val kind: Kind,
        override val flakiness: Flakiness,
        val skipReason: String,
        val reportTime: Long
    ) : AndroidTest() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Skipped) return false

            if (name != other.name) return false
            if (device != other.device) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + device.hashCode()
            return result
        }

        companion object {
            /**
             * @param reportTime т.к. тест пропущен и не имеет периода выполнения, указываем время отправки в репорт
             */
            fun fromTestMetadata(
                testStaticData: TestStaticData,
                skipReason: String,
                reportTime: Long
            ) = Skipped(
                name = testStaticData.name,
                device = testStaticData.device,
                description = testStaticData.description,
                testCaseId = testStaticData.testCaseId,
                dataSetNumber = testStaticData.dataSetNumber,
                externalId = testStaticData.externalId,
                featureIds = testStaticData.featureIds,
                tagIds = testStaticData.tagIds,
                priority = testStaticData.priority,
                behavior = testStaticData.behavior,
                kind = testStaticData.kind,
                flakiness = testStaticData.flakiness,
                skipReason = skipReason,
                reportTime = reportTime
            )
        }
    }

    /**
     * Тест который завершился (прошел или упал)
     */
    class Completed(
        override val incident: Incident?,
        override val dataSetData: Map<String, String>,
        override val video: Video?,
        override val preconditions: List<Step>,
        override val steps: List<Step>,
        override val name: TestName,
        override val device: DeviceName,
        override val description: String?,
        override val testCaseId: Int?,
        override val dataSetNumber: Int?,
        override val externalId: String?,
        override val featureIds: List<Int>,
        override val tagIds: List<Int>,
        override val priority: TestCasePriority?,
        override val behavior: TestCaseBehavior?,
        override val kind: Kind,
        override val startTime: Long,
        override val endTime: Long,
        override val flakiness: Flakiness,
        val stdout: String,
        val stderr: String
    ) : AndroidTest(), TestRuntimeData {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Completed) return false

            if (name != other.name) return false
            if (device != other.device) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + device.hashCode()
            return result
        }

        companion object {
            fun create(
                testStaticData: TestStaticData,
                testRuntimeData: TestRuntimeData,
                stdout: String,
                stderr: String
            ) = Completed(
                name = testStaticData.name,
                device = testStaticData.device,
                description = testStaticData.description,
                testCaseId = testStaticData.testCaseId,
                dataSetNumber = testStaticData.dataSetNumber,
                externalId = testStaticData.externalId,
                featureIds = testStaticData.featureIds,
                tagIds = testStaticData.tagIds,
                priority = testStaticData.priority,
                behavior = testStaticData.behavior,
                kind = testStaticData.kind,
                incident = testRuntimeData.incident,
                dataSetData = testRuntimeData.dataSetData,
                video = testRuntimeData.video,
                preconditions = testRuntimeData.preconditions,
                steps = testRuntimeData.steps,
                startTime = testRuntimeData.startTime,
                endTime = testRuntimeData.endTime,
                flakiness = testStaticData.flakiness,
                stdout = stdout,
                stderr = stderr
            )
        }
    }
}

/**
 * Только данные которые мы можем получить выполнив тест
 */
interface TestRuntimeData {

    val incident: Incident?

    /**
     * Must be in seconds
     */
    val startTime: Long

    /**
     * Must be in seconds
     */
    val endTime: Long
    val dataSetData: Map<String, String>
    val video: Video?
    val preconditions: List<Step>
    val steps: List<Step>
}

data class TestRuntimeDataPackage(
    override val incident: Incident?,
    override val startTime: Long,
    override val endTime: Long,
    override val dataSetData: Map<String, String>,
    override val video: Video?,
    override val preconditions: List<Step>,
    override val steps: List<Step>
) : TestRuntimeData {

    companion object
}

/**
 * Только базовая информация, которую мы можем спарсить из dex, не запуская тест
 */
interface TestStaticData {
    val name: TestName
    val device: DeviceName
    val description: String?
    val testCaseId: Int?
    val dataSetNumber: Int?
    val externalId: String?
    val featureIds: List<Int>
    val tagIds: List<Int>
    val priority: TestCasePriority?
    val behavior: TestCaseBehavior?
    val kind: Kind
    val flakiness: Flakiness
}

data class TestStaticDataPackage(
    override val name: TestName,
    override val device: DeviceName,
    override val description: String?,
    override val testCaseId: Int?,
    override val dataSetNumber: Int?,
    override val externalId: String?,
    override val featureIds: List<Int>,
    override val tagIds: List<Int>,
    override val priority: TestCasePriority?,
    override val behavior: TestCaseBehavior?,
    override val kind: Kind,
    override val flakiness: Flakiness
) : TestStaticData {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestStaticData) return false

        if (name != other.name) return false
        if (device != other.device) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + device.hashCode()
        return result
    }

    companion object {

        fun fromSimpleRunTest(simpleRunTest: SimpleRunTest) = TestStaticDataPackage(
            name = TestName(simpleRunTest.name),
            device = DeviceName(simpleRunTest.deviceName),
            description = simpleRunTest.description,
            testCaseId = simpleRunTest.testCaseId,
            dataSetNumber = simpleRunTest.dataSetNumber,
            externalId = simpleRunTest.externalId,
            featureIds = simpleRunTest.featureIds,
            tagIds = simpleRunTest.tagIds,
            priority = simpleRunTest.priority,
            behavior = simpleRunTest.behavior,
            kind = simpleRunTest.kind,
            flakiness = simpleRunTest.flakiness
        )
    }
}
