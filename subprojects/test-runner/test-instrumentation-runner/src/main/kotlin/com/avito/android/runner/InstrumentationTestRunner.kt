package com.avito.android.runner

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.avito.logger.LoggerFactory

abstract class InstrumentationTestRunner : AndroidJUnitRunner(), OrchestratorDelegate {

    abstract val loggerFactory: LoggerFactory

    protected lateinit var instrumentationArguments: Bundle

    private var delegateRegistry: DelegatesRegistry? = null

    protected open fun getDelegates(arguments: Bundle): List<InstrumentationTestRunnerDelegate> {
        return emptyList()
    }

    /**
     * WARNING: Shouldn't crash in this method.
     * Otherwise we can't pass an error to the report
     */
    final override fun onCreate(arguments: Bundle) {
        instrumentationArguments = arguments
        val isRealRun = isRealRun(arguments)
        if (isRealRun) {
            beforeOnCreate(arguments)
            delegateRegistry = DelegatesRegistry(
                getDelegates(arguments) + SystemDialogsManagerDelegate(loggerFactory)
            )
            delegateRegistry?.beforeOnCreate(arguments)
        }

        super.onCreate(arguments)
        if (isRealRun) {
            afterOnCreate(arguments)
            delegateRegistry?.afterOnCreate(arguments)
        }
    }

    protected open fun beforeOnCreate(arguments: Bundle) {
        // empty
    }

    protected open fun afterOnCreate(arguments: Bundle) {
        // empty
    }

    override fun onStart() {
        delegateRegistry?.beforeOnStart()
        super.onStart()
    }

    override fun finish(resultCode: Int, results: Bundle?) {
        delegateRegistry?.beforeFinish(resultCode, results)
        super.finish(resultCode, results)
        delegateRegistry?.afterFinish(resultCode, results)
    }
}
