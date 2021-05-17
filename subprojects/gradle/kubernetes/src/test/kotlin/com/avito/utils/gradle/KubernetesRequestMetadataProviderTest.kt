package com.avito.utils.gradle

import com.avito.http.internal.RequestMetadataProvider
import com.avito.truth.ResultSubject
import com.google.common.truth.Truth.assertThat
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.junit.jupiter.api.Test

internal class KubernetesRequestMetadataProviderTest {

    @Test
    fun `metadata contains get deployments request`() {
        val request = Request.Builder()
            .url("https://k8s.service:8443/apis/apps/v1/namespaces/android-emulator/deployments")
            .build()

        val metadataProvider: RequestMetadataProvider = createRequestMetadataProvider()

        val result = metadataProvider.provide(request)

        ResultSubject.assertThat(result).isSuccess().withValue {
            assertThat(it.methodName).isEqualTo("deployments_get")
        }
    }

    @Test
    fun `metadata contains put deployments request`() {
        val request = Request.Builder()
            .url(
                "https://k8s.service:8443/" +
                    "apis/apps/v1/namespaces/android-emulator/deployments/" +
                    "android-emulator-00f2f120-f5f7-4481-b34a-4466857701b6"
            )
            .method("PUT", RequestBody.create(MediaType.parse("application/yaml"), "some yaml content"))
            .build()

        val metadataProvider: RequestMetadataProvider = createRequestMetadataProvider()

        val result = metadataProvider.provide(request)

        ResultSubject.assertThat(result).isSuccess().withValue {
            assertThat(it.methodName).isEqualTo("deployments_put")
        }
    }

    private fun createRequestMetadataProvider(): RequestMetadataProvider = KubernetesRequestMetadataProvider()
}
