package com.avito.android

import com.avito.impact.changes.ChangesDetector
import com.avito.impact.changes.GitChangesDetector
import com.avito.impact.changes.IgnoreSettings
import com.avito.utils.logging.ciLogger
import com.avito.utils.rewriteNewLineList
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import java.io.File
import javax.inject.Inject

class FindModifiedTestsTask @Inject constructor(
    extension: ModifiedTestsFinderExtension,
    objects: ObjectFactory
) : DefaultTask() {

    @Input
    val targetCommit = extension.targetCommit

    @Input
    val tests = objects.listProperty<TestInApk>()

    @Suppress("UnstableApiUsage")
    @OutputFile
    val modifiedTestsFile: Provider<RegularFile> = extension.output.file("modified-tests.txt")

    @TaskAction
    fun doWork() {
        val changesDetector: ChangesDetector =
            GitChangesDetector(
                gitRootDir = project.rootDir,
                targetCommit = targetCommit.get(),
                ignoreSettings = IgnoreSettings(emptySet()),
                logger = ciLogger
            )

        val action = FindModifiedTestsAction(changesDetector)
        val androidTestDir = File(project.projectDir, "src/androidTest")

        val modifiedTests = action.find(
            androidTestDir = androidTestDir,
            tests = tests.get()
        ).get()

        modifiedTestsFile.get().asFile.rewriteNewLineList(modifiedTests)
    }
}
