package com.avito.android.test.util

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.util.Locale

fun getContextWithLocaleByLanguage(language: String): Context {
    val baseContext = ApplicationProvider.getApplicationContext<Application>()
    val configuration = baseContext.resources.configuration
    configuration.setLocale(Locale(language))
    return baseContext.createConfigurationContext(configuration)
}
