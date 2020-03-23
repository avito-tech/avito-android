package com.avito.android.test.report.transport

import com.avito.android.test.report.TestPackageParser
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.test.report.model.TestMetadata
import com.avito.report.model.Step

internal interface PreTransportMappers {

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

    fun DataSet.serialize(): Map<String, String> {
        return javaClass.declaredFields
            .filter { it.name != "number" }
            .map { field ->
                field.name to javaClass.methods
                    .find { it.name == "get${field.name.capitalize()}" }
                    ?.invoke(this)
                    .toString()
            }
            .toMap()
    }
}
