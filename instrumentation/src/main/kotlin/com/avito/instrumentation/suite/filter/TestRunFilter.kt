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
                override val description: String = "тест не соответствует переданному префиксу: $prefix"
            }

            class NotAnnotatedWith(annotations: Collection<String>) : Skip() {
                override val description: String = "тест не содержит аннотацию из списка: $annotations"
            }

            class OnlyFailed(status: Status) : Skip() {
                override val description: String =
                    "запускаем только неуспешные тесты в прошлом прогоне, а этот тест: ${status.javaClass.simpleName}"
            }

            object NotSpecifiedInFile : Skip() {
                override val description: String = "тест не указан явно в файле переданном для запуска"
            }

            object Ignored : Skip() {
                override val description: String = "тест содержит аннотацию @Ignore"
            }

            object NotSpecifiedInTestsToRun : Skip() {
                override val description: String = "тест не указан явно в списке для запуска"
            }

            object SkippedBySdk : Skip() {
                override val description: String = "тест помечен аннотацией SkipOnSdk"
            }

            object SkippedByDownsampling : Skip() {
                override val description: String = "тесту просто не повезло, пропущен из-за downsampling"
            }
        }
    }
}
