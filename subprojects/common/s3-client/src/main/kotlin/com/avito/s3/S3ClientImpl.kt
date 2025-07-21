package com.avito.s3

import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import com.avito.android.Result
import com.avito.s3.listener.OperationsListener
import java.io.File
import java.net.URL
import java.time.Duration
import java.time.Instant
import aws.sdk.kotlin.services.s3.S3Client as AWS_S3Client

internal class S3ClientImpl(
    private val endpointUrl: URL,
    private val awsS3client: AWS_S3Client,
    private val operationsListener: OperationsListener? = null,
) : S3Client {
    override suspend fun putObject(
        key: String,
        objekt: File,
    ): Result<URL> = executeS3Operation(OperationType.PUT_OBJECT) {
        if (!objekt.exists()) {
            throw IllegalArgumentException("File does not exist: ${objekt.absolutePath}")
        }

        awsS3client.putObject(
            PutObjectRequest {
                this.key = key
                this.metadata = emptyMap()
                this.body = objekt.asByteStream()
            }
        )
        URL("$endpointUrl/$key")
    }

    private suspend fun <T> executeS3Operation(s3OperationType: OperationType, block: suspend () -> T): Result<T> {
        val operationStartTime = Instant.now()
        val result = Result.tryCatch {
            block.invoke()
        }
        val operationDuration = Duration.between(operationStartTime, Instant.now())
        return result.onSuccess {
            operationsListener?.onOperationSuccess(
                operationName = s3OperationType.operationName,
                duration = operationDuration,
            )
        }.onFailure {
            operationsListener?.onOperationFailure(
                throwable = it,
                operationName = s3OperationType.operationName,
                duration = operationDuration,
            )
        }
    }
}
