package com.avito.i18n.plugin.service

import com.avito.i18n.plugin.dto.OldTranslationRequest
import com.avito.i18n.plugin.dto.OldTranslationResponse
import com.avito.i18n.plugin.dto.TranslationRequest
import com.avito.i18n.plugin.dto.TranslationResponse

internal interface LocalizationApi {
    fun translateWithDeprecatedApi(
        request: OldTranslationRequest,
    ): OldTranslationResponse

    fun translateWithNewApi(
        request: TranslationRequest
    ): TranslationResponse
}
