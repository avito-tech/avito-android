package com.avito.utils

import java.io.File
import java.net.URL

/**
 * @param path relative to packed resource;
 *             for example `src/test/resources/some-file.txt` should be referenced as `some-file.txt`
 *             and `src/test/resources/dir/another-file.txt` as `dir/another-file.txt`
 *
 * @param C any your class available should work; it is used to get classloader instance
 *          for example:
 *
 *          class YourClass {
 *              fun foo() {
 *                  fileFromJarResources<YourClass>("some-file.txt")
 *              }
 *          }
 */
inline fun <reified C> fileFromJarResources(path: String): File = File(resourceFrom<C>(path).file)

inline fun <reified C> resourceFrom(path: String): URL = C::class.java.classLoader.getResource(path)!!
