package com.avito.i18n.plugin.service

import com.avito.i18n.plugin.xml.StringsFile

internal interface LocalizationApi {
    fun translate(
        file: StringsFile,
        locales: Set<String>
    ): List<StringsFile>

    fun createComponent(
        name: String,
        file: String,
        locales: Set<String>
    )
}
