package com.avito.android.gradle.profile

import com.avito.android.gradle.profile.Operation.Companion.slowestFirst
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
public class BuildProfile {

    private val projects = LinkedHashMap<String, ProjectProfile>()

    /**
     * Should be set with a time as soon as possible after startup.
     */
    public var profilingStarted: Long = 0

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.buildStarted]
     * callback.
     */
    public var buildStarted: Long = 0

    private var settingsEvaluated: Long = 0
    private var projectsLoaded: Long = 0
    private var projectsEvaluated: Long = 0
    private var buildFinished: Long = 0

    public var isSuccessful: Boolean = false

    public val projectConfiguration: CompositeOperation<Operation>
        get() {
            val operations: MutableList<Operation> = ArrayList()
            for (projectProfile in projects.values) {
                operations.add(projectProfile.configurationOperation)
            }
            return CompositeOperation(operations.sortedWith(slowestFirst()))
        }

    /**
     * Get the elapsed time (in mSec) between the start of profiling and the buildStarted event.
     */
    @Deprecated("Inaccurate value because of manual buildStarted invocation")
    public val elapsedStartup: Long
        get() = buildStarted - profilingStarted

    /**
     * Get the total elapsed time (in mSec) between the start of profiling and the buildFinished event.
     */
    public val elapsedTotal: Long
        get() = buildFinished - profilingStarted

    /**
     * Get the elapsed time (in mSec) between the buildStarted event and the settingsEvaluated event.
     * Note that this will include processing of buildSrc as well as the settings file.
     */
    @Deprecated("Can't intercept setting evaluated event")
    public val elapsedSettings: Long
        get() = settingsEvaluated - buildStarted

    /**
     * Get the elapsed time (in mSec) between the settingsEvaluated event and the projectsLoaded event.
     */
    @Deprecated("Can't intercept projects loaded event")
    public val elapsedProjectsLoading: Long
        get() = projectsLoaded - settingsEvaluated

    /**
     * Get the elapsed time (in mSec) between the projectsLoaded event and the projectsEvaluated event.
     */
    @Deprecated("Can't intercept projects loaded event")
    public val elapsedProjectsConfiguration: Long
        get() = projectsEvaluated - projectsLoaded

    /**
     * Get the total task execution time from all projects.
     */
    public val elapsedTotalExecutionTime: Long
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
    public val initWithConfigurationTimeMs: Long
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
    public fun getProjectProfile(projectPath: String): ProjectProfile {
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
    public fun getProjects(): List<ProjectProfile> {
        return projects.values.sortedWith(slowestFirst())
    }

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.settingsEvaluated]
     * callback.
     */
    public fun setSettingsEvaluated(settingsEvaluated: Long) {
        this.settingsEvaluated = settingsEvaluated
    }

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.projectsLoaded]
     * callback.
     */
    public fun setProjectsLoaded(projectsLoaded: Long) {
        this.projectsLoaded = projectsLoaded
    }

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.projectsEvaluated]
     * callback.
     */
    public fun setProjectsEvaluated(projectsEvaluated: Long) {
        this.projectsEvaluated = projectsEvaluated
    }

    /**
     * Should be set with a timestamp from a [org.gradle.BuildListener.buildFinished]
     * callback.
     */
    public fun setBuildFinished(buildFinished: Long) {
        this.buildFinished = buildFinished
    }
}
