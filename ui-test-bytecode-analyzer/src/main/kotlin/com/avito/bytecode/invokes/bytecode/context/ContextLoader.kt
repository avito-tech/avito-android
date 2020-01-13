package com.avito.bytecode.invokes.bytecode.context

import org.apache.bcel.classfile.ClassParser
import org.apache.bcel.classfile.EmptyVisitor
import org.apache.bcel.classfile.JavaClass
import java.io.File
import java.util.jar.JarFile

class ContextLoader : EmptyVisitor() {

    companion object {
        private const val CLASS_FILE_POSTFIX = "class"
    }

    private val result: MutableMap<String, JavaClass> = mutableMapOf()

    fun load(directories: Collection<File>): Context {
        require(directories.isNotEmpty()) { "can't load classes from empty list of directories" }

        directories.forEach { directory ->
            require(directory.isDirectory) { "$directory must be directory" }

            directory.walk()
                .filter { it.isFile && it.extension == CLASS_FILE_POSTFIX }
                .forEach { classFile ->
                    visitJavaClass(ClassParser(classFile.absolutePath).parse())
                }
        }

        return Context(
            classes = result
        )
    }

    fun load(jar: JarFile): Context {
        val entries = jar.entries()

        for (entry in entries) {
            if (!entry.isDirectory && entry.name.endsWith(".$CLASS_FILE_POSTFIX")) {
                visitJavaClass(
                    ClassParser(jar.name, entry.name).parse()
                )
            }
        }

        return Context(
            classes = result
        )
    }

    override fun visitJavaClass(javaClass: JavaClass) {
        result[javaClass.className] = javaClass
    }
}
