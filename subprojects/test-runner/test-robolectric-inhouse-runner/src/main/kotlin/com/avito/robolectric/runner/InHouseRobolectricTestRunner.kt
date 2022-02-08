package com.avito.robolectric.runner

import com.avito.android.log.AndroidTestLoggerMetadataProvider
import com.avito.logger.LogLevel
import com.avito.logger.LoggerFactory
import com.avito.logger.LoggerFactoryBuilder
import com.avito.logger.handler.PrintlnLoggingHandlerProvider

public open class InHouseRobolectricTestRunner(
    private val testClass: Class<*>,
) : BaseRobolectricTestRunner(testClass) {

    override val loggerFactory: LoggerFactory by lazy {
        LoggerFactoryBuilder()
            .metadataProvider(AndroidTestLoggerMetadataProvider(testClass.name))
            .addLoggingHandlerProvider(PrintlnLoggingHandlerProvider(LogLevel.DEBUG, printStackTrace = false))
            .build()
    }
}
