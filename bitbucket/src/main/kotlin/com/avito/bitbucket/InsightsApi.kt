package com.avito.bitbucket

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * https://docs.atlassian.com/bitbucket-server/rest/latest/bitbucket-code-insights-rest.html
 */
internal interface InsightsApi {

    @PUT("/rest/insights/1.0/projects/{projectKey}/repos/{repositorySlug}/commits/{commitId}/reports/{insightKey}")
    fun createReport(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("commitId") commitId: String,
        @Path("insightKey") insightKey: String,
        @Body insightReportCreateRequest: InsightReportCreateRequest
    ): Call<InsightReportCreateResponse>

    @POST("/rest/insights/1.0/projects/{projectKey}/repos/{repositorySlug}/commits/{commitId}/reports/{insightKey}/annotations")
    fun addAnnotation(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("commitId") commitId: String,
        @Path("insightKey") insightKey: String,
        @Body annotationCreateRequest: AnnotationCreateRequest
    ): Call<Unit>

    @DELETE("/rest/insights/1.0/projects/{projectKey}/repos/{repositorySlug}/commits/{commitId}/reports/{insightKey}/annotations")
    fun deleteAll(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("commitId") commitId: String,
        @Path("insightKey") insightKey: String
    ): Call<Unit>

    @POST("/rest/insights/1.0/projects/{projectKey}/repos/{repositorySlug}/commits/{commitId}/reports/{insightKey}/annotations")
    fun addAnnotations(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("commitId") commitId: String,
        @Path("insightKey") insightKey: String,
        @Body annotationsBulkCreateRequest: AnnotationsBulkCreateRequest
    ): Call<Unit>
}

internal data class InsightReportCreateRequest(
    val title: String,
    val details: String,
    val data: List<InsightData>,
    val link: String
)

internal data class InsightReportCreateResponse(
    val createdDate: Long,
    val result: String
)

internal data class AnnotationCreateRequest(
    val path: String,
    val line: Int,
    val message: String,
    val severity: Severity
)

internal data class AnnotationsBulkCreateRequest(val annotations: List<AnnotationCreateRequest>)

enum class Severity { LOW, MEDIUM, HIGH }
