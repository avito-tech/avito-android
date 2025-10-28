package com.avito.deeplink_generator.internal.parser

import com.avito.deeplink_generator.model.Deeplink
import java.io.File

/**
 * Parses [Deeplink] models from file with [DeeplinkParser]. Skips deeplink scheme information.
 *
 * File format: plain text, each deeplink occupies a new string.
 *
 * Example: 1/some/deeplink com.scheme1,com.scheme2
 */
internal object DeeplinkFileParser {

    fun parse(deeplinksFile: File, defaultScheme: String): Set<Deeplink> {
        return deeplinksFile
            .bufferedReader()
            .readLines()
            .map { DeeplinkParser.parse(it.substringBefore(INFO_DELIMITER), defaultScheme) }
            .toSet()
    }
}
