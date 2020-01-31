package com.avito.test.gradle

import java.io.File
import java.net.URL

inline fun <reified C> fileFromJarResources(name: String) = File(C::class.java.classLoader.getResource(name).file)

inline fun <reified C> resourceFrom(name: String): URL = C::class.java.classLoader.getResource(name)
