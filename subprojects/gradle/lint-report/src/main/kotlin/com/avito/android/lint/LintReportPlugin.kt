package com.avito.android.lint

import com.avito.android.isAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class LintReportPlugin : Plugin<Project> {

    override fun apply(app: Project) {
        check(app.isAndroidApp()) { "Plugin must be applied to an application but was applied to ${app.path}" }

        app.extensions.create<LintReportExtension>("lintReport")

        // see [com.avito.android.lint.slack.LintReportToSlackTaskFactory] and [LintCheck] in ci steps
    }
}
