package com.avito.module.internal.dependencies

import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.MultipleSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.NoSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.OneSuitableApp

internal class FindAndroidAppTaskAdvisor {

    fun giveAdvice(verdict: FindAndroidAppTaskAction.Verdict): String {
        return when (verdict) {
            is OneSuitableApp -> "In your project is only one suitable app ${verdict.project.first.path}"
            is MultipleSuitableApps -> verdict.advice()
            is NoSuitableApps -> "There are no suitable Android apps"
        }
    }

    private fun MultipleSuitableApps.advice(): String {
        val minimumDependencies = requireNotNull(
            projects.minByOrNull { it.second.size }
        ).second.size
        val mostSuitableApps = projects.filter { it.second.size == minimumDependencies }
        val advice = when {
            mostSuitableApps.size == 1 -> {
                val app = mostSuitableApps[0]
                Advice(app.first.path, "it has least dependencies in graph size=${app.second.size}")
            }
            else -> {
                val app = mostSuitableApps[0]
                Advice(
                    projects = mostSuitableApps
                        .joinToString(
                            prefix = "[",
                            postfix = "]"
                        ) { it.first.path },
                    reason = "they have least dependencies in graph size=${app.second.size}"
                )
            }
        }
        return """
            |In your project are multiple suitable apps ${projects.map { it.first.path }}
            |You should prefer ${advice.projects} because ${advice.reason}
            """.trimMargin()
    }

    private class Advice(
        val projects: String,
        val reason: String
    )
}
