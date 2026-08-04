package com.avito.android

import com.avito.utils.StubProcessRunner
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AndroidSdkTest {

    @Test
    fun `platform - directory without a minor version - api below 36`(@TempDir androidHome: File) {
        assertThat(sdk(androidHome).platform(35))
            .isEqualTo(File(androidHome, "platforms/android-35"))
    }

    @Test
    fun `platform - directory without a minor version - api 36`(@TempDir androidHome: File) {
        assertThat(sdk(androidHome).platform(36))
            .isEqualTo(File(androidHome, "platforms/android-36"))
    }

    @Test
    fun `platform - directory with an explicit minor version - api above 36`(@TempDir androidHome: File) {
        assertThat(sdk(androidHome).platform(37))
            .isEqualTo(File(androidHome, "platforms/android-37.0"))
    }

    @Test
    fun `platform - directory with a requested minor version - api 36`(@TempDir androidHome: File) {
        assertThat(sdk(androidHome).platform(36, compileSdkMinor = 1))
            .isEqualTo(File(androidHome, "platforms/android-36.1"))
    }

    @Test
    fun `platform - directory with a requested minor version - api above 36`(@TempDir androidHome: File) {
        assertThat(sdk(androidHome).platform(37, compileSdkMinor = 2))
            .isEqualTo(File(androidHome, "platforms/android-37.2"))
    }

    private fun sdk(androidHome: File) = BaseAndroidSdk(androidHome, StubProcessRunner())
}
