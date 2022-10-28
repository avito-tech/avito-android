package com.avito.test.summary

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins

internal object MinimalTestSummaryProject {

    const val appName = "app"

    fun builder(): TestProjectGenerator {
        return TestProjectGenerator(
            plugins = plugins {
                id(testSummaryPluginId)
            },
            imports = listOf(
                "import com.avito.report.model.Team",
                "import com.avito.reportviewer.model.ReportCoordinates",
                "import com.avito.slack.model.SlackChannel",
                "import com.avito.alertino.model.AlertinoRecipient",
            ),
            buildGradleExtra = """
                testSummary {
                    register("$appName") {
                        buildUrl.set("someUrl")
                        currentBranch.set("someBranch")
                        
                        slack.token.set("someToken")
                        slack.workspace.set("someWorkspace")
                        slack.username.set("someUsername")
                        slack.unitToChannelMapping.set(
                            mapOf(Team("someTeam") to SlackChannel("someId", "#someChannel"))
                        )
                        slack.summaryChannel.set(SlackChannel("someId", "#someChannel"))
                        slack.reserveChannel.set(SlackChannel("someId", "#someChannel"))
                        slack.mentionOnFailures.set(setOf("someChannel"))
                        
                        alertino.alertinoEndpoint.set("https://localhost")
                        alertino.alertinoTemplate.set("template")
                        alertino.alertinoTemplate.set("text")
                        alertino.unitToChannelMapping.set(
                            mapOf(Team("someTeam") to AlertinoRecipient("#someChannel"))
                        )
                        alertino.summaryChannel.set(AlertinoRecipient("#someChannel"))
                        alertino.reserveChannel.set(AlertinoRecipient("#someChannel"))
                        alertino.mentionOnFailures.set(setOf("@username"))
                        
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
