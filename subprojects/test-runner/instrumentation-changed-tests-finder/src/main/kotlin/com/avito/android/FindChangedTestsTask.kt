package com.avito.android

import com.avito.git.GitInfoBuildService
import com.avito.gradle.worker.inMemoryWork
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * todo @CacheableTask + test for it
 */
public abstract class FindChangedTestsTask @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:Internal
    public val targetCommit: Property<String> = objects.property()

    @get:ServiceReference(GitInfoBuildService.NAME)
    public abstract val gitInfoService: Property<GitInfoBuildService>

    /**
     * The effective commit the changed-tests diff is computed against: the explicit
     * [targetCommit] when set, otherwise resolved from the target branch via [gitInfoService].
     *
     * Declared as an `@Input` (not resolved in the `@TaskAction` alone) so Gradle's up-to-date
     * check sees the real commit and re-runs the task when the target branch moves forward — even
     * if `src/androidTest` is unchanged. The git read stays in the execution phase (the value is
     * resolved during input snapshotting via the GitInfoBuildService), so it never re-enters the
     * configuration cache. See MBSA-2353.
     */
    @get:Input
    public val resolvedTargetCommit: Provider<String> =
        targetCommit.orElse(
            gitInfoService.map { service ->
                service.getGitState().targetBranch?.commit
                    ?: error(
                        "Could not resolve target commit for changed-tests detection. " +
                            "Either set the `targetCommit` property explicitly, pass `-PtargetBranch=<ref>`, " +
                            "or ensure your git state strategy populates `targetBranch`."
                    )
            }
        )

    @InputDirectory
    public val androidTestDir: DirectoryProperty = objects.directoryProperty()
        .convention(layout.projectDirectory.dir("src/androidTest"))

    @OutputFile
    public val changedTestsFile: Provider<RegularFile> = objects.directoryProperty()
        .convention(layout.buildDirectory)
        .file("changed-test-classes.txt")

    @TaskAction
    public fun doWork() {
        val commit = resolvedTargetCommit.get()
        workerExecutor.inMemoryWork {
            FindChangedTestsAction(
                project.rootProject.layout.projectDirectory,
                commit,
                androidTestDir,
                changedTestsFile.get(),
                GradleLoggerPlugin.getLoggerFactory(this)
            ).execute()
        }
    }
}
