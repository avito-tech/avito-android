package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RawContentTlsCredentialsProviderTest {

    @Test
    fun `provider should return PlainCredentials if both crt and key content are provided`() {
        val crtContent = "crt"
        val keyContent = "key"

        val provider = RawContentTlsCredentialsProvider(
            crtContent = crtContent,
            keyContent = keyContent
        )

        val credentials = provider.provideCredentials()

        assertTrue(credentials is TlsCredentials.PlainCredentials)
        assertEquals(crtContent, credentials.crt)
        assertEquals(keyContent, credentials.key)
    }

    @Test
    fun `provider should return Undefined if both crt and key content are missing`() {
        val provider = RawContentTlsCredentialsProvider(
            crtContent = "",
            keyContent = ""
        )

        val credentials = provider.provideCredentials()

        assertTrue(credentials is TlsCredentials.Undefined)
    }

    @Test
    fun `provider should return NotFound if crt content is missing`() {
        val keyContent = "key"

        val provider = RawContentTlsCredentialsProvider(
            crtContent = "",
            keyContent = keyContent
        )

        val credentials = provider.provideCredentials()

        assertTrue(credentials is TlsCredentials.NotFound)
        assertEquals(
            "No crt content specified",
            (credentials as TlsCredentials.NotFound).message
        )
    }

    @Test
    fun `provider should return NotFound if key content is missing`() {
        val crtContent = "crt"

        val provider = RawContentTlsCredentialsProvider(
            crtContent = crtContent,
            keyContent = ""
        )

        val credentials = provider.provideCredentials()

        assertTrue(credentials is TlsCredentials.NotFound)
        assertEquals(
            "No key content specified",
            (credentials as TlsCredentials.NotFound).message
        )
    }
}
