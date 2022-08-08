package com.avito.android.plugin.build_metrics.runtime

import com.avito.android.Result
import com.avito.android.plugin.build_metrics.internal.runtime.HeapInfo
import com.avito.android.plugin.build_metrics.internal.runtime.command.Jcmd
import com.avito.utils.ProcessRunner
import com.avito.utils.StubProcessRunner
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.google.common.truth.TruthJUnit.assume
import org.junit.jupiter.api.Test

internal class JcmdTest {

    @Test
    fun `heap info - parses G1 output`() {
        assume().that(javaHome.isJdk).isTrue()

        val heapInfo = parseHeapInfo(
            """
            53139:
            garbage-first heap   total 1038336K, used 496391K [0x00000005c0000000, 0x0000000800000000)
              region size 2048K, 99 young (202752K), 0 survivors (0K)
            Metaspace       used 219993K, capacity 246054K, committed 249864K, reserved 1265664K
              class space    used 28526K, capacity 32092K, committed 33308K, reserved 1048576K
        """.trimIndent()
        )

        assertThat(heapInfo.heap.usedKb).isEqualTo(496391)
        assertThat(heapInfo.heap.committedKb).isEqualTo(1038336)

        assertThat(heapInfo.metaspace.usedKb).isEqualTo(219993)
        assertThat(heapInfo.metaspace.committedKb).isEqualTo(249864)
    }

    @Test
    fun `heap info - parses ParallelGC output`() {
        assume().that(javaHome.isJdk).isTrue()

        val heapInfo = parseHeapInfo(
            """
             65649:
             PSYoungGen      total 1567744K, used 803793K [0x0000000740000000, 0x00000007ba200000, 0x0000000800000000)
              eden space 1148928K, 50% used [0x0000000740000000,0x0000000763a74b00,0x0000000786200000)
              from space 418816K, 52% used [0x00000007a0900000,0x00000007adf7f990,0x00000007ba200000)
              to   space 425984K, 0% used [0x0000000786200000,0x0000000786200000,0x00000007a0200000)
             ParOldGen       total 2105344K, used 1338053K [0x00000005c0000000, 0x0000000640800000, 0x0000000740000000)
              object space 2105344K, 63% used [0x00000005c0000000,0x0000000611ab1738,0x0000000640800000)
             Metaspace       used 213138K, capacity 257372K, committed 257744K, reserved 1271808K
              class space    used 29940K, capacity 35694K, committed 35712K, reserved 1048576K
        """.trimIndent()
        )

        assertThat(heapInfo.heap.usedKb).isEqualTo(803793 + 1338053)
        assertThat(heapInfo.heap.committedKb).isEqualTo(1567744 + 2105344)

        assertThat(heapInfo.metaspace.usedKb).isEqualTo(213138)
        assertThat(heapInfo.metaspace.committedKb).isEqualTo(257744)
    }

    @Test
    fun `heap info - current process`() {
        assume().that(javaHome.isJdk).isTrue()

        val currentVmId = ProcessHandle.current().pid()

        val heapInfoResult = jcmd.gcHeapInfo(pid = currentVmId)

        assertWithMessage(heapInfoResult.toString())
            .that(heapInfoResult.isSuccess()).isTrue()
    }

    private fun parseHeapInfo(output: String): HeapInfo {
        val processRunner = StubProcessRunner()
        processRunner.result = Result.Success(output)

        val result = Jcmd(processRunner, javaHome).gcHeapInfo(pid = 0)
        assertWithMessage(result.toString()).that(result.isSuccess()).isTrue()

        return result.getOrThrow()
    }
}

internal val jcmd = Jcmd(
    processRunner = ProcessRunner.create(null),
    javaHome
)
