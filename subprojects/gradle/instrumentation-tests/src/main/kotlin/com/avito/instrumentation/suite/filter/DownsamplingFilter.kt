package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.report.model.DeviceName
import java.util.Random

class DownsamplingFilter(
    private val factor: Float,
    seed: Long = System.currentTimeMillis()
) : TestRunFilter {

    private val random = Random(seed)

    init {
        require(factor > 0.0 && factor <= 1.0) {
            "downsampling factor must be in (0.0; 1.0]"
        }
    }

    override fun runNeeded(test: TestInApk, deviceName: DeviceName, api: Int): TestRunFilter.Verdict {
        return if (random.nextDouble() < factor) {
            TestRunFilter.Verdict.Run
        } else {
            TestRunFilter.Verdict.Skip.SkippedByDownsampling
        }
    }
}
