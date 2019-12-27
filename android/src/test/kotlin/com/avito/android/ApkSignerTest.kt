package com.avito.android

import com.avito.utils.ExistingDirectory
import com.avito.utils.ExistingFile
import com.avito.utils.FakeProcessRunner
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class ApkSignerTest {

    private val irrelevant = ExistingFile.Stub

    @Test
    fun `parseApkSignature - returns SHA-1 from apksigner output`() {
        val apkSignerOutput = """
            Signer #1 certificate DN: CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=RU
            Signer #1 certificate SHA-256 digest: 5ad4ea6ec6e8a9c8d5b8769a1bda93e7a19dd0748380d18a3759b9713f2301e1
            Signer #1 certificate SHA-1 digest: 6aef8d82c935cb0ae597ac31418df7f4f28505d6
            Signer #1 certificate MD5 digest: 1622ce002d84e5430ab59486f725c4b1
            WARNING: META-INF/android.arch.core_runtime.version not protected by signature. Unauthorized modifications to this JAR entry will not be detected. Delete or move the entry outside of META-INF/.
            WARNING: META-INF/android.arch.lifecycle_extensions.version not protected by signature. Unauthorized modifications to this JAR entry will not be detected. Delete or move the entry outside of META-INF/.
        """.trimIndent()

        val expected = "6aef8d82c935cb0ae597ac31418df7f4f28505d6"

        val processRunner = FakeProcessRunner()

        val apkSigner = ApkSigner.Impl(ExistingDirectory.Stub, processRunner)

        processRunner.result = Try.Success(apkSignerOutput)

        val actual = apkSigner.getApkSha1(irrelevant)

        assertThat(actual).isEqualTo(Try.Success(expected))
    }

    @Test
    fun `parseApkSignature - returns null - apksigner is incorrect`() {
        val apkSignerOutput = """
            There is no valid output
        """.trimIndent()

        val processRunner = FakeProcessRunner()

        val apkSigner = ApkSigner.Impl(ExistingDirectory.Stub, processRunner)

        processRunner.result = Try.Success(apkSignerOutput)

        val actual = apkSigner.getApkSha1(irrelevant)

        assertThat(actual).isInstanceOf(Try.Failure::class.java)
    }
}
