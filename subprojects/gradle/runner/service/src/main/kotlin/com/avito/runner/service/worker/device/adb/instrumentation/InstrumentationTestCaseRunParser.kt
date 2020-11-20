package com.avito.runner.service.worker.device.adb.instrumentation

import com.android.annotations.VisibleForTesting
import com.avito.runner.ProcessNotification
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.model.InstrumentationTestCaseRun
import rx.Observable

interface InstrumentationTestCaseRunParser {
    fun parse(output: Observable<ProcessNotification.Output>): Observable<InstrumentationTestCaseRun>

    class Impl : InstrumentationTestCaseRunParser {

        override fun parse(output: Observable<ProcessNotification.Output>): Observable<InstrumentationTestCaseRun> {
            return readInstrumentationOutput(output)
                .asTests()
        }

        @VisibleForTesting
        internal fun readInstrumentationOutput(output: Observable<ProcessNotification.Output>): Observable<InstrumentationEntry> {
            data class Result(val buffer: String = "", val readyForProcessing: Boolean = false)

            return output.map { it.line }
                .map { it.trim() }
                // `INSTRUMENTATION_CODE: -1` is last line printed by instrumentation, even if 0 tests were run.
                // if invalid command last line starts with Error:
                .takeUntil { it.startsWith("INSTRUMENTATION_CODE") || it.startsWith("Error:") }
                .scan(Result()) { previousResult, newLine ->
                    val buffer = when (previousResult.readyForProcessing) {
                        true -> newLine
                        false -> "${previousResult.buffer}${System.lineSeparator()}$newLine"
                    }

                    val isEntryEnd =
                        newLine.startsWith("INSTRUMENTATION_STATUS_CODE") || newLine.startsWith("INSTRUMENTATION_CODE") || newLine.startsWith(
                            "Error:"
                        )

                    Result(buffer = buffer, readyForProcessing = isEntryEnd)
                }
                .filter { it.readyForProcessing }
                .scan<InstrumentationEntry?>(null) { previous, new ->
                    val entry = parseInstrumentationEntry(new.buffer)

                    // Check current test doesn't have test field
                    if (entry is InstrumentationEntry.InstrumentationTestEntry &&
                        entry.test.isEmpty()
                    ) {

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
                                "Something wrong with instrumentation output:" +
                                    "Current entry doesn't have test field and previous entry is not run" +
                                    "or doesn't have test field too."
                            )
                        }
                    } else {
                        entry
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
                    val entries =
                        if (newEntry is InstrumentationEntry.InstrumentationTestEntry) previousResult.entries + newEntry else previousResult.entries

                    val tests: List<InstrumentationTestCaseRun> =
                        if (newEntry is InstrumentationEntry.InstrumentationResultEntry && newEntry.statusCode == InstrumentationEntry.InstrumentationResultEntry.StatusCode.Error) {
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
                                        className = it.clazz,
                                        name = it.test,
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
                                    .firstOrNull { it.className == entry.clazz && it.name == entry.test } == null
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
                        className = first.clazz,
                        name = first.test,
                        result = when (second.statusCode) {
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.Ok -> TestCaseRun.Result.Passed
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.Ignored -> TestCaseRun.Result.Ignored
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.Failure, InstrumentationEntry.InstrumentationTestEntry.StatusCode.AssumptionFailure -> TestCaseRun.Result.Failed.InRun(
                                errorMessage = second.stack
                            )
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.Start ->
                                throw IllegalStateException(
                                    "Unexpected status code [${second.statusCode}] " +
                                        "in second entry, ($first, $second)"
                                )
                        },
                        timestampStartedMilliseconds = first.timestampMilliseconds,
                        timestampCompletedMilliseconds = second.timestampMilliseconds
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
                return InstrumentationEntry.InstrumentationTestEntry(
                    numTests = str.parseInstrumentationStatusValue("numtests").toInt(),
                    stream = str.parseInstrumentationStatusValue("stream"),
                    stack = str.parseInstrumentationStatusValue("stack"),
                    id = str.parseInstrumentationStatusValue("id"),
                    test = str.parseInstrumentationStatusValue("test"),
                    clazz = str.parseInstrumentationStatusValue("class"),
                    current = str.parseInstrumentationStatusValue("current").toInt(),
                    statusCode = str.substringBetween("INSTRUMENTATION_STATUS_CODE: ", "INSTRUMENTATION_STATUS")
                        .trim()
                        .toInt()
                        .let { code ->
                            InstrumentationEntry.InstrumentationTestEntry.StatusCode.values()
                                .firstOrNull { it.code == code }
                        }
                        .let { statusCode ->
                            when (statusCode) {
                                null -> throw IllegalStateException(
                                    "Unknown test result status code [$statusCode] ($str)"
                                )
                                else -> statusCode
                            }
                        },
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
                        .let { statusCode ->
                            when (statusCode) {
                                null -> throw IllegalStateException(
                                    "Unknown instrumentation result status code [$statusCode] ($str)"
                                )
                                else -> statusCode
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
