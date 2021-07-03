package com.avito.bitbucket

import com.avito.http.internal.RequestMetadata
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Tag

/**
 * https://docs.atlassian.com/bitbucket-server/rest/latest/bitbucket-code-insights-rest.html
 */
@Suppress("MaxLineLength")
internal interface InsightsApi {

    @PUT("/rest/insights/1.0/projects/{projectKey}/repos/{repositorySlug}/commits/{commitId}/reports/{insightKey}")
    fun createReport(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("commitId") commitId: String,
        @Path("insightKey") insightKey: String,
        @Body insightReportCreateRequest: InsightReportCreateRequest,
        @Tag metadata: RequestMetadata = RequestMetadata("bitbucket", "create-insights-report")
    ): Call<InsightReportCreateResponse>

    @POST("/rest/insights/1.0/projects/{projectKey}/repos/{repositorySlug}/commits/{commitId}/reports/{insightKey}/annotations")
    fun addAnnotation(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("commitId") commitId: String,
        @Path("insightKey") insightKey: String,
        @Body annotationCreateRequest: AnnotationCreateRequest,
        @Tag metadata: RequestMetadata = RequestMetadata(SERVICE_NAME, "add-insight")
    ): Call<Unit>

    @DELETE("/rest/insights/1.0/projects/{projectKey}/repos/{repositorySlug}/commits/{commitId}/reports/{insightKey}/annotations")
    fun deleteAll(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("commitId") commitId: String,
        @Path("insightKey") insightKey: String,
        @Tag metadata: RequestMetadata = RequestMetadata(SERVICE_NAME, "delete-all-insights")

    ): Call<Unit>

    @POST("/rest/insights/1.0/projects/{projectKey}/repos/{repositorySlug}/commits/{commitId}/reports/{insightKey}/annotations")
    fun addAnnotations(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("commitId") commitId: String,
        @Path("insightKey") insightKey: String,
        @Body annotationsBulkCreateRequest: AnnotationsBulkCreateRequest,
        @Tag metadata: RequestMetadata = RequestMetadata(SERVICE_NAME, "add-insights")
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

public enum class Severity { LOW, MEDIUM, HIGH }

private const val SERVICE_NAME = "bitbucket"
