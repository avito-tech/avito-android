package com.avito.report

import com.avito.test.model.TestName

public interface TestDirGenerator {

    /**
     * should be unique through test suite run to avoid artifacts collision
     */
    public fun generateUniqueDir(): String

    public class Impl(
        private val name: TestName
    ) : TestDirGenerator {

        /**
         * dataset number not needed because method names are unique for them
         */
        override fun generateUniqueDir(): String {
            return "${name.className}#${name.methodName}"
        }
    }
}
