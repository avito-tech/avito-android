package com.avito.android.runner

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.avito.logger.LoggerFactory

abstract class InstrumentationTestRunner : AndroidJUnitRunner() {

    abstract val loggerFactory: LoggerFactory

    private var delegate: InstrumentationDelegate? = null

    protected open fun createFactory(): ContextFactory {
        return object : ContextFactory.Default() {
            override fun createIfRealRun(arguments: Bundle): Context {
                return DefaultTestInstrumentationContext(
                    errorsReporter = LogErrorsReporter()
                )
            }
        }
    }

    override fun onCreate(arguments: Bundle) {
        val context = createFactory().create(arguments)
        if (context != null) {
            delegate = InstrumentationDelegate(context.errorsReporter, loggerFactory)
        }
        super.onCreate(arguments)
    }

    override fun onStart() {
        delegate?.beforeOnStart()
        super.onStart()
    }
}
