package com.avito.android

import com.avito.impact.changes.ChangeType
import com.avito.impact.changes.ChangesDetector
import com.avito.impact.changes.GitChangesDetector
import com.avito.impact.changes.IgnoreSettings
import com.avito.instrumentation.impact.KotlinClassesFinder
import com.avito.instrumentation.impact.KotlinClassesFinder.Companion.KOTLIN_FILE_EXTENSION
import com.avito.logger.LoggerFactory
import com.avito.utils.rewriteNewLineList
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property

internal class FindChangedTestsAction(
    private val rootDir: Directory,
    private val targetCommit: Property<String>,
    private val androidTestDir: DirectoryProperty,
    private val changedTestsFile: RegularFile,
    loggerFactory: LoggerFactory,
) {
    private val logger = loggerFactory.create("FindChangedTestsAction")

    private val kotlinClassesFinder: KotlinClassesFinder = KotlinClassesFinder.create()

    /**
     * Change types considered as "Changed" in context of test execution strategies
     */
    private val changeTypes = arrayOf(
        ChangeType.ADDED,
        ChangeType.MODIFIED,
        ChangeType.COPIED
    )

    fun execute() {

        val changesDetector: ChangesDetector = GitChangesDetector(
            gitRootDir = rootDir.asFile,
            targetCommit = targetCommit.get(),
            ignoreSettings = IgnoreSettings(emptySet()),
        )

        val androidTestDir = androidTestDir.get().asFile

        changesDetector.computeChanges(
            targetDirectory = androidTestDir,
            excludedDirectories = emptyList()
        ).map { changedFiles ->
            changedFiles.asSequence()
                .filter { it.changeType in changeTypes }
                .filter { it.file.extension == KOTLIN_FILE_EXTENSION }
                .flatMap { kotlinClassesFinder.findClasses(it.file) }
                .map { it.toString() }
                .toList()
        }.fold(
            { changedTestNames ->
                changedTestsFile
                    .asFile
                    .rewriteNewLineList(changedTestNames)
            },
            { throwable ->
                logger.warn("Can't compute changed tests", throwable)
            }
        )
    }
}
