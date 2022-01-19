package ru.avito.image_builder.internal

import java.io.File
import java.util.Properties

/**
 * Properties file to store associated attributes for image
 */
internal class ImageBuildProperties(
    val name: String
) {

    companion object {

        private const val propertiesFileName = "docker-build.properties"

        fun readFromDir(dir: File): ImageBuildProperties {
            val dockerfile = File(dir, "Dockerfile")
            check(dockerfile.exists()) {
                """
                |Directory ${dir.canonicalPath} doesn't contain Dockerfile. 
                |$propertiesFileName must be in the same directory.
                 """.trimMargin()
            }
            val propertiesFile = File(dir, propertiesFileName)
            check(propertiesFile.exists()) {
                """Directory ${dir.canonicalPath} must contain $propertiesFileName with properties to build image.
                   Required properties in file:
                   - image-name: image name to publish
                """.trimIndent()
            }
            val properties = Properties().apply {
                load(propertiesFile.reader())
            }
            return ImageBuildProperties(
                name = properties.getMandatoryProperty("image-name")
            )
        }

        private fun Properties.getMandatoryProperty(key: String): String =
            requireNotNull(getProperty(key)) {
                "$propertiesFileName file must contain '$key' record"
            }
    }
}
