package com.avito.utils

import java.io.File
import java.net.URL

inline fun <reified C> fileFromJarResources(name: String) = File(resourceFrom<C>(name).file)

inline fun <reified C> resourceFrom(name: String): URL = C::class.java.classLoader.getResource(name)!!
