package com.avito.android.proguard_guard.configuration

import proguard.Configuration
import proguard.ConfigurationParser
import java.io.File

internal fun parseConfiguration(configurationFile: File): Configuration {
    val configuration = Configuration()
    ConfigurationParser(configurationFile, System.getProperties()).run {
        try {
            parse(configuration)
        } finally {
            close()
        }
    }
    return configuration
}
