package com.avito.reportviewer

import com.avito.logger.PrintlnLoggerFactory
import com.avito.test.http.MockDispatcher
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

internal class StubReportsExtension : BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private var state: State? = null

    override fun beforeEach(context: ExtensionContext) {
        val mockDispatcher = MockDispatcher()
        val mockWebServer = MockWebServer().apply { dispatcher = mockDispatcher }
        state = State(
            mockWebServer = mockWebServer,
            stubReportApi = StubReportApi(
                realApi = ReportsApiFactory.create(
                    host = mockWebServer.url("/").toString(),
                    builder = OkHttpClient.Builder(),
                    loggerFactory = PrintlnLoggerFactory,
                ),
                mockDispatcher = mockDispatcher
            )
        )
    }

    override fun afterEach(context: ExtensionContext) {
        state?.release()
    }

    @Suppress("NewApi") // test fixtures (not running in android runtime)
    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return parameterContext.parameter.type == StubReportApi::class.java
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any? {
        return state?.stubReportApi
    }

    companion object {
        @Suppress("unused")
        private val NAMESPACE = ExtensionContext.Namespace.create(StubReportsExtension::class.java)
    }

    private class State(
        val mockWebServer: MockWebServer,
        val stubReportApi: StubReportApi
    ) {
        fun release() {
            mockWebServer.shutdown()
        }
    }
}
