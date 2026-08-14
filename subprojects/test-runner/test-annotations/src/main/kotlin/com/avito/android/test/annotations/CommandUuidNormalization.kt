package com.avito.android.test.annotations

/**
 * TMS stores team uuids in lower case, [CommandUuid] value is normalized to the same form.
 *
 * Kotlin trim also removes a non breaking space, but not a zero width space,
 * so a uuid pasted with the latter stays invalid and is rejected by CommandUuidFormatCheck.
 */
public fun normalizeCommandUuid(value: String?): String? =
    value?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
