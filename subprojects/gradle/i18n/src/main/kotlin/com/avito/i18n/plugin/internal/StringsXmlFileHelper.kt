package com.avito.i18n.plugin.internal

import com.avito.i18n.plugin.dto.TranslatedTextUnit
import com.avito.i18n.plugin.dto.TranslationText
import com.avito.i18n.plugin.xml.BaseElement
import com.avito.i18n.plugin.xml.PluralsElement
import com.avito.i18n.plugin.xml.StringElement
import com.avito.i18n.plugin.xml.StringsXmlFile

internal object StringsXmlFileHelper {

    fun updateTargetWithTranslations(
        sourceStringsXmlFile: StringsXmlFile,
        targetStringsXmlFile: StringsXmlFile,
        translatedTextUnits: List<TranslatedTextUnit>,
        languageTag: String,
    ): StringsXmlFile {
        val targetNameToElementMap = targetStringsXmlFile.elements.associateBy { it.name!! }
        val nameToTranslatedTextUnitMap = translatedTextUnits.associateBy { it.key }
        val newTargetXmlFile = StringsXmlFile()

        for (elementFromSource in sourceStringsXmlFile.elements) {
            val sourceElementName = elementFromSource.name ?: continue
            val elementHash = elementFromSource.hash

            val translatedText = findTranslationTextForLanguageTag(
                nameToTranslatedTextUnitMap = nameToTranslatedTextUnitMap,
                elementName = sourceElementName,
                languageTag = languageTag
            )

            if (translatedText == null) {
                val elementFromTargetXmlFile = targetNameToElementMap[sourceElementName]
                if (elementFromTargetXmlFile != null) {
                    newTargetXmlFile.appendElement(
                        element = elementFromTargetXmlFile,
                        name = sourceElementName,
                        hash = elementHash
                    )
                }
            } else {
                newTargetXmlFile.appendTranslationText(
                    translationText = translatedText,
                    name = sourceElementName,
                    hash = elementHash,
                    sourceElement = elementFromSource
                )
            }
        }

        return newTargetXmlFile
    }

    private fun findTranslationTextForLanguageTag(
        nameToTranslatedTextUnitMap: Map<String, TranslatedTextUnit>,
        elementName: String,
        languageTag: String,
    ): TranslationText? {
        val translatedTexts = nameToTranslatedTextUnitMap[elementName]?.translatedTexts
            ?: return null

        val translationTextForLanguageCode = translatedTexts.find { it.lang == languageTag }?.translationText
        return translationTextForLanguageCode
    }

    private fun StringsXmlFile.appendElement(element: BaseElement, name: String, hash: String) {
        when (element) {
            is StringElement -> appendString(
                name = name,
                value = element.value,
                hash = hash,
                asMarkup = element.isInlineMarkup
            )
            is PluralsElement -> appendPlurals(
                name = name,
                values = element.items,
                hash = hash,
                markupQuantities = element.inlineMarkupQuantities
            )
        }
    }

    private fun StringsXmlFile.appendTranslationText(
        translationText: TranslationText,
        name: String,
        hash: String,
        sourceElement: BaseElement?,
    ) {
        when (translationText) {
            is TranslationText.Text -> appendString(
                name = name,
                value = translationText.text,
                hash = hash,
                asMarkup = (sourceElement as? StringElement)?.isInlineMarkup ?: false
            )
            is TranslationText.Plural -> appendPlurals(
                name = name,
                values = translationText.plural.toMap(),
                hash = hash,
                markupQuantities = (sourceElement as? PluralsElement)?.inlineMarkupQuantities ?: emptySet()
            )
        }
    }
}
