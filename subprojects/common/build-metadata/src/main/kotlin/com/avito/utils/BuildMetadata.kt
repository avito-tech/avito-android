package com.avito.utils

import java.util.Properties
import java.util.jar.Attributes

object BuildMetadata {

    /**
     * Build version of a Kotlin library that contains the class
     */
    fun <T : Any> kotlinLibraryVersion(clazz: Class<T>): String {
        val version: String? = clazz.`package`.implementationVersion
        val isEmpty = version.isNullOrBlank() || version == "0.0"
        check(!isEmpty) {
            "Can't load implementation version for $this. Value: \"$version\". Check manifest options for Jar"
        }
        return version!!
    }

    /**
     * Build version of an Android library that contains the class
     *
     * @moduleName - Gradle project name with the class
     */
    fun androidLibVersion(moduleName: String): String {
        val fileName = "META-INF/com.avito.android.$moduleName.properties"
        val propertiesStream = requireNotNull(BuildMetadata::class.java.classLoader!!.getResourceAsStream(fileName)) {
            "Can't find $fileName for $moduleName module. Check generated resources in the library"
        }
        val properties = Properties()
        properties.load(propertiesStream)

        val propertyName = Attributes.Name.IMPLEMENTATION_VERSION.toString()

        return requireNotNull(properties.getProperty(propertyName)) {
            "Can't find $propertyName in $fileName"
        }
    }
}
