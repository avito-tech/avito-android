package com.avito.report.model

public sealed class Stability {

    public abstract val attemptsCount: Int
    public abstract val successCount: Int

    public data class Unknown(
        override val attemptsCount: Int,
        override val successCount: Int
    ) : Stability()

    public data class Stable(
        override val attemptsCount: Int,
        override val successCount: Int
    ) : Stability()

    public data class Flaky(
        override val attemptsCount: Int,
        override val successCount: Int
    ) : Stability()

    public data class Failing(
        override val attemptsCount: Int
    ) : Stability() {
        override val successCount: Int = 0
    }
}
