package com.avito.i18n.plugin.internal

import com.avito.i18n.plugin.dto.OldTranslationRequest
import com.avito.i18n.plugin.getStringsFile
import com.avito.i18n.plugin.service.LocalizationService
import com.avito.i18n.plugin.xml.BaseElement
import com.avito.i18n.plugin.xml.PluralsElement
import com.avito.i18n.plugin.xml.StringElement
import com.avito.i18n.plugin.xml.StringsXmlFile
import org.gradle.api.GradleException
import org.xml.sax.InputSource
import java.io.File
import java.io.FileReader
import java.util.Locale
import javax.xml.transform.stream.StreamResult

internal class OldStringsFileTranslator(
    private val defaultStringsFile: File,
    private val locales: Set<String>,
    private val namespace: String,
    private val sourceLocale: String,
    private val componentName: String,
    private val service: LocalizationService
) {

    fun translate() {
        val resPath = defaultStringsFile.parentFile.parentFile
        val source = try {
            StringsXmlFile(InputSource(FileReader(defaultStringsFile)))
        } catch (e: Exception) {
            throw GradleException("Error parse file: ${defaultStringsFile.name}", e)
        }

        for (localeCode in locales) {
            val locale = Locale.forLanguageTag(localeCode)
            val targetFile = File(resPath, locale.getStringsFile())
            val target = try {
                if (targetFile.exists()) {
                    StringsXmlFile(InputSource(FileReader(targetFile)))
                } else {
                    StringsXmlFile()
                }
            } catch (e: Exception) {
                throw GradleException("Error parse file: ${defaultStringsFile.name}", e)
            }
            val diff = source.diff(target)

            if (diff.isNotEmpty()) {
                val response = service.translateOld(createTranslationRequest(diff, localeCode))
                val error = response.result.error
                if (error != null) {
                    throw GradleException("Response error: $error")
                }
                val data = response.result.data?.targetTextUnits?.get(localeCode)
                    ?: throw GradleException("Language code '$localeCode' not found in translated strings")

                val textUnits = data.textUnits.associateBy { it["key"] }

                updateTargetFile(target, source, textUnits, targetFile)
            } else {
                updateTargetFile(target, source, mapOf(), targetFile)
            }
        }
    }

    private fun updateTargetFile(
        target: StringsXmlFile,
        source: StringsXmlFile,
        textUnits: Map<String?, Map<String, String>>,
        targetFile: File
    ) {
        val targetElements = target.elements.associateBy { it.name!! }
        val newTarget = StringsXmlFile()

        for (e in source.elements) {
            val name = e.name
            if (!e.isTranslatable || name == null) {
                continue
            }
            val fields = textUnits[name]
            if (fields == null) {
                val element = targetElements[name]
                when (e) {
                    is StringElement -> newTarget.appendString(name, (element as? StringElement)?.value ?: "", e.hash)
                    is PluralsElement -> newTarget.appendPlurals(
                        name,
                        (element as? PluralsElement)?.items ?: emptyMap(),
                        e.hash
                    )
                }
            } else {
                when (e) {
                    is StringElement -> newTarget.appendString(name, fields["other"] ?: "", e.hash)
                    is PluralsElement -> newTarget.appendPlurals(name, fields.filterKeys { it != "key" }, e.hash)
                }
            }
        }

        if (targetFile.exists()) {
            targetFile.delete()
        } else {
            targetFile.parentFile.mkdirs()
        }
        newTarget.write(StreamResult(targetFile))
    }

    private fun createTranslationRequest(
        diff: Map<String, BaseElement>,
        locale: String
    ): OldTranslationRequest {
        val units = mutableListOf<Map<String, String>>()
        for ((key, value) in diff) {
            val fields = mutableMapOf("key" to key)
            when (value) {
                is PluralsElement -> fields += value.items

                is StringElement -> fields["other"] = value.value
            }
            units += fields
        }
        return OldTranslationRequest(
            namespace = namespace,
            componentName = componentName,
            sourceLang = sourceLocale,
            targetLangs = listOf(locale),
            textUnits = units
        )
    }
}
