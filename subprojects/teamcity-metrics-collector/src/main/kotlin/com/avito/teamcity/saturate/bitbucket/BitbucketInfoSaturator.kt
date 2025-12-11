package com.avito.teamcity.saturate.bitbucket

import com.avito.bitbucket.AtlassianCredentials
import com.avito.bitbucket.Bitbucket
import com.avito.bitbucket.BitbucketConfig
import com.avito.bitbucket.PullRequest
import com.avito.teamcity.saturate.BuildInfoSaturator
import com.avito.teamcity.saturate.ExtendedBuildInfo
import okhttp3.OkHttpClient
import org.jetbrains.teamcity.rest.Build

internal class BitbucketInfoSaturator(
    private val bitbucketHost: String,
    private val bitbucketToken: String,
    private val builder: OkHttpClient.Builder = OkHttpClient.Builder()
) : BuildInfoSaturator {

    override fun saturate(build: Build): Build {
        val projectKey = build.parameters.find { it.name == "env.BITBUCKET_PROJECT" } ?: return build
        val repoSlug = build.parameters.find { it.name == "env.BITBUCKET_REPOSITORY" } ?: return build
        val pullRequestId = build.parameters.find { it.name == "reverse.dep.*.env.PULL_REQUEST_ID" } ?: return build
        val bitbucketInstance = Bitbucket.create(
            bitbucketConfig = BitbucketConfig(
                baseUrl = bitbucketHost,
                credentials = AtlassianCredentials.BearerToken(
                    token = bitbucketToken,
                ),
                projectKey = projectKey.value,
                repositorySlug = repoSlug.value,
            ),
            pullRequestId = pullRequestId.value.toIntOrNull(),
            builder = builder
        )

        val pullRequest = bitbucketInstance.getPullRequest()
            .fold(
                onSuccess = { it },
                onFailure = { null }
            )

        return if (pullRequest != null) {
             ExtendedBuildInfo(build, pullRequest)
        } else {
            build
        }
    }
}

internal fun Build.pullRequest(): PullRequest? {
    return when {
        this is ExtendedBuildInfo<*> && data is PullRequest -> data
        this is ExtendedBuildInfo<*> -> build.pullRequest()
        else -> null
    }
}
