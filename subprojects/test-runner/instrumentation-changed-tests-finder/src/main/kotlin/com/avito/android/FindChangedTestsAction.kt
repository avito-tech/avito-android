package com.avito.android

import com.avito.impact.changes.ChangeType
import com.avito.impact.changes.ChangesDetector
import com.avito.impact.changes.GitChangesDetector
import com.avito.impact.changes.IgnoreSettings
import com.avito.instrumentation.impact.KotlinClassesFinder
import com.avito.instrumentation.impact.KotlinClassesFinder.Companion.KOTLIN_FILE_EXTENSION
import com.avito.utils.rewriteNewLineList
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

public abstract class FindChangedTestsAction : WorkAction<FindChangedTestsAction.Params> {

    private val kotlinClassesFinder: KotlinClassesFinder = KotlinClassesFinder.create()

    /**
     * Change types considered as "Changed" in context of test execution strategies
     */
    private val changeTypes = arrayOf(
        ChangeType.ADDED,
        ChangeType.MODIFIED,
        ChangeType.COPIED
    )

    override fun execute() {

        val changesDetector: ChangesDetector = GitChangesDetector(
            gitRootDir = parameters.rootDir.get().asFile,
            targetCommit = parameters.targetCommit.get(),
            ignoreSettings = IgnoreSettings(emptySet()),
        )

        val androidTestDir = parameters.androidTestDir.get().asFile

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
                parameters.changedTestsFile
                    .get()
                    .asFile
                    .rewriteNewLineList(changedTestNames)
            },
            { throwable -> throw throwable }
        )
    }

    public interface Params : WorkParameters {
        public val rootDir: RegularFileProperty
        public val targetCommit: Property<String>
        public val androidTestDir: DirectoryProperty
        public val changedTestsFile: RegularFileProperty
    }
}
