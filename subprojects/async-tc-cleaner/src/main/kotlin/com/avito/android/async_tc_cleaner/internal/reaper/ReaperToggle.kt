package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.logger.Logger
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

internal class ReaperToggle(
    private val toggleFile: Path?,
    private val default: Boolean,
    private val logger: Logger,
) {

    fun isEnabled(): Boolean {
        val file = toggleFile ?: return default
        return try {
            if (Files.exists(file)) {
                Files.readString(file).trim().toBoolean()
            } else {
                default
            }
        } catch (e: IOException) {
            logger.warn("Failed to read sweep toggle $file; using default=$default", e)
            default
        }
    }
}
