package com.avito.instrumentation.internal

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class LocalRunArgsCheckerTest {

    @Test
    fun `args with pem values - dump args - pem values are hidden`(@TempDir dumpDir: File) {
        val checker = LocalRunArgsChecker { dumpDir }

        checker.dumpArgs(
            mapOf(
                "avito.tls.crt" to stubPem("CERTIFICATE"),
                "avito.tls.key" to stubPem("PRIVATE KEY"),
                "deviceName" to "local",
            )
        )

        assertThat(checker.readDump()).containsExactly(
            "avito.tls.crt", "***",
            "avito.tls.key", "***",
            "deviceName", "local",
        )
    }
}
