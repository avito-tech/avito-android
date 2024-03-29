package com.avito.plugin

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

internal interface QAppsUploadApi {

    @Multipart
    @POST("/qapps/api/os/android/upload")
    fun upload(
        @Part comment: MultipartBody.Part,
        @Part branch: MultipartBody.Part,
        @Part version_name: MultipartBody.Part,
        @Part version_code: MultipartBody.Part,
        @Part package_name: MultipartBody.Part,
        /**
         * Влияет только на время хранения артефакта
         */
        @Part release_chain: MultipartBody.Part,
        @Part apk: MultipartBody.Part,
    ): Call<Void>
}
