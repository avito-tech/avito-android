package com.avito.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ProcessKtTest {

    @Test
    fun `command split - works with spaces in argument`() {
        val s = "git commit --author='test <>' --all --message='xxx xxx'"
        assertThat(RealProcessRunner(null).splitCommand(s))
            .asList()
            .containsExactly(
                "git",
                "commit",
                "--author=test <>",
                "--all",
                "--message=xxx xxx"
            )
    }
}
