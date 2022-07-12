package com.avito.deeplink_generator.internal.parser

import com.avito.deeplink_generator.model.Deeplink
import java.io.File

/**
 * Parses [Deeplink] models from file with [DeeplinkParser].
 *
 * File format: plain text, each deeplink occupies a new string.
 */
internal object DeeplinkFileParser {

    fun parse(deeplinksFile: File, defaultScheme: String): Set<Deeplink> {
        return deeplinksFile
            .bufferedReader()
            .readLines()
            .map { DeeplinkParser.parse(it, defaultScheme) }
            .toSet()
    }
}
