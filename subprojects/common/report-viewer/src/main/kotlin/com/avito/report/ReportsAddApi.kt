package com.avito.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates

interface ReportsAddApi {

    /**
     * RunTest.AddFull
     *
     * addFull - особый метод, он умеет под капотом создавать отчет (метод create) если он еще не создан
     *
     * @return set id
     */
    fun addTests(
        reportCoordinates: ReportCoordinates,
        buildId: String?,
        tests: Collection<AndroidTest>
    ): Result<List<String>>

    /**
     * RunTest.AddFull
     *
     * Применяется для отправки тестов по-одному с девайса
     *
     * @return set id
     */
    fun addTest(
        reportCoordinates: ReportCoordinates,
        buildId: String?,
        test: AndroidTest
    ): Result<String>
}
