package com.avito.i18n.plugin.service

import com.avito.i18n.plugin.dto.TranslationRequest
import com.avito.i18n.plugin.dto.TranslationResponse

internal interface LocalizationApi {
    fun translateWithNewApi(
        request: TranslationRequest
    ): Result<TranslationResponse>
}
