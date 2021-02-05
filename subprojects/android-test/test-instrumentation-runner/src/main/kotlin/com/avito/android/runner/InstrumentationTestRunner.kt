package com.avito.android.runner

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.avito.logger.LoggerFactory

abstract class InstrumentationTestRunner : AndroidJUnitRunner(), OrchestratorDelegate {

    abstract val loggerFactory: LoggerFactory

    private var delegate: InstrumentationDelegate? = null

    override fun onCreate(arguments: Bundle) {
        if (isRealRun(arguments)) {
            delegate = InstrumentationDelegate(loggerFactory)
        }
        super.onCreate(arguments)
    }

    override fun onStart() {
        delegate?.beforeOnStart()
        super.onStart()
    }
}
