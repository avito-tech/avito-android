package com.avito.i18n.plugin.internal.feedback

/**
 * Outcome of `updateTranslations` in a single module.
 *
 * @param errorText null when the module has been translated
 */
internal data class TranslationOutcome(
    val module: String,
    val locales: Set<String>,
    val errorText: String? = null,
)
