package com.avito.android.docker

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.builder.BuildContextBuilder
import org.funktionale.tries.Try
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.net.SocketException
import java.util.Base64

interface Docker {

    fun build(directory: File): Try<String>

    fun tag(imageId: String, imageName: ImageName): Try<ImageName>

    fun push(imageName: ImageName): Try<ImageName>

    class Impl(project: Project) : Docker {

        private val dockerCredentials: DockerCredentials by lazy { DockerCredentials.Implementation(project) }

        private val dockerClient: DockerClient = DockerClientImpl()

        override fun build(directory: File): Try<String> {
            val result = try {
                dockerClient.buildWithLogs(buildContext(directory))
            } catch (e: SocketException) {
                return Try.Failure(DockerException("Is Docker daemon running?", e))
            }

            return if (!result.imageId.isNullOrBlank()) {
                Try.Success(result.imageId.substringAfter("sha256:"))
            } else {
                Try.Failure(DockerException(result.log.joinToString(separator = "\n")))
            }
        }

        override fun tag(imageId: String, imageName: ImageName): Try<ImageName> {
            val tagResponse = dockerClient.tag(imageId, imageName.toString())

            return if (tagResponse.status.isSuccess) {
                Try.Success(imageName)
            } else {
                Try.Failure(DockerException(tagResponse.content.toString()))
            }
        }

        override fun push(imageName: ImageName): Try<ImageName> {
            val result = dockerClient.push(
                imageName.toString(),
                createDockerAuthToken(dockerCredentials.login, dockerCredentials.password)
            )

            return if (result.status.isSuccess) {
                Try.Success(imageName)
            } else {
                Try.Failure(DockerException(result.content.toString()))
            }
        }

        private fun buildContext(directory: File): FileInputStream {
            val buildContext = File.createTempFile("buildContext", ".tar").also { it.deleteOnExit() }
            BuildContextBuilder.archiveTarFilesRecursively(
                directory,
                buildContext
            )
            return FileInputStream(buildContext)
        }

        private fun createDockerAuthToken(login: String, password: String): String {
            require(login.isNotBlank()) { "docker login must be non-blank" }
            require(password.isNotBlank()) { "docker password must be non-blank" }
            val json = "{\"username\": \"$login\", \"password\":\"$password\"}"
            return Base64.getEncoder().encodeToString(json.toByteArray())
        }
    }

    companion object {

        fun fromProject(project: Project): Docker = Impl(project)
    }

    data class ImageName(
        val name: String,
        val tag: String = "latest",
        val registry: String
    ) {
        override fun toString(): String = "$registry/$name:$tag"
    }

    class DockerException : IllegalStateException {
        constructor(message: String) : super(message)
        constructor(message: String, cause: Exception) : super(message, cause)
    }
}
