package com.avito.android.test.report.video

import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_WRITE
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.util.executeMethod
import com.avito.android.util.getFieldValue
import java.io.File
import java.io.IOException
import java.io.InputStream

internal interface ShellCommandExecutor {

    /**
     * Executes a command and returns its output. The stream must be read to the end and closed.
     */
    fun execute(command: String): InputStream

    /**
     * Executes a command asynchronously redirecting its output to [output].
     */
    fun execute(command: String, output: File)
}

internal class UiAutomationShellCommandExecutor : ShellCommandExecutor {

    override fun execute(command: String): InputStream {
        val output = InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(command)

        return ParcelFileDescriptor.AutoCloseInputStream(output)
    }

    /**
     * executeShellCommand is called on UiAutomationConnection directly, because uiAutomation wraps
     * it with logic that redirects the output of a process started inside the UiAutomation service
     * into a pipe. Those pipes live as long as the Instrumentation process, so a process started on
     * behalf of the UiAutomation service (video recording, for example) races with the test run.
     *
     * This method does the same as uiAutomation.executeShellCommand(), but allows passing any file
     * descriptor, not only a pipe, which avoids the problem described above.
     *
     * https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/android/app/UiAutomationConnection.java
     */
    override fun execute(command: String, output: File) {
        // connect
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val connection = automation.getFieldValue<Any>("mUiAutomationConnection")

        val outputDescriptor = ParcelFileDescriptor.open(
            output,
            MODE_READ_WRITE
        )
        val inputDescriptor: ParcelFileDescriptor? = null

        /**
         * Since Android 27 the signature of executeShellCommand in UiAutomationConnection has
         * changed: one more argument appeared, it sets the input for the started process.
         *
         * Before 27:
         * https://chromium.googlesource.com/android_tools/+/e429db7f48cd615b0b408cda259ffbc17d3945bb/sdk/sources/android-23/android/app/UiAutomationConnection.java#230
         *
         * After:
         * https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-8.1.0_r14/core/java/android/app/UiAutomationConnection.java#305
         */
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O_MR1) {
            connection.executeMethod(
                "executeShellCommand",
                command,
                outputDescriptor
            )
        } else {
            connection.executeMethod(
                "executeShellCommand",
                command,
                outputDescriptor,
                inputDescriptor
            )
        }
        try {
            outputDescriptor.close()
        } catch (ignore: IOException) {
            // ignore
        }
    }
}
