package com.avito.android.http.nupokati.retrofit

import com.avito.android.http.nupokati.retrofit.model.artifact_upload.UploadArtifactResponse
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadAbortRequest
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadAbortResponse
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadCompleteRequest
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadCompleteResponse
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadInitRequest
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.MultipartUploadInitResponse
import com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked.UploadMultiPartArtifactResponse
import com.avito.android.http.nupokati.retrofit.model.save_test_result.SaveTestResultRequest
import com.avito.android.http.nupokati.retrofit.model.save_test_result.SaveTestResultResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

internal interface NupokatiV4Api {

    @POST("saveTestResult/")
    fun saveTestResult(
        @Body request: SaveTestResultRequest,
    ): Call<SaveTestResultResponse>

    @Multipart
    @POST("api/1/upload_artifact")
    fun uploadArtifact(
        @Part("platform") platform: String,
        @Part("project") project: String,
        @Part("version") version: String,
        @Part("storeName") storeName: String? = null,
        @Part("buildNumber") buildNumber: Int,
        @Part artifact: MultipartBody.Part,
    ): Call<UploadArtifactResponse>

    @POST("multipartUploadInit/")
    fun multipartUploadInit(
        @Body request: MultipartUploadInitRequest,
    ): Call<MultipartUploadInitResponse>

    @Multipart
    @POST("api/1/upload_part_artifact")
    fun uploadMultiPartArtifact(
        @Part("platform") platform: String,
        @Part("project") project: String,
        @Part("version") version: String,
        @Part("fileName") fileName: String,
        @Part("buildNumber") buildNumber: Int,
        @Part("uploadId") uploadId: String,
        @Part("partNumber") partNumber: Int,
        @Part("sha256Hex") sha256Hex: String,
        @Part chunkPart: MultipartBody.Part,
    ): Call<UploadMultiPartArtifactResponse>

    @POST("multipartUploadComplete/")
    fun multipartUploadComplete(
        @Body request: MultipartUploadCompleteRequest,
    ): Call<MultipartUploadCompleteResponse>

    @POST("multipartUploadAbort/")
    fun multipartUploadAbort(
        @Body request: MultipartUploadAbortRequest,
    ): Call<MultipartUploadAbortResponse>
}
