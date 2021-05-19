package com.avito.report.model

import com.avito.android.test.annotations.TestCaseBehavior
import com.avito.android.test.annotations.TestCasePriority

public sealed class AndroidTest : TestStaticData {

    /**
     * Тест должен быть запущен, или даже запускался,
     * но мы не смогли собрать его артефакты или упали во время их парсинга.
     * Такой тест ближе всего к Failed, но мы хотим различать их
     */
    public class Lost(
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
        public val startTime: Long,
        public val lastSignalTime: Long,
        public val stdout: String,
        public val stderr: String,
        public val incident: Incident?
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

        override fun toString(): String {
            return "Lost[name=$name; device=$device;]"
        }

        public companion object {

            /**
             * Lost instance without any info. Could be only in case of test suite inconsistency
             */
            public fun createWithoutInfo(
                testStaticData: TestStaticData,
                currentTimeSec: Long
            ): Lost {
                return fromTestStaticData(
                    testStaticData = testStaticData,
                    startTime = currentTimeSec,
                    lastSignalTime = currentTimeSec,
                    stdout = "",
                    stderr = "",
                    incident = null
                )
            }

            public fun fromTestStaticData(
                testStaticData: TestStaticData,
                startTime: Long,
                lastSignalTime: Long,
                stdout: String,
                stderr: String,
                incident: Incident?
            ): Lost = Lost(
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
    public class Skipped(
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
        public val skipReason: String,
        public val reportTime: Long
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

        override fun toString(): String {
            return "Skipped[name=$name; device=$device; reason=$skipReason]"
        }

        public companion object {

            /**
             * @param reportTime т.к. тест пропущен и не имеет периода выполнения, указываем время отправки в репорт
             */
            public fun fromTestMetadata(
                testStaticData: TestStaticData,
                skipReason: String,
                reportTime: Long
            ): Skipped = Skipped(
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
    public class Completed(
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
        public val stdout: String,
        public val stderr: String
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

        override fun toString(): String {
            return if (incident == null) {
                "Success[name=$name; device=$device]"
            } else {
                "Failed[name=$name; device=$device; error=${incident.errorMessage}]"
            }
        }

        public companion object {

            public fun create(
                testStaticData: TestStaticData,
                testRuntimeData: TestRuntimeData,
                stdout: String,
                stderr: String
            ): Completed = Completed(
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
