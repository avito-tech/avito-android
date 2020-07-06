package com.avito.retrace

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files

internal class ProguardRetracerTest {

    @Test
    fun retrace(@TempDir mappingDir: File) {
        val mappingFile = writeMapping(mappingDir)
        val result = ProguardRetracer.Impl(listOf(mappingFile)).retrace(STACKTRACE)

        assertThat(result).doesNotContain("b.a.a.a.a.a(:5)")
        assertThat(result).contains("com.avito.android.ui.Crasher.crash")
    }

    private fun writeMapping(dir: File): File {
        val path = Files.createTempFile(dir.toPath(), "mapping", null)
        Files.write(path, MAPPING_CONTENT.toByteArray())
        return path.toFile()
    }
}

private val MAPPING_CONTENT = """
# compiler: R8
# compiler_version: 1.6.82
# min_api: 21
# pg_map_id: 1cdc98b
# common_typos_disable
com.avito.android.ui.Crasher -> b.a.a.a.a:
    3:3:void <init>() -> <init>
    5:5:void crash() -> a
"""

private val STACKTRACE = """
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: Application crash captured by global handler
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: java.lang.RuntimeException: Ooops
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at b.a.a.a.a.a(:5)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at com.avito.android.ui.EditTextActivity.onStart(:21)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.Instrumentation.callActivityOnStart(Instrumentation.java:1391)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at androidx.test.runner.MonitoringInstrumentation.callActivityOnStart(MonitoringInstrumentation.java:714)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.Activity.performStart(Activity.java:7157)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.ActivityThread.handleStartActivity(ActivityThread.java:2937)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.servertransaction.TransactionExecutor.performLifecycleSequence(TransactionExecutor.java:180)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.servertransaction.TransactionExecutor.cycleToPath(TransactionExecutor.java:165)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:142)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:70)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.ActivityThread${'$'}H.handleMessage(ActivityThread.java:1808)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.os.Handler.dispatchMessage(Handler.java:106)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.os.Looper.loop(Looper.java:193)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at android.app.ActivityThread.main(ActivityThread.java:6669)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at java.lang.reflect.Method.invoke(Native Method)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at com.android.internal.os.RuntimeInit${'$'}MethodAndArgsCaller.run(RuntimeInit.java:493)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:858)
07-06 13:15:56.784  4604  4604 E ReportUncaughtHandler: Error during reporting test after global exception handling
"""