package com.avito.android.test.report.transport

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.util.isLambda
import com.avito.report.model.Step
import java.lang.reflect.Field

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
                field.name to if (field.type.isLambda()) {
                    // don't try to serialize lambdas KT-40666
                    "Unsupported type: lambda value"
                } else {
                    getFieldStringValue(field)
                }
            }
            .toMap()
    }

    private fun Any.getFieldStringValue(field: Field): String {
        return javaClass.methods
            .find { it.name == "get${field.name.capitalize()}" }
            ?.invoke(this)
            .toString()
    }
}
