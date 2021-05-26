package com.avito.report

public interface TestDirGenerator {

    /**
     * should be unique through test suite run to avoid artifacts collision
     */
    public fun generateUniqueDir(): String

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
