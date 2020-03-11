package com.avito.instrumentation

import org.intellij.lang.annotations.Language

@Language("Groovy")
val minimalInstrumentationPluginConfiguration = """//
android {
    defaultConfig {
        testInstrumentationRunner "runner"
    }
}

instrumentation {
    output = project.file("outputs").path
    reportApiUrl = "stub"
    reportApiFallbackUrl = "stub"
    reportViewerUrl = "stub"
    sentryDsn = "stub"
    slackToken = "stub"
    fileStorageUrl = "stub"
    registry = "stub"
    
    instrumentationParams = [
    "planSlug": "plan",
    "jobSlug" : "job",
    "runId"   : "1234"
    ]
    
    configurations {
        functional {
        }
    }
}
            """.trimIndent()
