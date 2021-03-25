package com.avito.bitbucket

import com.avito.http.internal.RequestMetadata
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Tag

internal interface BitbucketApi {

    /**
     * Response {
     *   protocol=http/1.1,
     *   code=201,
     *   message=,
     *   url=http://stash/rest/api/1.0/projects/X/repos/repository/pull-requests/12524/comments
     * }
     */
    @POST("/rest/api/1.0/projects/{projectKey}/repos/{repositorySlug}/pull-requests/{pullRequestId}/comments")
    fun addComment(
        @Path("projectKey") projectKey: String,
        @Path("repositorySlug") repositorySlug: String,
        @Path("pullRequestId") pullRequestId: Int,
        @Body comment: Comment,
        @Tag metadata: RequestMetadata = RequestMetadata(serviceName, "add-comment")
    ): Call<CommentResponse>

    @POST("/rest/api/1.0/tasks")
    fun addTask(
        @Body task: Task,
        @Tag metadata: RequestMetadata = RequestMetadata(serviceName, "add-task")
    ): Call<JsonObject>
}

internal data class CommentResponse(
    val id: String
)

internal data class Anchor(
    val id: String,
    val type: String = "COMMENT"
)

internal data class Task(
    val anchor: Anchor,
    val text: String
)

internal data class Comment(val text: String)

private const val serviceName = "bitbucket"
