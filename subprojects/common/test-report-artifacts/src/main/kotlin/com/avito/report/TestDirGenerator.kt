package com.avito.report

import com.avito.report.model.TestStaticData

public interface TestDirGenerator {

    /**
     * should be unique through test suite run to avoid artifacts collision
     */
    public fun generateUniqueDir(): String

    public class StaticData(testStaticData: TestStaticData) : TestDirGenerator {

        private val generator = Impl(
            className = testStaticData.name.className,
            methodName = testStaticData.name.methodName
        )

        override fun generateUniqueDir(): String {
            return generator.generateUniqueDir()
        }
    }

    public class Impl(
        private val className: String,
        private val methodName: String
    ) : TestDirGenerator {

        /**
         * dataset number not needed because method names are unique for them
         */
        override fun generateUniqueDir(): String {
            return buildString {
                append(className)
                append('#')
                append(methodName)
            }
        }
    }
}
