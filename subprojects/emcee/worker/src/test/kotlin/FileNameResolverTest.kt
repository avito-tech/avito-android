import com.avito.emcee.worker.internal.artifacts.FileNameResolver
import com.google.common.truth.Truth.assertThat
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.jupiter.api.Test

internal class FileNameResolverTest {

    private val fileNameResolver = FileNameResolver()

    @Test
    fun resolve() {
        val url = "https://artifactory.com/artifactory/repository/bucket/test.apk".toHttpUrl()
        assertThat(fileNameResolver.resolve(url)).isEqualTo("test.apk")
    }
}
