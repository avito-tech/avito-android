package com.avito.test.summary

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

public class TestSummaryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val objects = target.objects
        val testSummaryContainer = objects.domainObjectContainer(TestSummaryAppExtension::class.java) { name ->
            objects.newInstance(TestSummaryAppExtension::class.java, name)
        }
        target.extensions.add(testSummaryExtensionName, testSummaryContainer)

        if (!target.isRoot()) {
            val problem = Problem(
                shortDescription = "TestSummaryPlugin should be applied to root project",
                context = "TestSummaryPlugin applied to ${target.path}",
                because = "Summary tasks now registered on root project, one per PlanSlug+JobSlug key, " +
                    "to make cross-app dependency on single report possible"
            )
            throw problem.asRuntimeException()
        }

        testSummaryContainer.all { extension ->

            val appName = extension.name

            target.tasks.register<MarkReportAsSourceTask>(markReportForTmsTaskName(appName)) {
                group = "ci"
                description = "Marks $appName test report as source of truth for Avito TMS"

                this.reportCoordinates.set(extension.reportViewer.reportCoordinates)
                this.reportsHost.set(extension.reportViewer.reportsHost)
            }

            target.tasks.register<TestSummaryTask>(testSummaryTaskName(appName)) {
                group = "ci"

                val reportCoordinates = extension.reportViewer.reportCoordinates.get()

                description = "Sends $appName test summary report to slack for [" +
                    "planSlug:${reportCoordinates.planSlug}, " +
                    "jobSlug:${reportCoordinates.jobSlug}" +
                    "]"

                this.reportViewerExtension.set(extension.reportViewer)
                this.slackExtension.set(extension.slack)
                this.alertinoExtension.set(extension.alertino)
                this.buildUrl.set(extension.buildUrl)
            }
        }
    }
}
