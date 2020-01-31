@file:Suppress("FunctionName", "SpellCheckingInspection")

package com.avito.test.gradle

import java.io.File

fun File.kotlinClass(className: String, content: () -> String? = { "class $className" }) =
    file("$className.kt", content.invoke())

fun File.kotlinClass(
    className: String,
    packageName: String,
    content: () -> String = {
        """
            package $packageName
            class $className
        """.trimIndent()
    }
) = file("${packageName.replace(".", "/")}/$className.kt", content.invoke())

fun File.module(
    name: String,
    configuration: File.() -> Unit
) = dir(name, configuration)

fun File.mutate(content: String = "changes") = apply {
    writeText(content)
}

fun File.dir(
    path: String,
    mutator: File.() -> Unit = {}
): File {
    val dir = File(this, path)
    dir.mkdirs()
    dir.mutator()
    return dir
}

fun File.file(name: String, content: String? = null): File {
    val file: File = if (name.contains('/')) {
        val directory = File(this, name.substringBeforeLast('/'))
        directory.mkdirs()
        File(directory, name.substringAfterLast('/'))
    } else {
        File(this, name)
    }
    if (!file.exists()) {
        file.createNewFile()
    }
    if (content != null) {
        file.writeText(content.trimIndent())
    }
    return file
}

fun File.append(path: String, content: String): File {
    return File(this, path).apply { appendText(content) }
}
