import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

internal interface ProsectorApi {

    @Multipart
    @POST("/tmct")
    fun analyze(
        @Part("task_meta") meta: BranchCompareMeta,
        @Part apkSource: MultipartBody.Part,
        @Part apkTarget: MultipartBody.Part
    ): Call<ProsectorResponse>

    @Multipart
    @POST("/tmct")
    fun releaseAnalysis(
        @Part("task_meta") meta: ReleaseAnalysisMeta,
        @Part apk: MultipartBody.Part
    ): Call<ProsectorResponse>
}
