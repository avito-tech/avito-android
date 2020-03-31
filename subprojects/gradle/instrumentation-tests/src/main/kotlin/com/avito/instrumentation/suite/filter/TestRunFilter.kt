package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName
import com.avito.report.model.Status

interface TestRunFilter {

    fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): Verdict

    sealed class Verdict {

        object Run : Verdict()

        //todo нужно показывать все причины (тест не прошел через несколько фильтров)
        sealed class Skip : Verdict() {

            abstract val description: String

            class NotHasPrefix(prefix: String) : Skip() {
                override val description: String =
                    "test doesn't match the prefix: $prefix"
            }

            class NotAnnotatedWith(annotations: Collection<String>) : Skip() {
                override val description: String =
                    "test doesn't contain any annotation from list: $annotations"
            }

            abstract class ByPreviousTestRun(
                expectTests: String,
                actual: String
            ) : Skip() {

                object TestRunIsAbsent : ByPreviousTestRun(
                    expectTests = "test had a test run",
                    actual = "test didn't have a test run"
                )

                class TestRunIsSucceed(status: Status): ByPreviousTestRun(
                    expectTests = "test had failed test run",
                    actual = "test had test run in status: $status"
                )

                override val description: String =
                    "Skip because expect $expectTests, but actual $actual"
            }

            object NotSpecifiedInFile : Skip() {
                override val description: String =
                    "Test wasn't in execution file"
            }

            object Ignored : Skip() {
                override val description: String = "test contains @Ignore annotation"
            }

            object NotSpecifiedInTestsToRun : Skip() {
                override val description: String = "test wasn't in execution list"
            }

            object SkippedBySdk : Skip() {
                override val description: String = "test was marked by @SkipOnSdk annotation"
            }
        }
    }
}
