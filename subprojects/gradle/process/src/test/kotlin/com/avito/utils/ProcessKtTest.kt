package com.avito.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ProcessKtTest {

    @Test
    fun `command split - works with spaces in argument`() {
        val s = "git commit --author='test <>' --all --message='xxx xxx'"
        assertThat(ProcessRunner.Real(null).splitCommand(s))
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
