package com.avito.bitbucket

import com.avito.android.Result
import com.avito.http.HttpClientProvider
import okhttp3.HttpUrl
import java.io.File

public interface Bitbucket {

    public data class InsightIssue(
        val message: String,
        val path: String,
        val line: Int,
        val severity: Severity
    )

    public fun addComment(comment: String)

    public fun createCommentWithTask(comment: String, task: String)

    public fun addInsights(
        rootDir: File,
        sourceCommitHash: String,
        targetCommitHash: String,
        key: String,
        title: String,
        link: HttpUrl,
        issues: List<InsightIssue>
    ): Result<Unit>

    /**
     * Вариант без issues
     */
    public fun addInsightReport(
        sourceCommitHash: String,
        key: String,
        title: String,
        details: String,
        link: HttpUrl,
        data: List<InsightData>
    ): Result<Unit>

    public companion object {

        public fun create(
            bitbucketConfig: BitbucketConfig,
            pullRequestId: Int?,
            httpClientProvider: HttpClientProvider
        ): Bitbucket = BitbucketImpl(
            config = bitbucketConfig,
            pullRequestId = pullRequestId,
            httpClientProvider = httpClientProvider
        )
    }
}
