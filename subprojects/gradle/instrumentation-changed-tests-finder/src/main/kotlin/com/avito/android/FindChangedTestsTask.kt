package com.avito.android

import com.avito.impact.changes.ChangesDetector
import com.avito.impact.changes.GitChangesDetector
import com.avito.impact.changes.IgnoreSettings
import com.avito.instrumentation.impact.KotlinClassesFinderImpl
import com.avito.utils.logging.ciLogger
import com.avito.utils.rewriteNewLineList
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

@Suppress("UnstableApiUsage")
class FindChangedTestsTask @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) : DefaultTask() {

    @Input
    val targetCommit = objects.property<String>()

    @InputFile
    val androidTestDir: RegularFileProperty = objects.fileProperty()
        .convention { File(project.projectDir, "src/androidTest") }

    @InputFile
    val allTestsInApk: RegularFileProperty = objects.fileProperty()

    @OutputFile
    val modifiedTestsFile: Provider<RegularFile> = objects.directoryProperty()
        .convention(layout.buildDirectory)
        .file("changed-tests.txt")

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

        val modifiedTestNames = action.find(
            androidTestDir = androidTestDir.get().asFile,
            allTestsInApk = allTestsInApk.readTestsInApk().map { it.testName }
        ).get()

        modifiedTestsFile.get().asFile.rewriteNewLineList(modifiedTestNames)
    }
}
