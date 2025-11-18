package com.avito.android.http.nupokati.retrofit

import com.avito.android.MiB
import com.avito.android.Result
import com.avito.android.http.nupokati.NupokatiV4Client
import com.avito.android.http.nupokati.model.ArtifactUploadResult
import com.avito.android.http.nupokati.model.TestResultSaveResult
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadAbortRequest
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadAbortResponse
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadCompleteRequest
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadInitRequest
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadPart
import com.avito.android.http.nupokati.retrofit.model.save_test_result.SaveTestResultRequest
import com.avito.android.model.output.toNupokatiV4ReportCoordinates
import com.avito.reportviewer.model.ReportCoordinates
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.name

internal class RetrofitNupokatiV4Client(
    private val api: NupokatiV4Api,
    private val chunkedUploadThreshold: Long,
) : NupokatiV4Client {

    val digest: MessageDigest = MessageDigest.getInstance("SHA-256")

    override fun saveTestResult(
        project: String,
        platform: String,
        version: String,
        buildNumber: Int,
        reportUrl: String,
        reportCoordinates: ReportCoordinates,
    ): Result<TestResultSaveResult> {
        val httpRequest = SaveTestResultRequest(
            project = project,
            platform = platform,
            buildNumber = buildNumber,
            reportUrl = reportUrl,
            reportCoordinates = reportCoordinates.toNupokatiV4ReportCoordinates()
        )

        return api.saveTestResult(request = httpRequest).executeCall().fold(
            onSuccess = { response ->
                if (response.result.success) {
                    Result.Success(
                        TestResultSaveResult(
                            message = response.result.message
                        )
                    )
                } else {
                    Result.Failure(
                        RuntimeException(
                            "Error while saving test result: ${response.result.message}"
                        )
                    )
                }
            },
            onFailure = { Result.Failure(it) }
        )
    }

    override fun uploadArtifact(
        project: String,
        platform: String,
        version: String,
        buildNumber: Int,
        storeName: String?,
        file: Path,
    ): Result<ArtifactUploadResult> {
        val fileSize = file.fileSize()

        return if (fileSize <= chunkedUploadThreshold) {
            uploadArtifactSimple(platform, project, version, buildNumber, storeName, file)
        } else {
            uploadArtifactChunked(platform, project, version, buildNumber, storeName, file)
        }
    }

    private fun uploadArtifactSimple(
        platform: String,
        project: String,
        version: String,
        buildNumber: Int,
        storeName: String?,
        file: Path,
    ): Result<ArtifactUploadResult> {
        val requestBody = file.toFile().asRequestBody()
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val uploadResult = api.uploadArtifact(
            platform = platform,
            project = project,
            version = version,
            buildNumber = buildNumber,
            storeName = storeName,
            artifact = filePart,
        ).executeCall()

        return uploadResult.fold(
            onSuccess = { response ->
                Result.Success(
                    ArtifactUploadResult(
                        artifactUri = response.uri
                    )
                )
            },
            onFailure = { throwable ->
                Result.Failure(throwable)
            },
        )
    }

    private fun uploadArtifactChunked(
        platform: String,
        project: String,
        version: String,
        buildNumber: Int,
        storeName: String?,
        file: Path,
    ): Result<ArtifactUploadResult> {
        val chunkSizeBytes = 10.MiB

        val uploadId = initializeChunkedUpload(
            platform = platform,
            project = project,
            version = version,
            buildNumber = buildNumber,
            fileName = file.name,
            storeName = storeName,
        ).fold(
            onSuccess = { it },
            onFailure = { return Result.Failure(it) }
        )

        val uploadedParts = uploadArtifactChunks(
            file = file,
            chunkSizeBytes = chunkSizeBytes,
            platform = platform,
            project = project,
            version = version,
            buildNumber = buildNumber,
            uploadId = uploadId,
        ).fold(
            onSuccess = { it },
            onFailure = { throwable ->
                abortChunkedUpload(platform, project, version, buildNumber, file.name, uploadId)
                return Result.Failure(throwable)
            }
        )

        return finishChunkedUpload(
            platform = platform,
            project = project,
            version = version,
            buildNumber = buildNumber,
            fileName = file.name,
            uploadId = uploadId,
            uploadedParts = uploadedParts,
        ).fold(
            onSuccess = { artifactUri ->
                Result.Success(
                    ArtifactUploadResult(
                        artifactUri = artifactUri
                    )
                )
            },
            onFailure = {
                abortChunkedUpload(platform, project, version, buildNumber, file.name, uploadId)
                return Result.Failure(it)
            }
        )
    }

    private fun uploadArtifactChunks(
        file: Path,
        chunkSizeBytes: Long,
        platform: String,
        project: String,
        version: String,
        buildNumber: Int,
        uploadId: String,
    ): Result<List<MultipartUploadPart>> = Result.tryCatch {
        val uploadedParts = mutableListOf<MultipartUploadPart>()

        file.inputStream().use { inputStream ->
            val buffer = ByteArray(chunkSizeBytes.toInt())

            var partNumber = 1
            while (true) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead < 0) break

                val chunkData = if (bytesRead < chunkSizeBytes) {
                    buffer.copyOf(bytesRead)
                } else {
                    buffer
                }

                val chunkPart = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    chunkData.toRequestBody()
                )

                api.uploadMultiPartArtifact(
                    platform = platform,
                    project = project,
                    version = version,
                    fileName = file.name,
                    buildNumber = buildNumber,
                    uploadId = uploadId,
                    partNumber = partNumber,
                    sha256Hex = calculateSHA256(chunkData),
                    chunkPart = chunkPart
                ).executeCall().fold(
                    onSuccess = { partResponse ->
                        uploadedParts.add(
                            MultipartUploadPart(
                                partNumber = partResponse.partNumber,
                                etag = partResponse.etag
                            )
                        )
                    },
                    onFailure = { throw it }
                )

                partNumber++
            }
        }

        return Result.Success(uploadedParts)
    }

    private fun calculateSHA256(data: ByteArray): String {
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun initializeChunkedUpload(
        platform: String,
        project: String,
        version: String,
        buildNumber: Int,
        fileName: String,
        storeName: String?,
    ): Result<String> {
        val initRequest = MultipartUploadInitRequest(
            platform = platform,
            project = project,
            version = version,
            buildNumber = buildNumber,
            storeName = storeName,
            fileName = fileName
        )

        val initResponse = api.multipartUploadInit(initRequest).executeCall().fold(
            onSuccess = { Result.Success(it) },
            onFailure = { return Result.Failure(it) }
        )
        if (initResponse.value.result.error != null) {
            return Result.Failure(
                RuntimeException(
                    "Error while initializing multipart upload: ${initResponse.value.result.error}"
                )
            )
        }

        val uploadId = initResponse.value.result.uploadId ?: return Result.Failure(
            RuntimeException("multipartUploadInit returned null uploadId")
        )

        return Result.Success(uploadId)
    }

    private fun finishChunkedUpload(
        platform: String,
        project: String,
        version: String,
        buildNumber: Int,
        fileName: String,
        uploadId: String,
        uploadedParts: List<MultipartUploadPart>,
    ): Result<String> {
        val completeRequest = MultipartUploadCompleteRequest(
            platform = platform,
            project = project,
            version = version,
            buildNumber = buildNumber,
            fileName = fileName,
            uploadId = uploadId,
            parts = uploadedParts
        )

        val response = api.multipartUploadComplete(completeRequest).executeCall().fold(
            onSuccess = { Result.Success(it) },
            onFailure = { return Result.Failure(it) }
        )
        if (!response.value.result.error.isNullOrEmpty()) {
            return Result.Failure(
                RuntimeException(
                    "Error while completing multipart upload: ${response.value.result.error}"
                )
            )
        }
        val artifactUri = response.value.result.uri
            ?: return Result.Failure(RuntimeException("multipartUploadComplete returned null URI"))

        return Result.Success(artifactUri)
    }

    private fun abortChunkedUpload(
        platform: String,
        project: String,
        version: String,
        buildNumber: Int,
        fileName: String,
        uploadId: String,
    ): Result<MultipartUploadAbortResponse> {
        val abortRequest = MultipartUploadAbortRequest(
            platform = platform,
            project = project,
            version = version,
            buildNumber = buildNumber,
            fileName = fileName,
            uploadId = uploadId,
        )
        val response = api.multipartUploadAbort(abortRequest).executeCall()

        if (response is Result.Success && !response.value.result.error.isNullOrEmpty()) {
            return Result.Failure(
                RuntimeException(
                    "Error while aborting multipart upload: ${response.value.result.error}"
                )
            )
        }
        return response
    }

    private inline fun <reified T> Call<T>.executeCall(): Result<T> = Result.tryCatch {
        val response = execute()

        val result: Result<T> = if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: "error body is null"
            Result.Failure(
                RuntimeException(
                    "HTTP Response[" +
                        " code: ${response.code()}," +
                        " message: ${response.message()}," +
                        " body: $errorBody" +
                        "]"
                )
            )
        } else {
            response.body()?.let { body -> return Result.Success(body) }
            // For example, in case of 204 No Content
            @Suppress("UNCHECKED_CAST")
            return when (T::class.java) {
                Unit::class.java -> Result.Success(Unit) as Result<T>
                else -> Result.Failure(
                    RuntimeException(
                        "HTTP Response[" +
                            " code: ${response.code()}," +
                            " message: ${response.message()}," +
                            " body: Response body is null" +
                            "]"
                    )
                )
            }
        }
        return result
    }
}
