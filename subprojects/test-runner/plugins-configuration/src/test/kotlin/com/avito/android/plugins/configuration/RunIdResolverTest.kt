package com.avito.android.plugins.configuration

import com.avito.git.GitState
import com.avito.utils.gradle.envArgs
import com.google.common.truth.Truth.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

internal class RunIdResolverTest {

    /**
     * Guards MBSA-2360 (a): on a local build `getRunId()` must return a stable git-independent
     * runId WITHOUT reading git. getRunId() is realized eagerly at configuration time by consumers
     * (e.g. avito-android's `resolveRunId()` feeding AGP testInstrumentationRunnerArguments), so a
     * git read here would invalidate the configuration cache on every commit / branch switch.
     *
     * The GitResolver is backed by a provider that throws if realized — proving git is never read.
     */
    @Test
    fun `getRunId on local build - returns local identifier without reading git`() {
        val project = ProjectBuilder.builder().build()
        project.extensions.extraProperties["avito.build"] = "local"
        val gitMustNotBeRead = GitResolver(
            project.provider<GitState> { error("git must not be read at configuration time on a local build") }
        )
        val buildEnvResolver = BuildEnvResolver(project.provider { project.envArgs })

        val runId = RunIdResolver(gitMustNotBeRead, buildEnvResolver).getRunId()

        assertThat(runId.toReportViewerFormat()).startsWith("local")
    }
}
