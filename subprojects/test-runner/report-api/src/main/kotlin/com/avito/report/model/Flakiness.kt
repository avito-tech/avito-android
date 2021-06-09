package com.avito.report.model

/**
 * https://avito-tech.github.io/avito-android/docs/test/flakytests/
 */
public sealed class Flakiness {

    public class Flaky(public val reason: String) : Flakiness()

    public object Stable : Flakiness()
}
