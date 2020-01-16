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
