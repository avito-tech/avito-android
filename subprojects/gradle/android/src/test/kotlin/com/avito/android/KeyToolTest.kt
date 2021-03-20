package com.avito.android

import com.avito.truth.isInstanceOf
import com.avito.utils.ExistingFile
import com.avito.utils.StubProcessRunner
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class KeyToolTest {

    private val irrelevant = ExistingFile.Stub

    @Suppress("MaxLineLength")
    @Test
    fun `parseBundleSignature - returns lowercased SHA-1 from keytool output`() {
        val keytoolOutput = """
            Signer #1:

            Signature:

            Owner: CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=RU
            Issuer: CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=RU
            Serial number: 611fe924
            Valid from: Tue Nov 03 15:28:26 MSK 2015 until: Sat Mar 21 15:28:26 MSK 2043
            Certificate fingerprints:
                     MD5:  16:22:CE:00:2D:84:E5:43:0A:B5:94:86:F7:25:C4:B1
                     SHA1: 6A:EF:8D:82:C9:35:CB:0A:E5:97:AC:31:41:8D:F7:F4:F2:85:05:D6
                     SHA256: 5A:D4:EA:6E:C6:E8:A9:C8:D5:B8:76:9A:1B:DA:93:E7:A1:9D:D0:74:83:80:D1:8A:37:59:B9:71:3F:23:01:E1
                     Signature algorithm name: SHA256withRSA
                     Version: 3

        """.trimIndent()

        val expected = "6aef8d82c935cb0ae597ac31418df7f4f28505d6"

        val processRunner = StubProcessRunner()
        val keyTool = KeyTool(processRunner)

        processRunner.result = Result.Success(keytoolOutput)

        val actual = keyTool.getJarSha1(irrelevant)

        assertThat(actual).isEqualTo(Result.Success(expected))
    }

    @Test
    fun `parseBundleSignature - returns null - keytool output is incorrect`() {
        val keytoolOutput = """
            There is no valid output
        """.trimIndent()

        val processRunner = StubProcessRunner()
        val keyTool = KeyTool(processRunner)

        processRunner.result = Result.Success(keytoolOutput)

        val actual = keyTool.getJarSha1(irrelevant)

        assertThat(actual).isInstanceOf<Result.Failure<*>>()
    }
}
