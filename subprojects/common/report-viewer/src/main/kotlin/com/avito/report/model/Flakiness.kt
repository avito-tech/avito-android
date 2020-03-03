package com.avito.report.model

sealed class Flakiness {

    class Flaky(val reason: String) : Flakiness()

    object Stable : Flakiness()

}