package com.avito.android.instant_feedback.internal.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PullRequestMergedEvent(
    @SerialName("actor") val user: User,
    @SerialName("pullRequest") val pr: PullRequest,
)

@Serializable
internal data class User(
    @SerialName("name") val username: String,
    @SerialName("displayName") val fullname: String,
    @SerialName("emailAddress") val email: String,
)

@Serializable
internal data class PullRequest(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    @SerialName("toRef") val ref: RefMetadata
)

@Serializable
internal data class RefMetadata(
    @SerialName("repository") val repository: Repository,
)

@Serializable
internal data class Repository(
    @SerialName("name") val name: String,
)
