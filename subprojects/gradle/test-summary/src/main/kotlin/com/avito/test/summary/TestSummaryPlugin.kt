package com.avito.test.summary

import com.avito.android.Problem
import com.avito.android.asPlainText
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class TestSummaryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.extensions.create<TestSummaryExtension>(testSummaryExtensionName)

        if (!target.isRoot()) {
            val problem = Problem(
                shortDescription = "TestSummaryPlugin should be applied to root project",
                context = "TestSummaryPlugin applied to ${target.path}",
                because = "Summary tasks now registered from CiStep on root project, one per PlanSlug+JobSlug key, " +
                    "to make cross-app dependency on single report possible"
            )
            target.logger.warn(problem.asPlainText())
        }
    }
}
