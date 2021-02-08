package com.avito.instrumentation.configuration.target.scheduling.quota

import com.avito.instrumentation.reservation.request.QuotaConfigurationData

public open class QuotaConfiguration {

    public var retryCount: Int = 0
    public var minimumSuccessCount: Int = 0
    public var minimumFailedCount: Int = 0

    public fun validate() {
        val runsCount = retryCount + 1
        val minimumRequiredRunsCount = minimumSuccessCount + minimumFailedCount

        require(retryCount >= 0) { "Retry count in quota must be positive or 0" }

        require(minimumSuccessCount >= 0) { "minimumSuccessCount must be positive or 0" }
        require(minimumFailedCount >= 0) { "minimumFailedCount must be positive or 0" }
        require(minimumRequiredRunsCount > 0) {
            "minimumRequiredRunsCount (minimumSuccessCount + minimumFailedCount) must be greater than zero"
        }

        require(runsCount >= minimumRequiredRunsCount) {
            "Runs count (retryCount + 1) must be >= minimumSuccessCount + minimumFailedCount"
        }
    }

    public fun data(): QuotaConfigurationData = QuotaConfigurationData(
        retryCount = retryCount,
        minimumSuccessCount = minimumSuccessCount,
        minimumFailedCount = minimumFailedCount
    )
}
