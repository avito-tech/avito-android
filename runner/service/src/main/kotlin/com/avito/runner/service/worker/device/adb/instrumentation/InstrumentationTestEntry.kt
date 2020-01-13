package com.avito.runner.service.worker.device.adb.instrumentation

sealed class InstrumentationEntry {

    data class InstrumentationTestEntry(
        val numTests: Int,
        val stream: String,
        val id: String,
        val test: String,
        val clazz: String,
        val current: Int,
        val stack: String,
        val statusCode: StatusCode,
        val timestampMilliseconds: Long
    ) : InstrumentationEntry() {

        enum class StatusCode(val code: Int) {
            Start(1),
            Ok(0),
            Failure(-2),
            Ignored(-3),
            AssumptionFailure(-4)
        }
    }

    data class InstrumentationResultEntry(
        val shortMessage: String = "",
        val longMessage: String = "",
        val statusCode: StatusCode,
        val timestampMilliseconds: Long
    ) : InstrumentationEntry() {

        enum class StatusCode(val code: Int) {
            Ok(-1),
            Error(0)
        }

        fun getError() = when {
            longMessage.isNotEmpty() -> longMessage
            shortMessage.isNotEmpty() -> shortMessage
            else -> ""
        }
    }
}
