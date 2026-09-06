package com.avito.s3

import aws.sdk.kotlin.services.s3.model.PutObjectResponse
import com.avito.android.Result
import com.avito.s3.listener.OperationsListener
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isA
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.net.URI
import java.net.URL
import java.time.Duration
import aws.sdk.kotlin.services.s3.S3Client as AWS_S3Client

class S3ClientImplTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var testFile: File
    private lateinit var s3Client: S3Client
    private lateinit var config: S3ClientConfig
    private lateinit var mockOperationsListener: OperationsListener

    @BeforeEach
    fun setUp() = runTest {
        testFile = File(tempDir, "test.txt").apply {
            writeText("Test content for S3 upload")
        }

        config = S3ClientConfig(
            region = "us-east-1",
            endpointUrl = URI("https://example.com/test-bucket").toURL(),
            accessKeyId = "test-access-key",
            secretAccessKey = "test-secret-key",
            httpClientConfig = S3ClientConfig.HttpClientConfig(
                maxConcurrency = 10u,
                connectionIdleTimeout = Duration.ofSeconds(30)
            )
        )

        mockOperationsListener = mock<OperationsListener>()
        val mockAwsS3Client = mock<AWS_S3Client>()
        whenever(mockAwsS3Client.putObject(any())).thenReturn(PutObjectResponse {})
        s3Client = S3ClientImpl(config.endpointUrl, mockAwsS3Client, mockOperationsListener)
    }

    @Test
    fun `putObject - returns Success with correct URL when upload succeeds`() = runTest {
        val key = "prefix/test-file.txt"

        val result = s3Client.putObject(key, testFile)
        result.printToConsole()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success

        assertThat(success.value.toString()).isEqualTo("${config.endpointUrl}/$key")
    }

    @Test
    fun `putObject - returns Failure when file does not exist`() = runTest {
        val nonExistentFile = File(tempDir, "non-existent.txt")
        val key = "test-key"

        val result = s3Client.putObject(key, nonExistentFile)
        result.printToConsole()

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        val failure = result as Result.Failure

        assertThat(failure.throwable).isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `putObject - handles empty file correctly`() = runTest {
        val emptyFile = File(tempDir, "empty.txt").apply {
            writeText("")
        }
        val key = "empty-folder/empty-file.txt"

        val result = s3Client.putObject(key, emptyFile)
        result.printToConsole()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success

        assertThat(success.value.toString()).isEqualTo("${config.endpointUrl}/$key")
    }

    @Test
    fun `putObject - handles nested key structure`() = runTest {
        val key = "level1/level2/level3/deep-file.txt"

        val result = s3Client.putObject(key, testFile)
        result.printToConsole()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success

        assertThat(success.value.toString()).isEqualTo("${config.endpointUrl}/$key")
    }

    @Test
    fun `putObject - returns encoded URL when key contains characters illegal in URI`() = runTest {
        // brackets and spaces are legal in S3 keys and come from parameterized test names
        val key = "prefix/TestClass_method[api22] param.txt"

        val result = s3Client.putObject(key, testFile)
        result.printToConsole()

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success

        assertThat(success.value.toString())
            .isEqualTo("${config.endpointUrl}/prefix/TestClass_method%5Bapi22%5D%20param.txt")
    }

    @Test
    fun `putObject - calls OperationsListener onOperationSuccess when upload succeeds`() = runTest {
        val key = "test-key"

        val result = s3Client.putObject(key, testFile)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        verify(mockOperationsListener).onOperationSuccess(
            operationName = eq("put"),
            duration = any()
        )
    }

    @Test
    fun `putObject - calls OperationsListener onOperationFailure when upload fails`() = runTest {
        val nonExistentFile = File(tempDir, "non-existent.txt")
        val key = "test-key"

        val result = s3Client.putObject(key, nonExistentFile)

        assertThat(result).isInstanceOf(Result.Failure::class.java)
        verify(mockOperationsListener).onOperationFailure(
            throwable = isA<IllegalArgumentException>(),
            operationName = eq("put"),
            duration = any()
        )
    }

    @Test
    fun `putObject - works without OperationsListener`() = runTest {
        val mockAwsS3Client = mock<AWS_S3Client>()
        whenever(mockAwsS3Client.putObject(any())).thenReturn(PutObjectResponse {})
        val s3ClientWithoutListener = S3ClientImpl(config.endpointUrl, mockAwsS3Client, null)
        val key = "test-key"

        val result = s3ClientWithoutListener.putObject(key, testFile)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        val success = result as Result.Success
        assertThat(success.value.toString()).isEqualTo("${config.endpointUrl}/$key")
    }

    private fun Result<URL>.printToConsole() {
        when (this) {
            is Result.Success -> println("SUCCESS: ${this.value}")
            is Result.Failure -> {
                println("FAILURE: ${this.throwable.javaClass.simpleName}: ${this.throwable.message}")
                this.throwable.printStackTrace()
            }
        }
    }
}
