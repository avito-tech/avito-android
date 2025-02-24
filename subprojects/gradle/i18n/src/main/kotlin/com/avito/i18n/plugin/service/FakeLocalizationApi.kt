package com.avito.i18n.plugin.service

import com.avito.i18n.plugin.getValuesDir
import com.avito.i18n.plugin.xml.BaseElement
import com.avito.i18n.plugin.xml.PluralsElement
import com.avito.i18n.plugin.xml.StringElement
import com.avito.i18n.plugin.xml.StringsFile
import java.io.File

internal class FakeLocalizationApi : LocalizationApi {
    override fun translate(
        file: StringsFile,
        locales: Set<String>
    ): List<StringsFile> {
        val list = mutableListOf<StringsFile>()

        file.parse()
        val resPath = file.resPath

        for (l in locales) {
            val elements = mutableListOf<BaseElement>()

            for (e in file.elements) {
                if (!e.isTranslatable) {
                    continue
                }

                when (e) {
                    is StringElement -> elements += StringElement.create(e.name!!, e.text.reverseWords())

                    is PluralsElement -> elements += PluralsElement.create(
                        e.name!!,
                        e.values.mapValues { it.value.reverseWords() })
                }
            }

            val lFile = StringsFile(getOutputFile(resPath, l), elements)
            lFile.writeFile()
        }

        return list
    }

    override fun createComponent(name: String, file: String, locales: Set<String>) {
        println("Component $name created!")
    }

    private fun getOutputFile(resPath: File, locale: String): File {
        val values = locale.getValuesDir()

        return File(resPath, "$values/strings.xml")
    }

    private fun String.reverseWords(): String {
        val words = split(" ")
        val reversed = mutableListOf<String>()

        for (w in words) {
            reversed += if (REGEX.containsMatchIn(w)) {
                w
            } else {
                w.reversed()
            }
        }

        return reversed.joinToString(" ")
    }

    companion object {
        @Suppress("MaxLineLength")
        val REGEX =
            Regex("(?<!\\x25)\\x25(?:([1-9]\\d*)\\$|\\(([^)]+)\\))?(\\+)?(0|'[^$])?(-)?(\\d+)?(?:\\.(\\d+))?([b-fiosuxX])")
    }
}
