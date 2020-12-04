package com.avito.android

import com.avito.impact.changes.ChangesDetector
import com.avito.impact.changes.GitChangesDetector
import com.avito.impact.changes.IgnoreSettings
import com.avito.instrumentation.impact.KotlinClassesFinderImpl
import com.avito.utils.logging.ciLogger
import com.avito.utils.rewriteNewLineList
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

class FindChangedTestsTask @Inject constructor(
    extension: ModifiedTestsFinderExtension,
    objects: ObjectFactory
) : DefaultTask() {

    @Input
    val targetCommit = objects.property<String>()

    @Suppress("UnstableApiUsage")
    @InputFile
    val allTestsInApk = objects.fileProperty()

    @OutputFile
    val modifiedTestsFile: Provider<RegularFile> = extension.output.file("changed-tests.txt")

    @TaskAction
    fun doWork() {
        val changesDetector: ChangesDetector =
            GitChangesDetector(
                gitRootDir = project.rootDir,
                targetCommit = targetCommit.get(),
                ignoreSettings = IgnoreSettings(emptySet()),
                logger = ciLogger
            )

        val action = FindChangedTestsAction(
            changesDetector = changesDetector,
            kotlinClassesFinder = KotlinClassesFinderImpl()
        )

        val androidTestDir = File(project.projectDir, "src/androidTest")

        val modifiedTestNames = action.find(
            androidTestDir = androidTestDir,
            allTestsInApk = allTestsInApk.readTestsInApk().map { it.testName }
        ).get()

        modifiedTestsFile.get().asFile.rewriteNewLineList(modifiedTestNames)
    }
}
