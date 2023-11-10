package com.avito.runner.service.worker.device.adb.instrumentation

import com.android.annotations.VisibleForTesting
import com.avito.cli.Notification
import com.avito.runner.model.TestCaseRun
import com.avito.runner.service.worker.model.InstrumentationTestCaseRun
import com.avito.test.model.TestName
import rx.Observable
import java.io.File

internal interface InstrumentationTestCaseRunParser {

    fun parse(instrumentationOutput: Observable<Notification>): Observable<InstrumentationTestCaseRun>

    class Impl : InstrumentationTestCaseRunParser {

        override fun parse(
            instrumentationOutput: Observable<Notification>
        ): Observable<InstrumentationTestCaseRun> {
            return readInstrumentationOutput(instrumentationOutput)
                .asTests()
        }

        @VisibleForTesting
        internal fun readInstrumentationOutput(
            instrumentationOutput: Observable<Notification>
        ): Observable<InstrumentationEntry> {
            data class Result(val buffer: String = "", val readyForProcessing: Boolean = false)

            return instrumentationOutput
                // `INSTRUMENTATION_CODE: -1` is last line printed by instrumentation, even if 0 tests were run.
                // if invalid command last line starts with Error:
                .takeUntil { notification ->
                    when (notification) {
                        is Notification.Output -> {
                            val line = notification.line.trim()
                            line.startsWith("INSTRUMENTATION_CODE") || line.startsWith("Error:")
                        }

                        // We use Notification.Exit only for cases when instrumentation unexpectedly exit
                        // For example, when emulator process crashed during test
                        is Notification.Exit -> true
                    }
                }
                .scan(Result()) { previousResult, notification ->
                    when (notification) {
                        is Notification.Output -> {
                            val newLine = notification.line.trim()
                            val buffer = when (previousResult.readyForProcessing) {
                                true -> newLine
                                false -> "${previousResult.buffer}${System.lineSeparator()}$newLine"
                            }

                            val isEntryEnd = newLine.startsWith("INSTRUMENTATION_STATUS_CODE")
                                || newLine.startsWith("INSTRUMENTATION_CODE")
                                || newLine.startsWith("Error:")

                            Result(buffer = buffer, readyForProcessing = isEntryEnd)
                        }

                        is Notification.Exit -> throw RuntimeException(
                            """
                                |Unexpected instrumentation exit:
                                |${notification.output.ifEmpty { "<empty instrumentation output>" }}
                            """.trimMargin()
                        )
                    }
                }
                .filter { it.readyForProcessing }
                .scan<InstrumentationEntry?>(null) { previous, new ->
                    val entry = parseInstrumentationEntry(new.buffer)

                    when {
                        // Check current test doesn't have test field
                        entry is InstrumentationEntry.InstrumentationTestEntry &&
                            entry.test.isEmpty() -> {

                            // Check previous test entry is Start and has test name
                            if (previous is InstrumentationEntry.InstrumentationTestEntry &&
                                previous.statusCode == InstrumentationEntry.InstrumentationTestEntry.StatusCode.Start &&
                                previous.test.isNotEmpty()
                            ) {
                                // Copy test field from previous Start entry
                                entry.copy(
                                    id = previous.id,
                                    test = previous.test,
                                    clazz = previous.clazz
                                )
                            } else {
                                throw Exception(
                                    buildString {
                                        append("Something wrong with instrumentation output: ")
                                        append("Current entry doesn't have test field and previous entry is not run ")
                                        append("or doesn't have test field too.")
                                    }
                                )
                            }
                        }
                        // Attach additional output to actual test entry
                        entry is InstrumentationEntry.InstrumentationMacrobenchmarkOutputEntry -> {
                            check(previous is InstrumentationEntry.InstrumentationTestEntry) {
                                buildString {
                                    append("Wrong instrumentation output order: ")
                                    append("additional output entry (code=2) must ")
                                    append("be followed by tests start entry (code = 1).")
                                }
                            }
                            previous.copy(
                                statusCode = entry.statusCode,
                                macrobenchmarkOutputFile = entry.outputFilePath
                            )
                        }
                        else -> entry
                    }
                }
                .filterNotNull()
        }

        private fun Observable<InstrumentationEntry>.asTests(): Observable<InstrumentationTestCaseRun> {
            data class Result(
                val entries: List<InstrumentationEntry.InstrumentationTestEntry> = emptyList(),
                val tests: List<InstrumentationTestCaseRun> = emptyList(),
                val totalTestsCount: Int = 0
            )

            return this
                .scan(Result()) { previousResult, newEntry ->
                    val entries = if (newEntry is InstrumentationEntry.InstrumentationTestEntry) {
                        previousResult.entries + newEntry
                    } else {
                        previousResult.entries
                    }

                    val tests: List<InstrumentationTestCaseRun> =
                        if (newEntry is InstrumentationEntry.InstrumentationResultEntry
                            && newEntry.statusCode == InstrumentationEntry.InstrumentationResultEntry.StatusCode.Error
                        ) {
                            if (entries.isEmpty()) {
                                listOf(
                                    InstrumentationTestCaseRun.FailedOnStartTestCaseRun(
                                        message = newEntry.getError()
                                    )
                                )
                            } else {
                                val now = System.currentTimeMillis()

                                entries.map {
                                    InstrumentationTestCaseRun.CompletedTestCaseRun(
                                        name = TestName(it.clazz, it.test),
                                        result = TestCaseRun.Result.Failed.InRun(errorMessage = newEntry.getError()),
                                        timestampStartedMilliseconds = now,
                                        timestampCompletedMilliseconds = now
                                    )
                                }
                            }
                        } else {
                            entries.findTests()
                        }

                    Result(
                        // pass all unused entries to further processing
                        entries = entries
                            .filter { entry ->
                                tests
                                    .asSequence()
                                    .filterIsInstance<InstrumentationTestCaseRun.CompletedTestCaseRun>()
                                    .firstOrNull {
                                        it.name.className == entry.clazz && it.name.methodName == entry.test
                                    } == null
                            },
                        tests = tests,
                        totalTestsCount = previousResult.totalTestsCount + tests.size
                    )
                }
                .takeUntil {
                    if (it.entries.count { it.current == it.numTests } == 2) {
                        if (it.totalTestsCount < it.entries.first().numTests) {
                            throw IllegalStateException("Less tests were emitted than Instrumentation reported: $it")
                        }

                        true
                    } else {
                        false
                    }
                }
                .filter { it.tests.isNotEmpty() }
                .flatMap { Observable.from(it.tests) }
                .onErrorReturn { throwable ->
                    InstrumentationTestCaseRun.FailedOnInstrumentationParsing(
                        message = "Failed while parsing instrumentation",
                        throwable = throwable
                    )
                }
        }

        private fun List<InstrumentationEntry.InstrumentationTestEntry>.findTests(): List<InstrumentationTestCaseRun> {
            return mapIndexed { index, first ->
                val second = this
                    .subList(index + 1, this.size)
                    .firstOrNull {
                        first.clazz == it.clazz
                            && first.test == it.test
                            && first.current == it.current
                            && first.statusCode != it.statusCode
                    }

                if (second == null) null else first to second
            }
                .filterNotNull()
                .map { (first, second) ->
                    InstrumentationTestCaseRun.CompletedTestCaseRun(
                        name = TestName(first.clazz, first.test),
                        result = when (second.statusCode) {
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.Ok ->
                                TestCaseRun.Result.Passed.Regular
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.MacrobenchmarkOutput ->
                                TestCaseRun.Result.Passed.WithMacrobenchmarkOutputs(
                                    outputFiles = listOfNotNull(second.macrobenchmarkOutputFile)
                                        .map { File(it).toPath() }
                                )

                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.Ignored ->
                                TestCaseRun.Result.Ignored

                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.Failure,
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.AssumptionFailure ->
                                TestCaseRun.Result.Failed.InRun(errorMessage = second.stack)

                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.Start ->
                                throw IllegalStateException(
                                    "Unexpected status code [${second.statusCode}] " +
                                        "in second entry, ($first, $second)"
                                )
                        },
                        timestampStartedMilliseconds = first.timestampMilliseconds,
                        timestampCompletedMilliseconds = second.timestampMilliseconds,
                    )
                }
        }

        private fun String.substringBetween(first: String, vararg second: String): String {
            val indexOfFirst = indexOf(first)

            if (indexOfFirst < 0) {
                return ""
            }

            val startIndex = indexOfFirst + first.length

            val secondIndex = second.asSequence().map {
                indexOf(it, startIndex)
            }.find { it != -1 }

            val endIndex = secondIndex ?: length

            return substring(startIndex, endIndex)
        }

        private fun String.parseInstrumentationStatusValue(key: String): String =
            substringBetween("INSTRUMENTATION_STATUS: $key=", "INSTRUMENTATION_STATUS", "INSTRUMENTATION_STATUS_CODE")
                .trim()

        private fun String.parseInstrumentationResultValue(key: String): String =
            substringBetween("INSTRUMENTATION_RESULT: $key=", "INSTRUMENTATION_RESULT", "INSTRUMENTATION_CODE")
                .trim()

        private fun String.isTestEntry(): Boolean = contains("INSTRUMENTATION_STATUS_CODE")

        private fun parseInstrumentationEntry(str: String): InstrumentationEntry {
            if (str.isTestEntry()) {
                val statusCode =
                    str.substringBetween("INSTRUMENTATION_STATUS_CODE: ", "INSTRUMENTATION_STATUS")
                        .trim()
                        .toInt()
                        .let { code ->
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.values()
                                .firstOrNull { it.code == code }
                        }
                        .let { code ->
                            when (code) {
                                null -> throw IllegalStateException(
                                    "Unknown test result status code [$code] ($str)"
                                )

                                else -> code
                            }
                        }
                if (statusCode == InstrumentationEntry.InstrumentationTestEntry.StatusCode.MacrobenchmarkOutput) {
                    val filePath = str
                        .parseInstrumentationStatusValue("additionalTestOutputFile_baseline-profile")
                    check(filePath.isNotBlank()) {
                        "Received status code indicating additional output files, but file path was empty."
                    }
                    return InstrumentationEntry.InstrumentationMacrobenchmarkOutputEntry(
                        outputFilePath = filePath,
                    )
                }

                return InstrumentationEntry.InstrumentationTestEntry(
                    numTests = str.parseInstrumentationStatusValue("numtests").toInt(),
                    stream = str.parseInstrumentationStatusValue("stream"),
                    stack = str.parseInstrumentationStatusValue("stack"),
                    id = str.parseInstrumentationStatusValue("id"),
                    test = str.parseInstrumentationStatusValue("test"),
                    clazz = str.parseInstrumentationStatusValue("class"),
                    current = str.parseInstrumentationStatusValue("current").toInt(),
                    statusCode = statusCode,
                    timestampMilliseconds = System.currentTimeMillis()
                )
            } else {
                return InstrumentationEntry.InstrumentationResultEntry(
                    shortMessage = str.parseInstrumentationResultValue("shortMsg"),
                    longMessage = str.parseInstrumentationResultValue("longMsg"),
                    statusCode = str.substringBetween("INSTRUMENTATION_CODE: ", "\n")
                        .trim()
                        .toInt()
                        .let { code ->
                            InstrumentationEntry.InstrumentationResultEntry.StatusCode.values()
                                .firstOrNull { it.code == code }
                        }
                        .let { code ->
                            when (code) {
                                null -> throw IllegalStateException(
                                    "Unknown instrumentation result status code [$code] ($str)"
                                )
                                else -> code
                            }
                        },
                    timestampMilliseconds = System.currentTimeMillis()
                )
            }
        }

        /**
         * Returns [Observable] with non-null generic type T. Returned observable filter out null values
         */
        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> Observable<T?>.filterNotNull(): Observable<T> = filter { it != null } as Observable<T>
    }
}
