package com.avito.test.gradle.files

import com.avito.test.gradle.Generator
import com.avito.test.gradle.kotlinClass
import java.io.File

public class InstrumentationTest(public val className: String) : Generator {

    override fun generateIn(file: File) {
        file.kotlinClass(className, content = {
            """
            import org.junit.Test

            class $className {

                @Test
                fun test() {
                    //success
                }
            }
        """.trimIndent()
        })
    }
}
