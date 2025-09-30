package com.avito.android.instant_feedback.internal.config

import com.avito.android.Result

internal interface ConfigFactory {
    fun create(): Result<Config>
}
