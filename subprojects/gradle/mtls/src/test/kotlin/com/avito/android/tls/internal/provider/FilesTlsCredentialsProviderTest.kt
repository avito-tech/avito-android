package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class FilesTlsCredentialsProviderTest {

        @Test
        fun `provider should return credentials from files`(@TempDir tempDir: File) {
            val crtFile = File.createTempFile("client", ".crt", tempDir)
            val keyFile = File.createTempFile("client", ".key", tempDir)

            val provider = FilesTlsCredentialsProvider(
                crtFilePath = crtFile.absolutePath,
                keyFilePath = keyFile.absolutePath,
            )

            val credentials = provider.provideCredentials()

            assertTrue(credentials is TlsCredentials.FileCredentials)
        }

        @Test
        fun `provider should return NotFound if key file is missing`(@TempDir tempDir: File) {
            val crtFile = File.createTempFile("client", ".crt", tempDir)

            val provider = FilesTlsCredentialsProvider(
                crtFilePath = crtFile.absolutePath,
                keyFilePath = ""
            )

            val credentials = provider.provideCredentials()

            assertTrue(credentials is TlsCredentials.NotFound)
            assertEquals(
                "No key file path specified",
                (credentials as TlsCredentials.NotFound).message
            )
        }

        @Test
        fun `provider should return NotFound if crt file is missing`() {
            val keyFile = File.createTempFile("client", ".key")

            val provider = FilesTlsCredentialsProvider(
                crtFilePath = "",
                keyFilePath = keyFile.absolutePath
            )

            val credentials = provider.provideCredentials()

            assertTrue(credentials is TlsCredentials.NotFound)
            assertEquals(
                "No crt file path specified",
                (credentials as TlsCredentials.NotFound).message
            )
        }

    @Test
    fun `provider should return Undefined if both files are missing`() {
        val provider = FilesTlsCredentialsProvider(
            crtFilePath = "",
            keyFilePath = ""
        )

        val credentials = provider.provideCredentials()

        assertTrue(credentials is TlsCredentials.Undefined)
    }
}
