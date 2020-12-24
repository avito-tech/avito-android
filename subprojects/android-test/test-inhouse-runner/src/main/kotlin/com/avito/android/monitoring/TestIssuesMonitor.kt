package com.avito.android.monitoring

import okhttp3.Response

interface TestIssuesMonitor {

    fun onFailure(throwable: Throwable)

    /**
     * Ошибки не приводящие к падению теста, но вызывающие retry, которые увеливиают время прохождения
     */
    fun onWarning(throwable: Throwable)

    fun onWarning(response: Response)
}
