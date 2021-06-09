package com.avito.runner.config

import java.io.Serializable

public data class QuotaConfigurationData(
    val retryCount: Int,
    val minimumSuccessCount: Int,
    val minimumFailedCount: Int
) : Serializable
