package com.avito.android.tls.providers

import com.avito.android.tls.credentials.TlsCredentials
import com.avito.android.tls.internal.provider.DirectoryTlsCredentialsProvider
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DirectoryTlsCredentialsProviderTest {

    @Test
    fun `when credentials files exist - return tls credentials instance`(@TempDir tempDir: File) {
        val crtContent = "crtContent"
        val keyContent = "keyContent"
        createFile(tempDir, "personal.crt", crtContent)
        createFile(tempDir, "personal.key", keyContent)

        val defaultTlsCredentialsProvider = DirectoryTlsCredentialsProvider(tempDir)

        val credentials = defaultTlsCredentialsProvider.provideCredentials()
        assertThat(credentials).isNotNull()
        assertThat(credentials.crt).isEqualTo(crtContent)
        assertThat(credentials.key).isEqualTo(keyContent)
    }

    @Test
    fun `when directory contains few credentials files - return tls credentials undefined`(@TempDir tempDir: File) {
        val crtContent = "crtContent"
        val keyContent = "keyContent"
        createFile(tempDir, "personal.crt", crtContent)
        createFile(tempDir, "personal.key", keyContent)
        createFile(tempDir, "personal2.crt", crtContent)
        createFile(tempDir, "personal2.key", keyContent)

        val defaultTlsCredentialsProvider = DirectoryTlsCredentialsProvider(tempDir)

        val credentials = defaultTlsCredentialsProvider.provideCredentials()
        assertThat(credentials).isEqualTo(TlsCredentials.Undefined)
    }

    @Test
    fun `when credentials files do not exist - tls credentials undefined`(@TempDir tempDir: File) {
        val defaultTlsCredentialsProvider = DirectoryTlsCredentialsProvider(tempDir)
        val credentials = defaultTlsCredentialsProvider.provideCredentials()
        assertThat(credentials).isEqualTo(TlsCredentials.Undefined)
    }

    private fun createFile(tempDir: File, name: String, content: String): Boolean {
        val file = File(tempDir, name)
        val result = file.createNewFile()
        if (result) {
            file.writeText(content)
        }
        return result
    }
}
