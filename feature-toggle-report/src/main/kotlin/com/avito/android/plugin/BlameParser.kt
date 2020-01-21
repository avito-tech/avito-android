package com.avito.android.plugin

import java.time.Instant
import java.time.ZoneId

internal class BlameParser {

    private val allEmptySymbols = "\\s+".toRegex()
    private val trashSymbols = "[()<>]".toRegex()

    fun parseBlameCodeLines(blame: String): List<CodeElement> {
        return blame.splitToSequence(newLine)
            .mapIndexed { index, line ->
                val lineNumber = index + 1
                val lineSplit = line.split("$lineNumber)")
                if (lineSplit.size != blameLineNumberSplitSize) {
                    throw RuntimeException("Something wrong with blame: $line")
                }

                val codeLine = lineSplit.last()
                val otherStuff = lineSplit.first()

                val split = otherStuff.split(allEmptySymbols)

                val email = split[emailPart].replace(regex = trashSymbols, replacement = "")

                CodeElement(
                    codeLine = codeLine,
                    changeTime = split[timePart].toLocalDate(),
                    email = email
                )
            }
            .toList()
    }
}

private fun String.toLocalDate() =
    Instant.ofEpochSecond(this.toLong()).atZone(ZoneId.of("Europe/Moscow")).toLocalDate()

private const val newLine = "\n"
private const val emailPart = 2
private const val timePart = 3
private const val blameLineNumberSplitSize = 2
