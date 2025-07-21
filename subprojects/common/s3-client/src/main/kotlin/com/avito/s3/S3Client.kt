package com.avito.s3

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.net.url.Url
import com.avito.android.Result
import com.avito.s3.listener.OperationsListener
import java.io.File
import java.net.URL
import kotlin.time.toKotlinDuration
import aws.sdk.kotlin.services.s3.S3Client as AWS_S3Client

public interface S3Client {
    /**
     * @param key - relative [objekt] path in [s3Bucket]
     */
    public suspend fun putObject(
        key: String,
        objekt: File,
    ): Result<URL>

    public companion object {
        public fun create(config: S3ClientConfig, operationsListener: OperationsListener? = null): S3Client {
            val awsS3Client = AWS_S3Client {
                region = config.region
                endpointUrl = Url.parse(config.endpointUrl.toString())
                credentialsProvider = StaticCredentialsProvider(
                    credentials = Credentials(
                        accessKeyId = config.accessKeyId,
                        secretAccessKey = config.secretAccessKey
                    )
                )
                httpClient {
                    maxConcurrency = config.httpClientConfig.maxConcurrency
                    connectionIdleTimeout = config.httpClientConfig.connectionIdleTimeout.toKotlinDuration()
                }
            }
            return S3ClientImpl(
                endpointUrl = config.endpointUrl,
                awsS3client = awsS3Client,
                operationsListener = operationsListener,
            )
        }
    }
}
