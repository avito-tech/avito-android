package com.avito.teamcity

import com.avito.kotlin.dsl.getMandatoryStringProperty
import org.gradle.api.Project
import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.BuildId
import org.jetbrains.teamcity.rest.TeamCityInstance
import org.jetbrains.teamcity.rest.TeamCityInstanceFactory

interface TeamcityApi {

    sealed class BranchSpec {
        object DefaultBranch : BranchSpec()
        object AllBranches : BranchSpec()
        class SpecificBranch(val branchName: String) : BranchSpec()
    }

    sealed class ListResult {
        data class OK(val builds: List<SimpleBuild>) : ListResult()
        object NoBuildsFound : ListResult()
        data class Failure(val exception: Exception) : ListResult()
    }

    /**
     * @param finishDate epoch seconds
     */
    data class SimpleBuild(val buildNumber: String, val commitHash: String, val finishDate: Long)

    fun getLastBuilds(
        buildType: String,
        commit: String?,
        branchSpec: BranchSpec,
        limit: Int,
        onlySuccess: Boolean
    ): Sequence<Build>

    fun getLastSimpleBuilds(
        buildType: String,
        commit: String?,
        branchSpec: BranchSpec,
        limit: Int,
        onlySuccess: Boolean
    ): ListResult

    fun getLastBuildsId(
        buildType: String,
        commit: String?,
        branchSpec: BranchSpec,
        limit: Int = 20,
        onlySuccess: Boolean = false
    ): Sequence<String>

    fun getBuild(buildId: String): Build

    @Deprecated("Use getBuild instead")
    fun getBuildNumber(buildId: String): String?

    fun getPreviousBuildOnCommit(
        buildType: String,
        commit: String,
        branchSpec: BranchSpec = BranchSpec.AllBranches
    ): Build?

    fun triggerBuild(
        buildType: String,
        comment: String,
        branchName: String,
        parameters: Map<String, String> = emptyMap()
    ): Build

    class Impl private constructor(private val teamCityInstance: TeamCityInstance) : TeamcityApi {

        constructor(url: String, user: String, password: String) : this(
            TeamCityInstanceFactory.httpAuth(
                url,
                user,
                password
            ).withLogResponses()
        )

        constructor(project: Project) : this(
            url = project.getMandatoryStringProperty("teamcityUrl"),
            user = project.getMandatoryStringProperty("teamcityApiUser"),
            password = project.getMandatoryStringProperty("teamcityApiPassword")
        )

        constructor(credentials: TeamcityCredentials) : this(credentials.url, credentials.user, credentials.password)

        override fun getLastSimpleBuilds(
            buildType: String,
            commit: String?,
            branchSpec: BranchSpec,
            limit: Int,
            onlySuccess: Boolean
        ): ListResult {
            return try {
                val result = getLastBuilds(buildType, commit, branchSpec, limit, onlySuccess)

                val mapped = result.map { build ->
                    val buildNumber = build.buildNumber ?: error("buildNumber unavailable for $build")
                    if (build.revisions.size != 1) {
                        error("Can't decide what vcs root needed, add support for multiple vcs roots if needed")
                    }
                    val revision = build.revisions.first()

                    SimpleBuild(
                        buildNumber,
                        revision.version,
                        build.finishDateTime?.toEpochSecond() ?: error("build not finished? $build")
                    )
                }.toList()

                if (mapped.isEmpty()) {
                    ListResult.NoBuildsFound
                } else {
                    ListResult.OK(mapped)
                }
            } catch (e: Exception) {
                ListResult.Failure(e)
            }
        }

        override fun getLastBuilds(
            buildType: String,
            commit: String?,
            branchSpec: BranchSpec,
            limit: Int,
            onlySuccess: Boolean
        ): Sequence<Build> {
            val buildLocator = teamCityInstance.builds()
                .fromConfiguration(BuildConfigurationId(buildType))
                .apply { commit?.let { withVcsRevision(commit) } }
                .limitResults(limit)

            when (branchSpec) {
                BranchSpec.DefaultBranch -> {
                    //do nothing
                }
                BranchSpec.AllBranches -> buildLocator.withAllBranches()
                is BranchSpec.SpecificBranch -> buildLocator.withBranch(branchSpec.branchName)
            }

            if (!onlySuccess) {
                buildLocator.includeFailed()
            }
            return buildLocator.all()
        }

        override fun getLastBuildsId(
            buildType: String,
            commit: String?,
            branchSpec: BranchSpec,
            limit: Int,
            onlySuccess: Boolean
        ): Sequence<String> =
            getLastBuilds(buildType, commit, branchSpec, limit, onlySuccess).map { build -> build.id.stringId }

        override fun getBuild(buildId: String): Build = teamCityInstance.build(BuildId(buildId))

        override fun getBuildNumber(buildId: String): String? = teamCityInstance.build(BuildId(buildId)).buildNumber

        override fun getPreviousBuildOnCommit(
            buildType: String,
            commit: String,
            branchSpec: BranchSpec
        ): Build? = getLastBuilds(
            buildType = buildType,
            commit = commit,
            branchSpec = branchSpec,
            limit = 1,
            onlySuccess = false
        ).lastOrNull()

        override fun triggerBuild(
            buildType: String,
            comment: String,
            branchName: String,
            parameters: Map<String, String>
        ): Build {
            return teamCityInstance.buildConfiguration(BuildConfigurationId(buildType))
                .runBuild(parameters = parameters, comment = comment, logicalBranchName = branchName)
        }
    }
}
