package com.avito.bitbucket

import com.avito.logger.LoggerFactory
import okhttp3.HttpUrl
import org.funktionale.tries.Try
import java.io.File

interface Bitbucket {

    data class InsightIssue(
        val message: String,
        val path: String,
        val line: Int,
        val severity: Severity
    )

    fun addComment(comment: String)

    fun createCommentWithTask(comment: String, task: String)

    fun addInsights(
        rootDir: File,
        sourceCommitHash: String,
        targetCommitHash: String,
        key: String,
        title: String,
        link: HttpUrl,
        issues: List<InsightIssue>
    ): Try<Unit>

    /**
     * Вариант без issues
     */
    fun addInsightReport(
        sourceCommitHash: String,
        key: String,
        title: String,
        details: String,
        link: HttpUrl,
        data: List<InsightData>
    ): Try<Unit>

    companion object {

        fun create(
            bitbucketConfig: BitbucketConfig,
            pullRequestId: Int?,
            loggerFactory: LoggerFactory
        ): Bitbucket = BitbucketImpl(
            config = bitbucketConfig,
            pullRequestId = pullRequestId,
            loggerFactory = loggerFactory
        )
    }
}
