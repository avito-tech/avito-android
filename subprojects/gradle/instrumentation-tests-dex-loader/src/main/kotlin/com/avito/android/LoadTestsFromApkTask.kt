package com.avito.android

import com.avito.android.check.AllChecks
import com.avito.android.check.TestSignatureCheck
import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
@Suppress("UnstableApiUsage")
class LoadTestsFromApkTask @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout
) : DefaultTask() {

    @InputDirectory
    val testApk = objects.directoryProperty()

    @OutputFile
    val testsInApkFile = objects.directoryProperty()
        .convention(layout.buildDirectory)
        .file("tests-in-apk.json")

    @TaskAction
    fun doWork() {
        val testSuiteLoader: TestSuiteLoader = TestSuiteLoaderImpl()
        val apkFile = testApk.get().getApkOrThrow()
        val checks: TestSignatureCheck = AllChecks()
        val testsInApk = testSuiteLoader.loadTestSuite(
            file = apkFile,
            testSignatureCheck = checks // todo extract checks somehow
        ).get()

        testsInApkFile.writeTestsInApk(testsInApk)
    }
}
