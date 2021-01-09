package com.avito.android.gradle.profile

import org.gradle.util.CollectionUtils
import java.util.ArrayList
import java.util.LinkedHashMap

/**
 * See [ProfileEventAdapter] to understand limitations.
 *
 * Root container for profile information about a build.  This includes summary
 * information about the overall build timing and collection of project specific
 * information.  All timing information is stored as milliseconds since epoch times.
 *
 * Setters are expected to be called in the following order:
 *
 *  - setProfilingStarted
 *  - setBuildStarted *
 *  - setSettingsEvaluated *
 *  - setProjectsLoaded *
 *  - setProjectsEvaluated
 *  - setBuildFinished
 */
class BuildProfile {

    private val projects = LinkedHashMap<String, ProjectProfile>()

    /**
     * Should be set with a time as soon as possible after startup.
     */
    var profilingStarted: Long = 0

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.buildStarted]
     * callback.
     */
    var buildStarted: Long = 0

    private var settingsEvaluated: Long = 0
    private var projectsLoaded: Long = 0
    private var projectsEvaluated: Long = 0
    private var buildFinished: Long = 0

    var isSuccessful: Boolean = false

    val projectConfiguration: CompositeOperation<Operation>
        get() {
            var operations: MutableList<Operation> = ArrayList()
            for (projectProfile in projects.values) {
                operations.add(projectProfile.configurationOperation)
            }
            operations = CollectionUtils.sort(operations, Operation.slowestFirst())
            return CompositeOperation(operations)
        }

    /**
     * Get the elapsed time (in mSec) between the start of profiling and the buildStarted event.
     */
    @Deprecated("Inaccurate value because of manual buildStarted invocation")
    val elapsedStartup: Long
        get() = buildStarted - profilingStarted

    /**
     * Get the total elapsed time (in mSec) between the start of profiling and the buildFinished event.
     */
    val elapsedTotal: Long
        get() = buildFinished - profilingStarted

    /**
     * Get the elapsed time (in mSec) between the buildStarted event and the settingsEvaluated event.
     * Note that this will include processing of buildSrc as well as the settings file.
     */
    @Deprecated("Can't intercept setting evaluated event")
    val elapsedSettings: Long
        get() = settingsEvaluated - buildStarted

    /**
     * Get the elapsed time (in mSec) between the settingsEvaluated event and the projectsLoaded event.
     */
    @Deprecated("Can't intercept projects loaded event")
    val elapsedProjectsLoading: Long
        get() = projectsLoaded - settingsEvaluated

    /**
     * Get the elapsed time (in mSec) between the projectsLoaded event and the projectsEvaluated event.
     */
    @Deprecated("Can't intercept projects loaded event")
    val elapsedProjectsConfiguration: Long
        get() = projectsEvaluated - projectsLoaded

    /**
     * Get the total task execution time from all projects.
     */
    val elapsedTotalExecutionTime: Long
        get() {
            var result: Long = 0
            for (projectProfile in projects.values) {
                result += projectProfile.elapsedTime
            }
            return result
        }

    /**
     * Total time to initialize and configure the project to run tasks.
     * An analog of **Build scan > Performance > Build > Initialization & configuration**
     */
    val initWithConfigurationTimeMs: Long
        get() {
            val firstTask = getProjects()
                .flatMap { it.getTasks() }
                .minByOrNull { it.startTime } ?: return 0

            return firstTask.startTime - profilingStarted
        }

    /**
     * Get the profiling container for the specified project
     *
     * @param projectPath to look up
     */
    fun getProjectProfile(projectPath: String): ProjectProfile {
        var result: ProjectProfile? = projects[projectPath]
        if (result == null) {
            result = ProjectProfile(projectPath)
            projects[projectPath] = result
        }
        return result
    }

    /**
     * Get a list of the profiling containers for all projects
     *
     * @return list
     */
    fun getProjects(): List<ProjectProfile> {
        return CollectionUtils.sort(projects.values, Operation.slowestFirst())
    }

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.settingsEvaluated]
     * callback.
     */
    fun setSettingsEvaluated(settingsEvaluated: Long) {
        this.settingsEvaluated = settingsEvaluated
    }

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.projectsLoaded]
     * callback.
     */
    fun setProjectsLoaded(projectsLoaded: Long) {
        this.projectsLoaded = projectsLoaded
    }

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.projectsEvaluated]
     * callback.
     */
    fun setProjectsEvaluated(projectsEvaluated: Long) {
        this.projectsEvaluated = projectsEvaluated
    }

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.buildFinished]
     * callback.
     */
    fun setBuildFinished(buildFinished: Long) {
        this.buildFinished = buildFinished
    }
}
