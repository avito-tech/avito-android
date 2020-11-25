package com.avito.report.model

sealed class Stability {

    abstract val attemptsCount: Int
    abstract val successCount: Int

    data class Unknown(
        override val attemptsCount: Int,
        override val successCount: Int
    ) : Stability()

    data class Stable(
        override val attemptsCount: Int,
        override val successCount: Int
    ) : Stability()

    data class Flaky(
        override val attemptsCount: Int,
        override val successCount: Int
    ) : Stability()

    data class Failing(
        override val attemptsCount: Int
    ) : Stability() {
        override val successCount: Int = 0
    }
}
