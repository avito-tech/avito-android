package com.avito.android.async_tc_cleaner.internal.reaper

import com.avito.logger.PrintlnLoggerFactory
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class ReaperToggleTest {

    private val logger = PrintlnLoggerFactory.create("reaper-toggle")

    @Test
    fun `falls back to the default when no file is configured`() {
        assertTrue(ReaperToggle(toggleFile = null, default = true, logger = logger).isEnabled())
        assertFalse(ReaperToggle(toggleFile = null, default = false, logger = logger).isEnabled())
    }

    @Test
    fun `falls back to the default when the file is absent`(@TempDir tmp: Path) {
        val missing = tmp.resolve("enabled")
        assertTrue(ReaperToggle(missing, default = true, logger = logger).isEnabled())
        assertFalse(ReaperToggle(missing, default = false, logger = logger).isEnabled())
    }

    @Test
    fun `a present file overrides the default in both directions`(@TempDir tmp: Path) {
        val file = Files.writeString(tmp.resolve("enabled"), "true")
        assertTrue(ReaperToggle(file, default = false, logger = logger).isEnabled(), "file enables over default=false")

        Files.writeString(file, "false")
        assertFalse(ReaperToggle(file, default = true, logger = logger).isEnabled(), "file disables over default=true")
    }

    @Test
    fun `trims whitespace and ignores case`(@TempDir tmp: Path) {
        val file = Files.writeString(tmp.resolve("enabled"), "  TRUE\n")
        assertTrue(ReaperToggle(file, default = false, logger = logger).isEnabled())
    }

    @Test
    fun `re-reads the file on each call`(@TempDir tmp: Path) {
        val file = Files.writeString(tmp.resolve("enabled"), "true")
        val gate = ReaperToggle(file, default = false, logger = logger)
        assertTrue(gate.isEnabled())

        Files.writeString(file, "false")
        assertFalse(gate.isEnabled(), "the toggle is re-read live, not cached")
    }
}
