package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask

internal class GitConfigurator(
    private val gitResolver: GitResolver
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        task.gitBranch.set(gitResolver.getGitBranch())
        task.gitCommit.set(gitResolver.getGitCommit())
    }
}
