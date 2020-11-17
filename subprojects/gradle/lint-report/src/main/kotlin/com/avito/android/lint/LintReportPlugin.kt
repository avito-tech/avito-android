package com.avito.android.lint

import com.avito.android.isAndroidApp
import com.avito.android.withAndroidApp
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class LintReportPlugin : Plugin<Project> {

    override fun apply(app: Project) {
        check(app.isAndroidApp()) { "Plugin must be applied to an application but was applied to ${app.path}" }

        app.extensions.create<LintReportExtension>("lintReport")

        // see [com.avito.android.lint.slack.LintReportToSlackTaskFactory] and [LintCheck] in ci steps

        if (app.buildEnvironment is BuildEnvironment.CI) {
            // На CI гоняем только lint в application-модуле с checkDependencies=true
            setupDependencies(app)
        }

        // Локально может понадобиться запустить lint в нескольких модулях, поэтому report-таска
        // должна запускаться точно последней
    }

    private fun setupDependencies(app: Project) {
        val androidLintAccessor = AndroidLintAccessor(app)

        app.withAndroidApp { appExtension ->

            appExtension.testVariants.all { _ ->

                androidLintAccessor.taskProvider().configure {

                    // see LintWorkerApiWorkaround.md
                    it.mustRunAfter("preInstrumentation")
                }
            }
        }
    }
}
