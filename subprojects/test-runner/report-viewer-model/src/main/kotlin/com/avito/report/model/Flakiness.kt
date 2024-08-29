package com.avito.report.model

/**
 * https://avito-tech.github.io/avito-android/docs/test/flakytests/
 */
public sealed class Flakiness {

    public data class Flaky(public val reason: String) : Flakiness()

    public data object Stable : Flakiness()
}
