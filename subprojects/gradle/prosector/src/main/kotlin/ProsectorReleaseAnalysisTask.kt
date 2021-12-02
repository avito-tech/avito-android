
import com.avito.android.stats.statsd
import com.avito.http.HttpClientProvider
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
public abstract class ProsectorReleaseAnalysisTask : DefaultTask() {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public lateinit var apk: File

    @Input
    public lateinit var meta: ReleaseAnalysisMeta

    @Input
    public lateinit var host: String

    @Internal
    public var debug: Boolean = false

    @TaskAction
    public fun doWork() {
        val timeProvider: TimeProvider = DefaultTimeProvider()

        val httpClientProvider = HttpClientProvider(
            statsDSender = project.statsd.get(),
            timeProvider = timeProvider,
            loggerFactory = Slf4jGradleLoggerFactory
        )

        try {
            val result = createClient(httpClientProvider).releaseAnalysis(
                meta = meta,
                apk = MultipartBody.Part.createFormData(
                    "build_after",
                    apk.name,
                    apk.asRequestBody(MultipartBody.FORM)
                )
            ).execute()

            // todo prosector service not so stable now, should not fail build
            //  require(result.isSuccessful) { "${result.message()} ${result.errorBody()?.string()}" }
            //  require(result.body()?.result == "ok") { "Service should return {result:ok} normally" }

            logger.info(
                "isSuccessful = ${result.isSuccessful}; body = ${result.body()?.result}; errorBody = ${
                    result.errorBody()
                        ?.string()
                }"
            )
        } catch (e: Throwable) {
            logger.error("Prosector upload failed", e)
        }
    }

    private fun createClient(
        httpClientProvider: HttpClientProvider,
        gson: Gson = GsonBuilder().setLenient().create()
    ): ProsectorApi = Retrofit.Builder()
        .baseUrl(host)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(httpClientProvider.provide().build())
        .build()
        .create(ProsectorApi::class.java)
}
