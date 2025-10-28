package com.avito.deeplink_generator.internal.parser

import com.avito.deeplink_generator.model.Deeplink
import java.io.File

/**
 * Parses [Deeplink] models from file.
 *
 * File format: plain text, each deeplink occupies a new string.
 *
 * Example: 1/some/deeplink com.scheme1,com.scheme2
 */
internal object SchemeAwareDeeplinkFileParser {

    fun parse(deeplinksFile: File): Set<Deeplink> {
        return deeplinksFile
            .bufferedReader()
            .readLines()
            .flatMap { line ->
                val host = line.substringBefore(HOST_DELIMITER)
                val path = line.substringAfter(host).substringBefore(INFO_DELIMITER)
                val schemes = line.substringAfter(INFO_DELIMITER).split(SCHEME_DELIMITER).toSet()
                schemes.map { scheme ->
                    Deeplink(
                        scheme = scheme,
                        host = host,
                        path = path,
                    )
                }
            }
            .toSet()
    }
}

internal const val HOST_DELIMITER = "/"
internal const val INFO_DELIMITER = " "
internal const val SCHEME_DELIMITER = ","
