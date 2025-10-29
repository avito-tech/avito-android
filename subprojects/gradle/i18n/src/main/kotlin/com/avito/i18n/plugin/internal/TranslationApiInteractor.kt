package com.avito.i18n.plugin.internal

import com.avito.i18n.plugin.dto.TranslatedTextUnit
import com.avito.i18n.plugin.dto.TranslationInputText
import com.avito.i18n.plugin.dto.TranslationPluralText
import com.avito.i18n.plugin.dto.TranslationRequest
import com.avito.i18n.plugin.dto.TranslationResponse
import com.avito.i18n.plugin.dto.TranslationText
import com.avito.i18n.plugin.service.LocalizationService
import com.avito.i18n.plugin.xml.BaseElement
import com.avito.i18n.plugin.xml.PluralsElement
import com.avito.i18n.plugin.xml.StringElement
import com.avito.i18n.plugin.xml.StringsXmlFile
import org.gradle.api.GradleException

internal class TranslationApiInteractor(
    private val namespace: String,
    private val sourceLocale: String,
    private val componentName: String,
    private val service: LocalizationService
) {

    fun getTranslatedTextUnitsForLocale(
        sourceStringsXmlFile: StringsXmlFile,
        targetStringsXmlFile: StringsXmlFile,
        languageTag: String
    ): List<TranslatedTextUnit> {
        val request = createRequest(
            sourceStringsXmlFile = sourceStringsXmlFile,
            targetStringsXmlFile = targetStringsXmlFile,
            languageTag = languageTag
        ) ?: return emptyList()

        val response = service.translate(request)

        if (response.result.status.statusCode != STATUS_CODE_OK) {
            throw GradleException(
                "Response error, status = ${response.result.status.statusCode}; " +
                    "message = ${response.result.status.message}"
            )
        }

        val validTextUnits = getFilteredTextUnits(response)

        return validTextUnits
    }

    private fun getFilteredTextUnits(response: TranslationResponse): List<TranslatedTextUnit> {
        val responseTranslatedTextsWithStatus = response.result.data?.translatedTextsWithStatus
            ?: return emptyList()

        val validTextUnits = mutableListOf<TranslatedTextUnit>()
        for (translatedTextWithStatus in responseTranslatedTextsWithStatus) {
            if (translatedTextWithStatus.status == TRANSLATION_CODE_SYNCED) {
                validTextUnits.add(translatedTextWithStatus.unit)
            }
        }

        return validTextUnits
    }

    private fun createRequest(
        sourceStringsXmlFile: StringsXmlFile,
        targetStringsXmlFile: StringsXmlFile,
        languageTag: String,
    ): TranslationRequest? {
        val translationInputTexts: List<TranslationInputText> =
            sourceStringsXmlFile.diff(targetStringsXmlFile)
                .mapNotNull { (key, element) ->
                    val translationText = createTranslationText(element) ?: return@mapNotNull null
                    TranslationInputText(
                        key = key,
                        componentName = componentName,
                        text = translationText,
                        context = null
                    )
                }

        if (translationInputTexts.isEmpty()) return null

        return TranslationRequest(
            namespace = namespace,
            sourceLang = sourceLocale,
            platform = PLATFORM_ANDROID,
            targetLangs = listOf(languageTag),
            textUnits = translationInputTexts
        )
    }

    private fun createTranslationText(element: BaseElement): TranslationText? =
        when (element) {
            is StringElement -> TranslationText.Text(
                text = element.value
            )

            is PluralsElement -> {
                val pluralItems = element.items
                TranslationText.Plural(
                    plural = TranslationPluralText.fromMap(pluralItems)
                )
            }

            else -> null
        }
}

private const val PLATFORM_ANDROID: String = "android"
private const val STATUS_CODE_OK: String = "ok"
private const val TRANSLATION_CODE_SYNCED: Int = 10
