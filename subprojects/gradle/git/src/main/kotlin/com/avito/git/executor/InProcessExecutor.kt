package com.avito.git.executor

import com.avito.android.Result
import com.avito.utils.ProcessRunner
import java.io.File
import java.time.Duration

/**
 * Is used for CI scenarios only. Should be removed.
 * TODO: migrate to [GradleCompatibleExecutor]
 */
internal class InProcessExecutor(
    rootDir: File,
) : GitExecutor {

    private val processRunner = ProcessRunner.create(rootDir)

    override fun git(command: String): Result<String> {
        return processRunner.run(command = "git $command", timeout = Duration.ofSeconds(30))
    }
}
