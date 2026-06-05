package com.avito.android.string_transform.internal.task.common

import java.util.zip.ZipEntry

internal class CompressionManifest(
    private val entries: Map<String, Int>,
) {

    fun methodFor(relPath: String): Int? = entries[relPath]

    fun remapKeys(mapping: Map<String, String>): CompressionManifest {
        return CompressionManifest(entries.mapKeys { (key, _) -> mapping[key] ?: key })
    }

    // Correct only while entries deleted between rename and repack stay DEFLATED
    // (MetadataCleaner removes signatures and dependencies.pb). A STORED deletion would warn falsely.
    fun storedEntriesMissing(finalRelPaths: Set<String>): List<String> {
        return entries
            .filter { (_, method) -> method == ZipEntry.STORED }
            .keys
            .filter { it !in finalRelPaths }
            .sorted()
    }

    companion object {
        val EMPTY = CompressionManifest(emptyMap())
    }
}
