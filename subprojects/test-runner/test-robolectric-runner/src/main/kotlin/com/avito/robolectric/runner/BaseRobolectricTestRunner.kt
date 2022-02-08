package com.avito.robolectric.runner

import com.avito.logger.LoggerFactory
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.inject.Injector

public abstract class BaseRobolectricTestRunner
@JvmOverloads constructor(
    testClass: Class<*>,
    injector: Injector = DEFAULT_INJECTOR
) : RobolectricTestRunner(testClass, injector) {

    protected abstract val loggerFactory: LoggerFactory

    private companion object {
        private val DEFAULT_INJECTOR = defaultInjector().build()
    }
}
