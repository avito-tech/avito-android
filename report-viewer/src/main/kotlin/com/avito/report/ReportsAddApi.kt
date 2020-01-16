package com.avito.report

import com.avito.report.model.AndroidTest
import com.avito.report.model.ReportCoordinates
import org.funktionale.tries.Try

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
    ): Try<List<String>>

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
    ): Try<String>
}
