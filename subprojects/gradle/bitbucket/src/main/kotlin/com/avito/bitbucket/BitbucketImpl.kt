package com.avito.bitbucket

import com.avito.android.Result
import com.avito.http.BasicAuthenticator
import com.avito.http.HttpClientProvider
import com.avito.impact.changes.newChangesDetector
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

internal class BitbucketImpl(
    private val config: BitbucketConfig,
    private val pullRequestId: Int?,
    httpClientProvider: HttpClientProvider
) : Bitbucket {

    /**
     * A report cannot have more than 1000 annotations by default,
     * however this property is configurable at an instance level.
     */
    private val insightAnnotationsServerLimitPerReport = 1000

    private val retrofit = Retrofit.Builder()
        .baseUrl(config.baseUrl.toHttpUrl())
        .client(
            httpClientProvider
                .provide().authenticator(
                    BasicAuthenticator(
                        user = config.credentials.user,
                        password = config.credentials.password
                    )
                ).build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val bitbucketApi: BitbucketApi = retrofit.create(BitbucketApi::class.java)
    private val insightsApi: InsightsApi = retrofit.create(InsightsApi::class.java)

    override fun addComment(comment: String) {
        val pullRequestId = this.pullRequestId ?: return
        bitbucketApi.addComment(config.projectKey, config.repositorySlug, pullRequestId, Comment(comment)).execute()
    }

    override fun createCommentWithTask(comment: String, task: String) {
        val pullRequestId = this.pullRequestId ?: return
        val commentResponse =
            bitbucketApi.addComment(
                config.projectKey,
                config.repositorySlug,
                pullRequestId,
                Comment(comment)
            ).execute().body()!!
        bitbucketApi.addTask(
            Task(
                anchor = Anchor(id = commentResponse.id),
                text = task
            )
        ).execute()
    }

    override fun addInsights(
        rootDir: File,
        sourceCommitHash: String,
        targetCommitHash: String,
        key: String,
        title: String,
        link: HttpUrl,
        issues: List<Bitbucket.InsightIssue>
    ): Result<Unit> =
        // todo extract
        newChangesDetector(
            rootDir = rootDir,
            targetCommit = targetCommitHash
        )
            .computeChanges(rootDir)
            .map { changes ->

                val changedFiles = changes.map { it.relativePath }

                val issuesList = issues.map { issue ->
                    AnnotationCreateRequest(
                        // /app это папка в docker, а нужен относительный корня путь
                        path = issue.path.replaceFirst("/app/", ""),
                        line = issue.line,
                        message = issue.message,
                        severity = Severity.MEDIUM
                    )
                }

                // отправляем issue только по диффу , т.к. остальные всеравно не отрисуются
                val filteredIssues = issuesList.filter { changedFiles.contains(it.path) }

                insightsApi.createReport(
                    projectKey = config.projectKey,
                    repositorySlug = config.repositorySlug,
                    commitId = sourceCommitHash,
                    insightKey = key,
                    insightReportCreateRequest = InsightReportCreateRequest(
                        title = title,
                        details = "", // todo lint version?
                        data = emptyList(),
                        link = link.toString()
                    )
                )
                    .execute()
                    .handleError(link = link)

                insightsApi.deleteAll(config.projectKey, config.repositorySlug, sourceCommitHash, key)
                    .execute()
                    .handleError()

                if (filteredIssues.isNotEmpty()) {
                    insightsApi.addAnnotations(
                        config.projectKey,
                        config.repositorySlug,
                        sourceCommitHash,
                        key,
                        AnnotationsBulkCreateRequest(filteredIssues.take(insightAnnotationsServerLimitPerReport))
                    )
                        .execute()
                        .handleError(issueSize = filteredIssues.size)
                }
            }

    override fun addInsightReport(
        sourceCommitHash: String,
        key: String,
        title: String,
        details: String,
        link: HttpUrl,
        data: List<InsightData>
    ) = Result.tryCatch {
        insightsApi.createReport(
            projectKey = config.projectKey,
            repositorySlug = config.repositorySlug,
            commitId = sourceCommitHash,
            insightKey = key,
            insightReportCreateRequest = InsightReportCreateRequest(
                title = title,
                details = details,
                data = data,
                link = link.toString()
            )
        )
            .execute()
            .handleError(link = link)
    }

    // todo generic solution
    private fun <T> retrofit2.Response<T>.handleError(link: HttpUrl? = null, issueSize: Int = 0) {
        if (!isSuccessful) {

            val errorMessage = StringBuilder("Error creating insight")
            errorMessage.appendLine(" ${code()}")
            errorMessage.appendLine(" ${message()}")
            errorMessage.appendLine(" ${errorBody()?.string() ?: "no error body"}")
            if (issueSize > 0) {
                errorMessage.appendLine("number of issues: $issueSize")
            }
            if (link != null) {
                errorMessage.appendLine("link: $link")
            }
            throw BitbucketClientException(errorMessage.toString(), null)
        }
    }
}
