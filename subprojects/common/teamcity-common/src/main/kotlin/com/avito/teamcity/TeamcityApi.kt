package com.avito.teamcity

import org.jetbrains.teamcity.rest.Build

public interface TeamcityApi {

    public sealed class BranchSpec {
        public object DefaultBranch : BranchSpec()
        public object AllBranches : BranchSpec()
        public class SpecificBranch(public val branchName: String) : BranchSpec()
    }

    public sealed class ListResult {
        public data class OK(val builds: List<SimpleBuild>) : ListResult()
        public object NoBuildsFound : ListResult()
        public data class Failure(val exception: Exception) : ListResult()
    }

    /**
     * @param finishDate epoch seconds
     */
    public data class SimpleBuild(val buildNumber: String, val commitHash: String, val finishDate: Long)

    public fun getLastBuilds(
        buildType: String,
        commit: String?,
        branchSpec: BranchSpec,
        limit: Int,
        onlySuccess: Boolean
    ): Sequence<Build>

    public fun getLastSimpleBuilds(
        buildType: String,
        commit: String?,
        branchSpec: BranchSpec,
        limit: Int,
        onlySuccess: Boolean
    ): ListResult

    public fun getLastBuildsId(
        buildType: String,
        commit: String?,
        branchSpec: BranchSpec,
        limit: Int = 20,
        onlySuccess: Boolean = false
    ): Sequence<String>

    public fun getBuild(buildId: String): Build

    @Deprecated("Use getBuild instead")
    public fun getBuildNumber(buildId: String): String?

    public fun getPreviousBuildOnCommit(
        buildType: String,
        commit: String,
        branchSpec: BranchSpec = BranchSpec.AllBranches
    ): Build?

    public fun triggerBuild(
        buildType: String,
        comment: String,
        branchName: String,
        parameters: Map<String, String> = emptyMap()
    ): Build

    public companion object {

        public fun create(teamcityCredentials: TeamcityCredentials): TeamcityApi = TeamcityApiImpl(teamcityCredentials)
    }
}
