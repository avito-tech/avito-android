import com.avito.utils.logging.ciLogger
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

@CacheableTask
abstract class ProsectorReleaseAnalysisTask : DefaultTask() {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    lateinit var apk: File

    @Input
    lateinit var meta: ReleaseAnalysisMeta

    @Input
    lateinit var host: String

    @Internal
    var debug: Boolean = false

    private val apiClient by lazy {
        Retrofit.Builder()
            .baseUrl(host)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .client(
                OkHttpClient.Builder().apply {
                    if (debug) {
                        addInterceptor(HttpLoggingInterceptor { message ->
                            project.ciLogger.info(message)
                        }.apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        })
                    }
                }
                    .build()
            )
            .build()
            .create(ProsectorApi::class.java)
    }

    @TaskAction
    fun doWork() {
        try {
            val result = apiClient.releaseAnalysis(
                meta = meta,
                apk = MultipartBody.Part.createFormData(
                    "build_after",
                    apk.name,
                    RequestBody.create(MultipartBody.FORM, apk)
                )
            ).execute()

            //todo prosector service not so stable now, should not fail build
            //require(result.isSuccessful) { "${result.message()} ${result.errorBody()?.string()}" }
            //require(result.body()?.result == "ok") { "Service should return {result:ok} normally" }

            ciLogger.info(
                "isSuccessful = ${result.isSuccessful}; body = ${result.body()?.result}; errorBody = ${result.errorBody()?.string()}"
            )
        } catch (e: Throwable) {
            ciLogger.critical("Prosector upload failed", e)
        }
    }
}
