package com.avito.android.runner.delegates

import android.os.Bundle
import android.util.Log
import com.avito.android.runner.InstrumentationDelegate
import com.avito.android.runner.InstrumentationDelegateProvider
import com.avito.android.test.report.Report
import com.avito.android.test.report.incident.AppCrashException
/**
 * Мы перехватываем все падения приложения тут с помощью глобального хэндлера.
 * Мы используем этот механизм вместе с onException.
 *
 * Если происходит падение внутри приложения в другом треде (например в IO), то срабатывает
 * глобальный обработчик ошибок и крашит приложение внутри Android Runtime. Это падение
 * instrumentation не перехватывает.
 *
 * Сейчас за обработку всех падений приложения в mainThread и внутри instrumentation колбеков
 * отвечает onException. Все остальное (например, падение в отдельном треде) мы перехватываем в
 * глобальном обработчике.
 */
class ApplicationCrashHandler(private val report: Report) : InstrumentationDelegate() {

    override fun beforeOnCreate(arguments: Bundle) {
        val handler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Application crash captured by global handler", throwable)
            tryToReportUnexpectedError(throwable)
            handler?.uncaughtException(thread, throwable)
        }
    }

    override fun onException(obj: Any?, e: Throwable): Boolean {
        Log.e(TAG, "Application crash captured by onException handler inside instrumentation", e)
        tryToReportUnexpectedError(e)
        return super.onException(obj, e)
    }

    private fun tryToReportUnexpectedError(e: Throwable) {
        report.registerIncident(AppCrashException(e))
        report.reportTestCase()
    }

    class Provider : InstrumentationDelegateProvider {

        override fun get(context: InstrumentationDelegateProvider.Context): InstrumentationDelegate {
            return ApplicationCrashHandler(context.report)
        }
    }

    companion object {
        private const val TAG = "ReportUncaughtHandler"
    }
}