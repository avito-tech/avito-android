package com.avito.report

import com.avito.report.model.TestStaticData

interface TestDirGenerator {

    /**
     * should be unique through test suite run to avoid artifacts collision
     */
    fun generateUniqueDir(): String

    class StaticData(testStaticData: TestStaticData) : TestDirGenerator {

        private val generator = Impl(
            className = testStaticData.name.className,
            methodName = testStaticData.name.methodName
        )

        override fun generateUniqueDir(): String {
            return generator.generateUniqueDir()
        }
    }

    class Impl(
        private val className: String,
        private val methodName: String
    ) : TestDirGenerator {

        /**
         * dataset number not needed because method names are unique for them
         *
         * todo need deviceName
         */
        override fun generateUniqueDir(): String {
            return buildString {
                append(className)
                append('#')
                append(methodName)
            }
        }
    }

    object Stub : TestDirGenerator {

        override fun generateUniqueDir(): String = "test#test"
    }
}
