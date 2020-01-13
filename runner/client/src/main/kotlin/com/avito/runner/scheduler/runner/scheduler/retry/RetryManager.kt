package com.avito.runner.scheduler.runner.scheduler.retry

import com.avito.runner.service.model.DeviceTestCaseRun

interface RetryManager {

    /**
     * @return сколько еще раз осталось запустить тест
     */
    fun retryCount(history: List<DeviceTestCaseRun>): Int
}
