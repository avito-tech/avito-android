package com.avito.android.plugin.build_metrics

import com.avito.android.plugin.build_metrics.internal.gradle.requestedtasks.BuildExecutionHistory
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class BuildMetricsRepoKeyTest {

    @Test
    fun `repo key generation - returns sanitized origin url - when origin url is available`() {
        val key = buildMetricsRepoKey(
            originUrl = "git@github.com:avito/avito-android-tools.git", repoName = "ignored/repo",
        )

        assertThat(key).isEqualTo("git_github_com_avito_avito-android-tools_git")
    }

    @Test
    fun `repo key generation - returns repo name - when origin url is missing`() {
        val key = buildMetricsRepoKey(
            originUrl = null, repoName = "project/repo",
        )

        assertThat(key).isEqualTo("project_repo")
    }

    private fun buildMetricsRepoKey(originUrl: String?, repoName: String?): String =
        BuildExecutionHistory.storageKey(
            originUrl = originUrl,
            repoName = repoName,
        )
}
