package org.apache.tools.ant.types

import java.util.StringTokenizer

/**
 * Partial copy of
 * https://github.com/apache/ant/blob/master/src/main/org/apache/tools/ant/types/Commandline.java
 * converted to Kotlin (due to javadoc issues)
 *
 * Reason: [ProcessBuilder] expects command as an arguments list, arguments could be separated by space
 */
internal fun translateCommandline(toProcess: String?): Array<String> {
    if (toProcess.isNullOrEmpty()) {
        // no command? no string
        return emptyArray()
    }

    // parse with a simple finite state machine
    val normal = 0
    val inQuote = 1
    val inDoubleQuote = 2
    var state = normal
    val tok = StringTokenizer(toProcess, "\"' ", true)
    val result = ArrayList<String>()
    val current = StringBuilder()
    var lastTokenHasBeenQuoted = false

    while (tok.hasMoreTokens()) {
        val nextTok = tok.nextToken()
        when (state) {
            inQuote -> if ("'" == nextTok) {
                lastTokenHasBeenQuoted = true
                state = normal
            } else {
                current.append(nextTok)
            }

            inDoubleQuote -> if ("\"" == nextTok) {
                lastTokenHasBeenQuoted = true
                state = normal
            } else {
                current.append(nextTok)
            }

            else -> {
                if ("'" == nextTok) {
                    state = inQuote
                } else if ("\"" == nextTok) {
                    state = inDoubleQuote
                } else if (" " == nextTok) {
                    if (lastTokenHasBeenQuoted || current.isNotEmpty()) {
                        result.add(current.toString())
                        current.setLength(0)
                    }
                } else {
                    current.append(nextTok)
                }
                lastTokenHasBeenQuoted = false
            }
        }
    }
    if (lastTokenHasBeenQuoted || current.isNotEmpty()) {
        result.add(current.toString())
    }
    require(!(state == inQuote || state == inDoubleQuote)) { "unbalanced quotes in $toProcess" }
    return result.toTypedArray<String>()
}
