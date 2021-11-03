package com.avito.impact.changes

import com.avito.android.Result
import com.avito.utils.ProcessRunner
import java.io.File
import java.time.Duration

public interface ChangesDetector {

    /**
     * Determines changed files(relative paths) under provided [targetDirectory], minus [excludedDirectories]
     *
     * @return list of changed files; could fail on git problems
     */
    public fun computeChanges(
        targetDirectory: File,
        excludedDirectories: Iterable<File> = emptyList()
    ): Result<List<ChangedFile>>
}

internal class ChangesDetectorStub(private val reason: String) : ChangesDetector {

    override fun computeChanges(targetDirectory: File, excludedDirectories: Iterable<File>): Result<List<ChangedFile>> {
        return Result.Failure(IllegalStateException(reason))
    }
}

public class GitChangesDetector(
    private val gitRootDir: File,
    private val targetCommit: String,
    private val ignoreSettings: IgnoreSettings
) : ChangesDetector {

    private val cache: MutableMap<Key, Result<List<ChangedFile>>> = mutableMapOf()
    private val gitDiffWithTargetBranch by lazy { gitDiffWith() }
    private val processRunner = ProcessRunner.create(gitRootDir)

    init {
        require(gitRootDir.exists()) { "Directory ${gitRootDir.canonicalPath} doesn't exist" }
        require(gitRootDir.canRead()) { "Directory ${gitRootDir.canonicalPath} is not readable" }
    }

    /**
     * Determines changed files(relative paths) under provided [targetDirectory], minus [excludedDirectories]
     *
     * @return list of changed files; could fail on git problems
     */
    override fun computeChanges(targetDirectory: File, excludedDirectories: Iterable<File>): Result<List<ChangedFile>> {
        return cache.getOrPut(Key(targetDirectory, excludedDirectories)) {
            computeChangedFiles(targetDirectory, excludedDirectories)
        }
    }

    /**
     * Just a git diff between HEAD and [targetCommit]
     */
    private fun gitDiffWith(): Result<Sequence<ChangedFile>> {
        return processRunner.run(
            command = "git diff --name-status $targetCommit",
            timeout = Duration.ofSeconds(10)
        ).map { output: String ->
            output.lineSequence()
                .filterNot { it.isBlank() }
                .map { line ->
                    line.parseGitDiffLine().map { it.asChangedFile(gitRootDir) }
                }
                .filter { it is Result.Success }
                .map { it as Result.Success<ChangedFile> }
                .map { it.getOrThrow() }
        }
    }

    private fun computeChangedFiles(
        targetDirectory: File,
        excludedDirectories: Iterable<File> = emptyList()
    ): Result<List<ChangedFile>> {
        if (!targetDirectory.toPath().startsWith(gitRootDir.toPath())) {
            return Result.Failure(IllegalArgumentException("$targetDirectory must be inside $gitRootDir"))
        }
        val targetPath = targetDirectory.toPath()

        val excludedPaths = excludedDirectories.map { it.toPath() }

        return gitDiffWithTargetBranch.map { changedFiles ->
            changedFiles
                .filter { changedFile -> changedFile.file.toPath().startsWith(targetPath) }
                .filterNot { changedFile ->
                    excludedPaths.any { changedFile.file.toPath().startsWith(it) }
                }
                .filterNot { changedFile ->
                    val pattern = ignoreSettings.match(changedFile.relativePath)
                    pattern != null
                }
                .toList()
        }
    }

    private data class Key(val targetDirectory: File, val excludedDirectories: Iterable<File>)
}

public fun newChangesDetector(
    rootDir: File,
    targetCommit: String?,
): ChangesDetector {
    val ignoreFile = File(rootDir, ".tia_ignore")
    val settings = readIgnoreSettings(ignoreFile)

    return if (targetCommit.isNullOrBlank()) {
        ChangesDetectorStub("targetCommit branch was not set")
    } else {
        GitChangesDetector(
            gitRootDir = rootDir,
            targetCommit = targetCommit,
            ignoreSettings = settings,
        )
    }
}

private fun readIgnoreSettings(settings: File): IgnoreSettings {
    val patterns: Set<String> = if (settings.exists()) {
        settings.readLines()
            .filterNot { it.isBlank() }
            .map { it.trim() }
            .filterNot { it.startsWith("#") }
            .toSet()
    } else {
        emptySet()
    }
    return IgnoreSettings(patterns)
}
