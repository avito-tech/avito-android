package com.avito.robolectric.runner

import com.avito.android.log.AndroidTestLoggerMetadataProvider
import com.avito.logger.LogLevel
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggerFactoryBuilder
import com.avito.logger.handler.PrintlnLoggingHandlerProvider
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.inject.Injector

public open class InHouseRobolectricTestRunner
@JvmOverloads constructor(
    testClass: Class<*>,
    injector: Injector = DEFAULT_INJECTOR
) : RobolectricTestRunner(testClass, injector) {

    protected val loggerFactory: LoggerFactory by lazy {
        LoggerFactoryBuilder()
            .metadataProvider(AndroidTestLoggerMetadataProvider(testClass.name))
            .addLoggingHandlerProvider(PrintlnLoggingHandlerProvider(LogLevel.DEBUG, printStackTrace = false))
            .build()
    }

    private companion object {
        private val DEFAULT_INJECTOR = defaultInjector().build()
    }
}
