package com.avito.i18n.plugin.internal

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.i18n.plugin.getStringsFile
import com.avito.i18n.plugin.xml.StringsXmlFile
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

    fun translate(): Result<Unit> {
        val resPath = defaultStringsFile.parentFile.parentFile
        val sourceStringXmlFile = runCatching {
            StringsXmlFile(InputSource(FileReader(defaultStringsFile)))
        }.getOrElse { e ->
            return Result.failure(
                Problem.Builder(
                    shortDescription = "Failed to parse source strings file",
                    context = "Parsing '${defaultStringsFile.name}' at ${defaultStringsFile.path}"
                )
                    .because(e.message ?: "Unknown error")
                    .throwable(e)
                    .build()
                    .asRuntimeException()
            )
        }

        for (languageTag in locales) {
            val locale = Locale.forLanguageTag(languageTag)
            val targetFile = File(resPath, locale.getStringsFile())
            val targetStringXmlFile = runCatching {
                if (targetFile.exists()) {
                    StringsXmlFile(InputSource(FileReader(targetFile)))
                } else {
                    StringsXmlFile()
                }
            }.getOrElse { e ->
                return Result.failure(
                    Problem.Builder(
                        shortDescription = "Failed to parse target strings file",
                        context = "Parsing '${targetFile.name}' for locale '$languageTag' at ${targetFile.path}"
                    )
                        .because(e.message ?: "Unknown error")
                        .throwable(e)
                        .build()
                        .asRuntimeException()
                )
            }

            val translatedTextUnits = apiInteractor.getTranslatedTextUnitsForLocale(
                sourceStringsXmlFile = sourceStringXmlFile,
                targetStringsXmlFile = targetStringXmlFile,
                languageTag = languageTag
            ).getOrElse { e ->
                return Result.failure(e)
            }

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
        return Result.success(Unit)
    }
}
