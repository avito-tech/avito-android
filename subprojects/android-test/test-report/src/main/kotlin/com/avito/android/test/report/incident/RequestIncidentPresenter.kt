package com.avito.android.test.report.incident

import com.avito.android.Result
import com.avito.report.model.IncidentElement

internal class RequestIncidentPresenter : IncidentPresenter {

    override fun canCustomize(exception: Throwable): Boolean =
        exception is RequestIncidentException ||
            exception.message?.contains(DELIMITER) ?: false

    private data class Data(val title: String, val body: String)

    override fun customize(exception: Throwable): Result<List<IncidentElement>> {
        return try {
            val data = if (exception is RequestIncidentException) {
                Data(exception.message, exception.body)
            } else {
                exception.message!!.split(DELIMITER).let { Data(it[0], it[1]) }
            }

            Result.Success(
                listOf(
                    IncidentElement(
                        message = data.title,
                        data = data.body,
                        origin = "request"
                    )
                )
            )
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}

class RequestIncidentException(
    override val message: String,
    val body: String,
    override val cause: Throwable?
) : Exception(message, cause)

const val DELIMITER = "|requestBody="
