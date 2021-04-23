package com.avito.android.test.report.transport

import com.avito.android.Result
import com.avito.android.test.report.model.DataSet
import com.avito.android.test.report.model.StepResult
import com.avito.android.util.isLambda
import com.avito.filestorage.ContentType
import com.avito.filestorage.FutureValue
import com.avito.filestorage.map
import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import com.avito.report.model.Step
import com.avito.time.TimeProvider
import okhttp3.HttpUrl
import java.lang.reflect.Field

internal interface TransportMappers {

    fun Entry.File.Type.toContentType(): ContentType {
        return when (this) {
            Entry.File.Type.html -> ContentType.HTML
            Entry.File.Type.img_png -> ContentType.PNG
            Entry.File.Type.video -> ContentType.MP4
            Entry.File.Type.plain_text -> ContentType.TXT
        }
    }

    fun Entry.File.Type.extension(): String {
        return when (this) {
            Entry.File.Type.html -> "html"
            Entry.File.Type.img_png -> "png"
            Entry.File.Type.video -> "mp4"
            Entry.File.Type.plain_text -> "txt"
        }
    }

    fun FutureValue<Result<HttpUrl>>.toEntry(
        comment: String,
        timeProvider: TimeProvider,
        type: Entry.File.Type
    ): FutureValue<Entry.File> {
        return map { result ->
            val fileAddress = result.fold(
                onSuccess = { FileAddress.URL(it) },
                onFailure = { FileAddress.Error(it) }
            )

            Entry.File(
                comment = comment,
                fileAddress = fileAddress,
                timeInSeconds = timeProvider.nowInSeconds(),
                fileType = type
            )
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
