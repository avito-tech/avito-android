package com.avito.module.internal.dependencies

import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.MultipleSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.NoSuitableApps
import com.avito.module.internal.dependencies.FindAndroidAppTaskAction.Verdict.OneSuitableApp

internal class FindAndroidAppTaskAdvisor {

    fun giveAdvice(verdict: FindAndroidAppTaskAction.Verdict): String {
        return when (verdict) {
            is OneSuitableApp -> "In your project is only one suitable app ${verdict.projectWithDeps.project.path}"
            is MultipleSuitableApps -> verdict.advice()
            is NoSuitableApps -> verdict.advice()
        }
    }

    private fun NoSuitableApps.advice(): String {
        val resultsSortedByRelevance = result
            .groupBy { it.missedModules.size }
            .mapValues { (_, results) ->
                results.sortedBy { it.dependencies.size }
            }
            .map { it.key to it.value }
            .sortedBy { it.first }

        return buildString {
            appendLine("There are no suitable Android apps")
            appendLine("Apps are sorted by missing modules:")
            resultsSortedByRelevance.forEach { (_, results) ->
                results.forEach { result ->
                    append("App ${result.project.path}")
                    appendLine(" contains ${result.presentedModules} but missed ${result.missedModules}")
                }
            }
        }.trimIndent()
    }

    private fun MultipleSuitableApps.advice(): String {
        val minimumDependencies = requireNotNull(
            projectsWithDeps.minByOrNull { it.dependencies.size }
        ).dependencies.size
        val mostSuitableApps = projectsWithDeps.filter { it.dependencies.size == minimumDependencies }
        val advice = when {
            mostSuitableApps.size == 1 -> {
                val app = mostSuitableApps[0]
                Advice(app.project.path, "it has the least dependencies in graph size=${app.dependencies.size}")
            }
            else -> {
                val app = mostSuitableApps[0]
                Advice(
                    projects = mostSuitableApps
                        .joinToString(
                            prefix = "[",
                            postfix = "]"
                        ) { it.project.path },
                    reason = "they have the least dependencies in graph size=${app.dependencies.size}"
                )
            }
        }
        return """
            |There are multiple suitable apps ${projectsWithDeps.map { it.project.path }}
            |You should prefer ${advice.projects} because ${advice.reason}
            """.trimMargin()
    }

    private class Advice(
        val projects: String,
        val reason: String
    )
}
