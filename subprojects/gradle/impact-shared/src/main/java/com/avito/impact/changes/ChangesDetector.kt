package com.avito.impact.changes

import com.avito.utils.logging.CILogger
import com.avito.utils.runCommand
import org.funktionale.tries.Try
import java.io.File

interface ChangesDetector {

    /**
     * Determines changed files(relative paths) under provided [targetDirectory], minus [excludedDirectories]
     *
     * @return list of changed files; could fail on git problems
     */
    fun computeChanges(targetDirectory: File, excludedDirectories: Iterable<File> = emptyList()): Try<List<ChangedFile>>
}

class ChangesDetectorStub(private val reason: String) : ChangesDetector {

    override fun computeChanges(targetDirectory: File, excludedDirectories: Iterable<File>): Try<List<ChangedFile>> {
        return Try.Failure(IllegalStateException(reason))
    }
}

class GitChangesDetector(
    private val gitRootDir: File,
    private val targetCommit: String,
    private val ignoreSettings: IgnoreSettings,
    private val logger: CILogger
) : ChangesDetector {

    private val cache: MutableMap<Key, Try<List<ChangedFile>>> = mutableMapOf()
    private val gitDiffWithTargetBranch by lazy { gitDiffWith() }

    init {
        require(gitRootDir.exists()) { "Directory ${gitRootDir.canonicalPath} doesn't exist" }
        require(gitRootDir.canRead()) { "Directory ${gitRootDir.canonicalPath} is not readable" }
    }

    /**
     * Determines changed files(relative paths) under provided [targetDirectory], minus [excludedDirectories]
     *
     * @return list of changed files; could fail on git problems
     */
    override fun computeChanges(targetDirectory: File, excludedDirectories: Iterable<File>): Try<List<ChangedFile>> {
        return cache.getOrPut(Key(targetDirectory, excludedDirectories)) {
            computeChangedFiles(targetDirectory, excludedDirectories)
        }
    }

    /**
     * Just a git diff between HEAD and [targetCommit]
     */
    private fun gitDiffWith(): Try<Sequence<ChangedFile>> {
        return runCommand(
            command = "git diff --name-status $targetCommit",
            workingDirectory = gitRootDir
        ).map { output: String ->
            output.lineSequence()
                .filterNot { it.isBlank() }
                .map { line ->
                    line.parseGitDiffLine().map { it.asChangedFile(gitRootDir) }
                }
                .filter { it is Try.Success }
                .map { it as Try.Success<ChangedFile> }
                .map { it.get() }
        }
    }

    private fun computeChangedFiles(
        targetDirectory: File,
        excludedDirectories: Iterable<File> = emptyList()
    ): Try<List<ChangedFile>> {
        if (!targetDirectory.toPath().startsWith(gitRootDir.toPath())) {
            return Try.Failure(IllegalArgumentException("$targetDirectory must be inside $gitRootDir"))
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
                    val isPathIgnored = pattern != null
                    if (isPathIgnored) {
                        logger.debug("File ${changedFile.relativePath} is ignored due to pattern $pattern")
                    }
                    isPathIgnored
                }
                .toList()
        }
    }

    private data class Key(val targetDirectory: File, val excludedDirectories: Iterable<File>)
}

fun newChangesDetector(rootDir: File, targetCommit: String?, logger: CILogger): ChangesDetector {
    val ignoreFile = File(rootDir, ".tia_ignore")
    val settings = readIgnoreSettings(ignoreFile)

    return if (targetCommit.isNullOrBlank()) {
        ChangesDetectorStub("targetCommit branch was not set")
    } else {
        GitChangesDetector(
            gitRootDir = rootDir,
            targetCommit = targetCommit,
            ignoreSettings = settings,
            logger = logger
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
