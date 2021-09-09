package com.avito.k8s

import com.avito.android.Result
import com.avito.http.internal.RequestMetadata
import com.avito.http.internal.RequestMetadataProvider
import okhttp3.Request

/**
 * API reference: https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.21/#deployment-v1-apps
 */
internal class KubernetesRequestMetadataProvider : RequestMetadataProvider {

    override fun provide(request: Request): Result<RequestMetadata> {
        val pathSegments = request.url().pathSegments()

        val serviceName = "kubernetes"

        try {
            when (pathSegments[0]) {
                "api" -> if (pathSegments[2] == "namespaces") {
                    if (pathSegments[4] == "pods") {
                        return Result.Success(
                            RequestMetadata(
                                serviceName = serviceName,
                                methodName = "pods_${request.method().lowercase()}"
                            )
                        )
                    }
                }
                "apis" -> {
                    val apiGroup = pathSegments[1]
                    if (apiGroup == "apps") {
                        if (pathSegments[3] == "namespaces") {
                            if (pathSegments[5] == "deployments") {
                                return Result.Success(
                                    RequestMetadata(
                                        serviceName = serviceName,
                                        methodName = "deployments_${request.method().lowercase()}"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            return Result.Failure(IllegalArgumentException(getErrorMessage(request), e))
        }

        return Result.Failure(IllegalArgumentException(getErrorMessage(request)))
    }

    private fun getErrorMessage(request: Request): String {
        return buildString {
            appendLine("KubernetesRequestMetadataProvider: unknown k8s API method")
            appendLine("Original request was: ${request.method()} ${request.url()}")
            appendLine("To fix it just add required method handling")
        }
    }
}
