package com.avito.runner.service.worker.device.adb.request.shell

import com.avito.runner.service.worker.device.adb.request.AdbRequestSerializer
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class RunTestsAdbShellRequestTest {

    private val serializer = AdbRequestSerializer("stub")

    @Test
    fun `am instrument command - serialized correctly`() {
        val request = RunTestsAdbShellRequest(
            testPackageName = "com.avito.android",
            testRunnerClass = "com.avito.android.InstrumentationRunner",
            instrumentationArguments = mapOf(
                "plainValue" to "value",
                "emptyValue" to "",
                "blankValue" to "  ",
                "jsonValue" to "{\"foo\" : {\"bar\" : 5 } }",
                "spaces in key" to "value"
            ),
            enableDeviceDebug = true,
        )
        val serialized = serializer.serialize(request)
        @Suppress("MaxLineLength")
        assertThat(serialized).isEqualTo("adb -s stub shell am instrument -w -r -e debug true  -e 'plainValue' 'value' -e 'emptyValue' '' -e 'blankValue' '  ' -e 'jsonValue' '{\"foo\" : {\"bar\" : 5 } }' -e 'spaces in key' 'value' com.avito.android/com.avito.android.InstrumentationRunner")
    }
}
