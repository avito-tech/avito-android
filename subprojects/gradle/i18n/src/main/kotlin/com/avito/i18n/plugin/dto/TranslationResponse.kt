package com.avito.i18n.plugin.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TranslationResponse(
    @SerialName("result") val result: TranslationResponseResult
)

@Serializable
internal data class TranslationResponseResult(
    @SerialName("status") val status: TranslationResponseStatus,
    @SerialName("data") val data: TranslationResponseData?,
)

@Serializable
internal data class TranslationResponseStatus(
    @SerialName("code") val statusCode: String,
    @SerialName("message") val message: String,
)

@Serializable
internal data class TranslationResponseData(
    @SerialName("task") val task: TranslationTask,
    @SerialName("textUnits") val translatedTextsWithStatus: List<TranslatedTextWithTranslationStatus>
)

@Serializable
internal data class TranslationTask(
    @SerialName("taskUUID") val taskId: String,
    @SerialName("namespace") val namespace: String,
    @SerialName("contentType") val contentType: String,
    @SerialName("region") val region: String?,
    @SerialName("status") val status: String,
    @SerialName("description") val description: String,
)

@Serializable
internal data class TranslatedTextWithTranslationStatus(
    @SerialName("status") val status: Int,
    @SerialName("unit") val unit: TranslatedTextUnit,
    @SerialName("absentLanguages") val absentLanguages: List<String>?,
)

@Serializable
internal data class TranslatedTextUnit(
    @SerialName("key") val key: String,
    @SerialName("component") val component: String,
    @SerialName("namespace") val namespace: String,
    @SerialName("status") val status: Int,
    @SerialName("translated") val translatedTexts: List<TranslatedTextWithLang>?,
    @SerialName("error") val error: TextUnitError?
)

@Serializable
internal data class TranslatedTextWithLang(
    @SerialName("lang") val lang: String,
    @SerialName("text") val translationText: TranslationText,
)

@Serializable
internal data class TextUnitError(
    @SerialName("message") val message: String
)
