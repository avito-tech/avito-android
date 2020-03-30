package com.avito.test.gradle

import org.gradle.util.GUtil
import java.io.File
import java.net.URL

object HelperRuntime {

    fun isAdditionalTestKitClasspathAvailable(): Boolean = getAdditionalTestKitClasspathFile() != null

    fun getAdditionalTestKitClasspath(): List<File> {
        val file = requireNotNull(getAdditionalTestKitClasspathFile())

        val properties = GUtil.loadProperties(file)

        require(properties.containsKey(ADDITIONAL_TEST_KIT_CLASSPATH_KEY))
        { "$ADDITIONAL_TEST_KIT_PROPERTIES_FILE should contain property named $ADDITIONAL_TEST_KIT_CLASSPATH_KEY" }

        val value = properties.getProperty(ADDITIONAL_TEST_KIT_CLASSPATH_KEY)?.trim()

        require(!value.isNullOrBlank()) { "$ADDITIONAL_TEST_KIT_CLASSPATH_KEY should contain classpath" }

        return value.split(File.pathSeparator).map { File(it) }
    }

    private fun getAdditionalTestKitClasspathFile(): URL? =
        Thread.currentThread().contextClassLoader.getResource(ADDITIONAL_TEST_KIT_PROPERTIES_FILE)
}

const val ADDITIONAL_TEST_KIT_PROPERTIES_FILE = "additional-testkit-classpath.properties"
const val ADDITIONAL_TEST_KIT_CLASSPATH_KEY = "additional-testkit-classpath"
