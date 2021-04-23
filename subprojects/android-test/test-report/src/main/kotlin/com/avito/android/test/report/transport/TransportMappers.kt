package com.avito.android.test.report.transport

import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.util.isLambda
import com.avito.filestorage.RemoteStorageRequest
import com.avito.report.model.Entry
import com.avito.report.model.Step
import java.lang.reflect.Field

internal interface TransportMappers {

    fun RemoteStorageRequest.toFileType(): Entry.File.Type {
        // false positive 'must be exhaustive' error in IDE,
        // should be fixed in kotlin 1.5 https://youtrack.jetbrains.com/issue/KT-44821
        return when (this) {
            is RemoteStorageRequest.ContentRequest.Html -> Entry.File.Type.html
            is RemoteStorageRequest.ContentRequest.PlainText -> Entry.File.Type.plain_text
            is RemoteStorageRequest.FileRequest.Image -> Entry.File.Type.img_png
            is RemoteStorageRequest.FileRequest.Video -> Entry.File.Type.video
        }
    }

    fun transformStepList(stepList: List<StepResult>): List<Step> {
        return stepList.map { stepResult ->
            Step(
                timestamp = stepResult.timestamp,
                number = stepResult.number,
                title = stepResult.title,
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
