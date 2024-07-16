package com.avito.test.summary

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

internal object MinimalTestSummaryProject {

    const val appName = "app"

    fun builder(testSummaryDestination: File): TestProjectGenerator {
        return TestProjectGenerator(
            plugins = plugins {
                id(testSummaryPluginId)
                id("com.avito.android.gradle-logger")
            },
            imports = listOf(
                "import com.avito.report.model.Team",
                "import com.avito.reportviewer.model.ReportCoordinates",
                "import com.avito.alertino.model.AlertinoRecipient",
            ),
            buildGradleExtra = """
                testSummary {
                    register("$appName") {
                        buildUrl.set("someUrl")
                        currentBranch.set("someBranch")
                        
                        alertino.alertinoEndpoint.set("https://localhost")
                        alertino.alertinoTemplate.set("template")
                        alertino.alertinoTemplate.set("text")
                        alertino.summaryChannel.set(AlertinoRecipient("#someChannel"))
                        alertino.reserveChannel.set(AlertinoRecipient("#someChannel"))
                        alertino.testSummaryDestination.set(File("${testSummaryDestination.absolutePath}"))
                        
                        reportViewer.url.set("someUrl")
                        reportViewer.reportsHost.set("someUrl")
                        reportViewer.reportCoordinates.set(
                            ReportCoordinates("somePlanSlug", "someJobSlug", "someRunId")
                        )
                    }
                }
            """.trimIndent(),
            useKts = true,
            modules = listOf(AndroidAppModule(name = appName))
        )
    }
}
