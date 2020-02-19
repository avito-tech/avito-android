package com.avito.instrumentation.rerun

import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@Suppress("UnstableApiUsage")
@CacheableTask
abstract class BuildOnTargetCommitForTestTask @Inject constructor(
    objects: ObjectFactory,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @Input
    val appPath = objects.property<String>()

    @Input
    val testedVariant = objects.property<String>()

    @Input
    val versionName = objects.property<String>()

    @Input
    val versionCode = objects.property<Int>()

    @Input
    val targetCommit = objects.property<String>()

    @Input
    val repoSshUrl = objects.property<String>()

    @Input
    val shouldFailBuild = objects.property<Boolean>()

    @Internal
    val tempDir: DirectoryProperty = objects.directoryProperty()

    @Internal
    val stubForTest = objects.property<Boolean>()

    @OutputFile
    val mainApk: RegularFileProperty = objects.fileProperty()

    @OutputFile
    val testApk: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun doWork() {
        if (stubForTest.get()) {
            mainApk.asFile.get().writeText("stub")
            testApk.asFile.get().writeText("stub")
        } else {
            // TODO: use new API
            @Suppress("DEPRECATION")
            workerExecutor.submit(BuildOnTargetCommitForTest::class.java) { workerConfiguration ->
                workerConfiguration.isolationMode = IsolationMode.NONE
                workerConfiguration.setParams(
                    BuildOnTargetCommitForTest.Params(
                        shouldFailBuild = shouldFailBuild.get(),
                        logger = ciLogger,
                        gitAccess = GitAccess.SshAccess(repoSshUrl.get()),
                        tempDir = tempDir.asFile.get(),
                        targetCommit = targetCommit.get(),
                        appPath = appPath.get(),
                        variant = testedVariant.get(),
                        versionName = versionName.get(),
                        versionCode = versionCode.get(),
                        buildScan = project.gradle.startParameter.isBuildScan
                    )
                )
            }
        }
    }
}
