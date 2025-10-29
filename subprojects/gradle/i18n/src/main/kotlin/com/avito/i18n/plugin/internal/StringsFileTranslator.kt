package com.avito.i18n.plugin.internal

import com.avito.i18n.plugin.getStringsFile
import com.avito.i18n.plugin.xml.StringsXmlFile
import org.gradle.api.GradleException
import org.xml.sax.InputSource
import java.io.File
import java.io.FileReader
import java.util.Locale
import javax.xml.transform.stream.StreamResult

internal class StringsFileTranslator(
    private val defaultStringsFile: File,
    private val locales: Set<String>,
    private val apiInteractor: TranslationApiInteractor,
) {

    fun translate() {
        val resPath = defaultStringsFile.parentFile.parentFile
        val sourceStringXmlFile = try {
            StringsXmlFile(InputSource(FileReader(defaultStringsFile)))
        } catch (e: Exception) {
            throw GradleException("Error parsing file: ${defaultStringsFile.name}", e)
        }

        for (languageTag in locales) {
            val locale = Locale.forLanguageTag(languageTag)
            val targetFile = File(resPath, locale.getStringsFile())
            val targetStringXmlFile = try {
                if (targetFile.exists()) {
                    StringsXmlFile(InputSource(FileReader(targetFile)))
                } else {
                    StringsXmlFile()
                }
            } catch (e: Exception) {
                throw GradleException("Error parsing file: ${defaultStringsFile.name}", e)
            }

            val translatedTextUnits = apiInteractor.getTranslatedTextUnitsForLocale(
                sourceStringsXmlFile = sourceStringXmlFile,
                targetStringsXmlFile = targetStringXmlFile,
                languageTag = languageTag
            )

            val updatedTargetFile = StringsXmlFileHelper.updateTargetWithTranslations(
                sourceStringsXmlFile = sourceStringXmlFile,
                targetStringsXmlFile = targetStringXmlFile,
                translatedTextUnits = translatedTextUnits,
                languageTag = languageTag
            )

            if (targetFile.exists()) {
                targetFile.delete()
            } else {
                targetFile.parentFile.mkdirs()
            }
            updatedTargetFile.write(StreamResult(targetFile))
        }
    }
}
