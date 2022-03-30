import com.avito.emcee.worker.Config
import com.google.common.truth.Truth
import com.squareup.moshi.Moshi
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileReader

internal class ConfigSerializationTest {

    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(Config::class.java)

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private val configFile = File(javaClass.classLoader.getResource("config.json").file)

    @Test
    fun deserialize() {
        val config = deserializeConfig()
        Truth.assertThat(config)
            .isEqualTo(
                Config(
                    workerId = "stub-worker-id",
                    restAddress = "stub-rest-address",
                    queueUrl = "stub-queue-url",
                    androidSdkPath = "/Users/john/androidSdk",
                    avd = setOf(
                        Config.Avd(21, "default", "stub-emulator-name", "stub-sd-card-name")
                    )
                )
            )
    }

    private fun deserializeConfig(): Config {
        val fileReader = FileReader(configFile)
        val json = fileReader.readText()
        return requireNotNull(adapter.fromJson(json)) {
            "Failed to deserialize config $json"
        }
    }
}
