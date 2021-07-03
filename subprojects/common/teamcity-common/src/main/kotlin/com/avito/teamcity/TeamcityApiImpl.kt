package com.avito.teamcity

import org.jetbrains.teamcity.rest.Build
import org.jetbrains.teamcity.rest.BuildConfigurationId
import org.jetbrains.teamcity.rest.BuildId
import org.jetbrains.teamcity.rest.TeamCityInstance
import org.jetbrains.teamcity.rest.TeamCityInstanceFactory

internal class TeamcityApiImpl private constructor(private val teamCityInstance: TeamCityInstance) : TeamcityApi {

    constructor(credentials: TeamcityCredentials) : this(credentials.url, credentials.user, credentials.password)

    private constructor(url: String, user: String, password: String) : this(
        TeamCityInstanceFactory.httpAuth(
            url,
            user,
            password
        ).withLogResponses()
    )

    override fun getLastSimpleBuilds(
        buildType: String,
        commit: String?,
        branchSpec: TeamcityApi.BranchSpec,
        limit: Int,
        onlySuccess: Boolean
    ): TeamcityApi.ListResult {
        return try {
            val result = getLastBuilds(buildType, commit, branchSpec, limit, onlySuccess)

            val mapped = result.map { build ->
                val buildNumber = build.buildNumber ?: error("buildNumber unavailable for $build")
                if (build.revisions.size != 1) {
                    error("Can't decide what vcs root needed, add support for multiple vcs roots if needed")
                }
                val revision = build.revisions.first()

                TeamcityApi.SimpleBuild(
                    buildNumber,
                    revision.version,
                    build.finishDateTime?.toEpochSecond() ?: error("build not finished? $build")
                )
            }.toList()

            if (mapped.isEmpty()) {
                TeamcityApi.ListResult.NoBuildsFound
            } else {
                TeamcityApi.ListResult.OK(mapped)
            }
        } catch (e: Exception) {
            TeamcityApi.ListResult.Failure(e)
        }
    }

    override fun getLastBuilds(
        buildType: String,
        commit: String?,
        branchSpec: TeamcityApi.BranchSpec,
        limit: Int,
        onlySuccess: Boolean
    ): Sequence<Build> {
        val buildLocator = teamCityInstance.builds()
            .fromConfiguration(BuildConfigurationId(buildType))
            .apply { commit?.let { withVcsRevision(commit) } }
            .limitResults(limit)

        when (branchSpec) {
            TeamcityApi.BranchSpec.DefaultBranch -> {
                // do nothing
            }
            TeamcityApi.BranchSpec.AllBranches -> buildLocator.withAllBranches()
            is TeamcityApi.BranchSpec.SpecificBranch -> buildLocator.withBranch(branchSpec.branchName)
        }

        if (!onlySuccess) {
            buildLocator.includeFailed()
        }
        return buildLocator.all()
    }

    override fun getLastBuildsId(
        buildType: String,
        commit: String?,
        branchSpec: TeamcityApi.BranchSpec,
        limit: Int,
        onlySuccess: Boolean
    ): Sequence<String> =
        getLastBuilds(buildType, commit, branchSpec, limit, onlySuccess).map { build -> build.id.stringId }

    override fun getBuild(buildId: String): Build = teamCityInstance.build(BuildId(buildId))

    override fun getBuildNumber(buildId: String): String? = teamCityInstance.build(BuildId(buildId)).buildNumber

    override fun getPreviousBuildOnCommit(
        buildType: String,
        commit: String,
        branchSpec: TeamcityApi.BranchSpec
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
