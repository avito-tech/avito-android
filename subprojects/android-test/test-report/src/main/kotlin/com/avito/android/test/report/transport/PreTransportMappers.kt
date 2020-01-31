package com.avito.android.test.report.transport

import com.avito.android.test.report.TestPackageParser
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.android.test.report.model.TestType
import com.avito.report.model.Kind
import com.avito.report.model.Step
import kotlin.reflect.full.memberProperties

internal interface PreTransportMappers {

    fun combineFeatures(testMetadata: TestMetadata): List<String> {
        val result = testMetadata.packageParserResult
        return if (result is TestPackageParser.Result.Success) {
            testMetadata.features + result.features
        } else {
            testMetadata.features
        }
    }

    fun transformStepList(stepList: List<StepResult>): List<Step> {
        return stepList.map { stepResult ->
            Step(
                timestamp = stepResult.timestamp!!,
                number = stepResult.number!!,
                title = stepResult.title!!,
                entryList = stepResult.entryList
            )
        }
    }

    //todo remove reflect call
    fun com.avito.android.test.report.model.DataSet.serialize(): Map<String, String> =
        this::class.memberProperties
            .filter { it.name != "number" }
            .map { it.name to it.getter.call(this).toString() }
            .toMap()
}
