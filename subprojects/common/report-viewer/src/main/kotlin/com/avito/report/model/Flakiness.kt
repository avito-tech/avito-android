package com.avito.report.model

/**
 * https://avito-tech.github.io/avito-android/docs/test/flakytests/
 */
sealed class Flakiness {

    class Flaky(val reason: String) : Flakiness()

    object Stable : Flakiness()
}
