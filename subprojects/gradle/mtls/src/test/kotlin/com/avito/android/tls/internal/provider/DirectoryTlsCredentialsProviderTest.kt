package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials
import org.gradle.internal.impldep.org.testng.AssertJUnit.assertEquals
import org.gradle.internal.impldep.org.testng.AssertJUnit.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DirectoryTlsCredentialsProviderTest {

    @Test
    fun `provider should return credentials from directory`(@TempDir tempDir: File) {
        val crtFile = File(tempDir, "client.crt")
        crtFile.createNewFile()

        val keyFile = File(tempDir, "client.key")
        keyFile.createNewFile()

        val provider = DirectoryTlsCredentialsProvider(
            directory = tempDir
        )

        val credentials = provider.provideCredentials()

        assertTrue(credentials is TlsCredentials.FileCredentials)
    }

    @Test
    fun `provider should return Undefined if directory does not exist`(@TempDir tempDir: File) {
        val provider = DirectoryTlsCredentialsProvider(
            directory = File(tempDir, "non-existing-directory")
        )

        val credentials = provider.provideCredentials()
        assertTrue(credentials is TlsCredentials.Undefined)
    }

    @Test
    fun `provider should return NotFound if key file is missing`(@TempDir tempDir: File) {
        val crtFile = File(tempDir, "client.crt")
        crtFile.createNewFile()

        val provider = DirectoryTlsCredentialsProvider(
            directory = tempDir
        )

        val credentials = provider.provideCredentials()

        assertTrue(credentials is TlsCredentials.NotFound)
        assertEquals(
            "No key or crt files found in directory: ${tempDir.absolutePath}",
            (credentials as TlsCredentials.NotFound).message
        )
    }

    @Test
    fun `provider should return NotFound if crt file is missing`(@TempDir tempDir: File) {
        val keyFile = File(tempDir, "client.key")
        keyFile.createNewFile()

        val provider = DirectoryTlsCredentialsProvider(
            directory = tempDir
        )

        val credentials = provider.provideCredentials()

        assertTrue(credentials is TlsCredentials.NotFound)
        assertEquals(
            "No key or crt files found in directory: ${tempDir.absolutePath}",
            (credentials as TlsCredentials.NotFound).message
        )
    }

    @Test
    fun `provider should return NotFound if multiple key and crt files are present`(@TempDir tempDir: File) {
        val crtFile1 = File(tempDir, "client1.crt").apply { createNewFile() }
        val keyFile1 = File(tempDir, "client1.key").apply { createNewFile() }
        val crtFile2 = File(tempDir, "client2.crt").apply { createNewFile() }
        val keyFile2 = File(tempDir, "client2.key").apply { createNewFile() }

        val provider = DirectoryTlsCredentialsProvider(
            directory = tempDir
        )

        assertTrue(provider.provideCredentials() is TlsCredentials.NotFound)
        assertEquals(
            """
                |Multiple key and/or crt files found in directory: ${tempDir.absolutePath}.
                |Files:
                |$TAB- ${crtFile1.path}
                |$TAB- ${keyFile1.path}
                |$TAB- ${crtFile2.path}
                |$TAB- ${keyFile2.path}
            """.trimMargin(marginPrefix = "|"),
            (provider.provideCredentials() as TlsCredentials.NotFound).message
        )
    }

    companion object {
        private const val TAB = "\t"
    }
}
