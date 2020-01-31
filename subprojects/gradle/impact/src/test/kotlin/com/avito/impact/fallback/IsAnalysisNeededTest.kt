package com.avito.impact.fallback

import com.avito.git.Branch
import com.avito.git.GitLocalStateStub
import com.avito.git.GitState
import com.avito.git.GitStateStub
import com.avito.impact.plugin.ImpactAnalysisExtension
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.isInstanceOf
import org.gradle.api.internal.provider.Providers
import org.junit.jupiter.api.Test

class IsAnalysisNeededTest {

    @Test
    fun `no git = skip`() {
        val result = isAnalysisNeededWith(gitState = null)

        assertThat(result).isInstanceOf<IsAnalysisNeededResult.Skip>()
        assertThat((result as IsAnalysisNeededResult.Skip).reason).contains("git is not available")
    }

    @Test
    fun `skip in config = skip`() {
        val result = isAnalysisNeededWith(
            config = impactAnalysisExtension { skipAnalysis = true },
            gitState = gitState(
                current = branch("feature"),
                target = branch("develop")
            )
        )

        assertThat(result).isInstanceOf<IsAnalysisNeededResult.Skip>()
        assertThat((result as IsAnalysisNeededResult.Skip).reason).contains("skipAnalysis=true")
    }

    @Test
    fun `fail to get currentBranch = skip`() {
        val result = isAnalysisNeededWith(
            gitState = gitState(
                current = branch(invalidBranchName),
                target = branch("develop")
            )
        )

        assertThat(result).isInstanceOf<IsAnalysisNeededResult.Skip>()
        assertThat((result as IsAnalysisNeededResult.Skip).reason).contains("current branch")
    }

    @Test
    fun `fail to get targetBranch = skip`() {
        val result = isAnalysisNeededWith(
            gitState = gitState(
                current = branch("feature"),
                target = branch(invalidBranchName)
            )
        )

        assertThat(result).isInstanceOf<IsAnalysisNeededResult.Skip>()
        assertThat((result as IsAnalysisNeededResult.Skip).reason).contains("target branch")
    }

    @Test
    fun `currentBranch protected = skip`() {
        val result = isAnalysisNeededWith(
            config = impactAnalysisExtension { protectedBranches = setOf("master", "release/*") },
            gitState = gitState(
                current = branch("release/32.3"),
                target = branch("develop")
            )
        )

        assertThat(result).isInstanceOf<IsAnalysisNeededResult.Skip>()
        assertThat((result as IsAnalysisNeededResult.Skip).reason).contains("branch is protected")
    }

    @Test
    fun `targetBranch protected = skip`() {
        val result = isAnalysisNeededWith(
            config = impactAnalysisExtension { protectedBranches = setOf("master", "release/*") },
            gitState = gitState(
                current = branch("feature"),
                target = branch("release/32.3")
            )
        )

        assertThat(result).isInstanceOf<IsAnalysisNeededResult.Skip>()
        assertThat((result as IsAnalysisNeededResult.Skip).reason).contains("branch is protected")
    }

    @Test
    fun `same branches = skip in CI`() {
        val result = isAnalysisNeededWith(
            gitState = gitState(
                current = branch("ATBT-2233"),
                target = branch("ATBT-2233")
            )
        )

        assertWithMessage("Run all checks on the branch itself is a common case in CI")
            .that(result).isInstanceOf<IsAnalysisNeededResult.Skip>()
        assertThat((result as IsAnalysisNeededResult.Skip).reason).contains("running on target branch")
    }

    @Test
    fun `same branches = run locally on feature branch`() {
        val result = isAnalysisNeededWith(
            gitState = gitLocalState(
                current = branch("ATBT-2233"),
                target = branch("ATBT-2233")
            )
        )

        assertWithMessage("We may have uncommitted changes")
            .that(result).isInstanceOf<IsAnalysisNeededResult.Run>()
    }

    @Test
    fun `same branches = run locally on default branch`() {
        val result = isAnalysisNeededWith(
            gitState = gitLocalState(
                current = branch("develop"),
                target = branch("develop")
            )
        )

        assertWithMessage("We may have uncommitted changes")
            .that(result).isInstanceOf<IsAnalysisNeededResult.Run>()
    }

    @Test
    fun `different branches = run`() {
        val result = isAnalysisNeededWith(
            gitState = gitState(
                current = branch("ATBT-2233"),
                target = branch("develop")
            )
        )

        assertThat(result).isInstanceOf<IsAnalysisNeededResult.Run>()
    }

    private fun isAnalysisNeededWith(
        config: ImpactAnalysisExtension = ImpactAnalysisExtension(),
        gitState: GitState?
    ): IsAnalysisNeededResult = isAnalysisNeeded(
        config = config,
        gitState = if (gitState == null) Providers.notDefined() else Providers.of(gitState)
    )

    private fun gitState(current: Branch, target: Branch?): GitState = GitStateStub(
        currentBranch = current,
        targetBranch = target
    )

    private fun gitLocalState(current: Branch, target: Branch?): GitState = GitLocalStateStub(
        currentBranch = current,
        targetBranch = target
    )

    private fun branch(name: String) = Branch(name, "commit")

    private fun impactAnalysisExtension(action: ImpactAnalysisExtension.() -> Unit) =
        ImpactAnalysisExtension().apply(action)
}

private const val invalidBranchName = ""
