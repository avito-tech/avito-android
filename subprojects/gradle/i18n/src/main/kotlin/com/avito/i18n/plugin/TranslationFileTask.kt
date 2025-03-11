package com.avito.i18n.plugin

import com.avito.i18n.plugin.dto.TranslationRequest
import com.avito.i18n.plugin.service.LocalizationService
import com.avito.i18n.plugin.xml.BaseElement
import com.avito.i18n.plugin.xml.PluralsElement
import com.avito.i18n.plugin.xml.StringElement
import com.avito.i18n.plugin.xml.StringsXmlFile
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.xml.sax.InputSource
import java.io.File
import java.io.FileReader
import java.util.Locale
import javax.xml.transform.stream.StreamResult

// The task is not cached. The output depends on the response from the backend.
internal abstract class TranslationFileTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val defaultStringsFile: RegularFileProperty

    @get:Input
    abstract val locales: SetProperty<String>

    @get:Input
    abstract val namespace: Property<String>

    @get:Input
    abstract val sourceLocale: Property<String>

    @get:Input
    abstract val componentName: Property<String>

    @get:Internal
    abstract val service: Property<LocalizationService>

    @TaskAction
    fun doTranslateFile() {
        val file = defaultStringsFile.get().asFile
        val resPath = file.parentFile.parentFile
        val source = try {
            StringsXmlFile(InputSource(FileReader(file)))
        } catch (e: Exception) {
            throw GradleException("Error parse file: ${file.name}", e)
        }
        for (l in locales.get()) {
            val locale = Locale.forLanguageTag(l)
            val targetFile = File(resPath, locale.getStringsFile())
            val target = try {
                if (targetFile.exists()) {
                    StringsXmlFile(InputSource(FileReader(targetFile)))
                } else {
                    StringsXmlFile()
                }
            } catch (e: Exception) {
                throw GradleException("Error parse file: ${file.name}", e)
            }
            val diff = source.diff(target)

            if (diff.isNotEmpty()) {
                val response = service.get().translate(createTranslationRequest(diff, l))
                val error = response.result.error
                if (error != null) {
                    throw GradleException("Response error: $error")
                }
                val data = response.result.data?.targetTextUnits?.get(l)
                    ?: throw GradleException("Language code '$l' not found in translated strings")

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
    ): TranslationRequest {
        val units = mutableListOf<Map<String, String>>()
        for ((key, value) in diff) {
            val fields = mutableMapOf("key" to key)
            when (value) {
                is PluralsElement -> fields += value.items

                is StringElement -> fields["other"] = value.value
            }
            units += fields
        }
        return TranslationRequest(
            namespace = namespace.get(),
            componentName = componentName.get(),
            sourceLang = sourceLocale.get(),
            targetLangs = listOf(locale),
            textUnits = units
        )
    }
}
