package com.avito.android

import com.avito.truth.isInstanceOf
import com.avito.utils.ExistingDirectory
import com.avito.utils.StubProcessRunner
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class AaptTest {

    private val irrelevant = File(".")

    @Test
    fun `parseApkPackageName - returns package name from aapt output`() {
        val aaptOutput = """
            package: name='ru.domofond.app.dev' versionCode='248' versionName='debug' platformBuildVersionName='debug'
            sdkVersion:'21'
            targetSdkVersion:'28'
            uses-permission: name='android.permission.INTERNET'
        """.trimIndent()

        val expected = "ru.domofond.app.dev"

        val processRunner = StubProcessRunner()
        val aapt = Aapt.Impl(ExistingDirectory.Stub, processRunner)

        processRunner.result = Result.Success(aaptOutput)

        val actual = aapt.getPackageName(irrelevant)

        assertThat(actual).isEqualTo(Result.Success(expected))
    }

    @Test
    fun `parseApkPackageName - returns null - aapt output is incorrect`() {
        val aaptOutput = """
            There is no valid output
        """.trimIndent()

        val processRunner = StubProcessRunner()
        val aapt = Aapt.Impl(ExistingDirectory.Stub, processRunner)

        processRunner.result = Result.Success(aaptOutput)

        val actual = aapt.getPackageName(irrelevant)

        assertThat(actual).isInstanceOf<Result.Failure<*>>()
    }
}
