package com.avito.android.string_transform.internal.task.mapping

import com.avito.android.Result
import com.avito.android.string_transform.internal.rules.NormalizedRule
import java.io.File
import java.io.RandomAccessFile
import java.nio.charset.StandardCharsets

internal class MappingTextTransformer {

    fun transform(
        inputMapping: File,
        outputMapping: File,
        rules: List<NormalizedRule>,
    ): Result<Unit> = Result.tryCatch {
        val hadTrailingNewline = inputMapping.endsWithNewline()
        inputMapping.useLines(StandardCharsets.UTF_8) { lines ->
            outputMapping.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                val iter = lines.iterator()
                if (iter.hasNext()) writer.write(applyRules(iter.next(), rules))
                while (iter.hasNext()) {
                    writer.write("\n")
                    writer.write(applyRules(iter.next(), rules))
                }
                if (hadTrailingNewline) writer.write("\n")
            }
        }
    }

    internal fun applyRules(
        text: String,
        rules: List<NormalizedRule>,
    ): String {
        return rules.fold(text) { current, rule ->
            current.replace(rule.from, rule.to)
        }
    }

    private fun File.endsWithNewline(): Boolean {
        if (length() == 0L) return false
        return RandomAccessFile(this, "r").use { raf ->
            raf.seek(raf.length() - 1)
            raf.readByte() == '\n'.code.toByte()
        }
    }
}
