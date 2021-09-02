package com.avito.android.build_verdict

internal object VerdictCases {

    interface Execution {
        fun compileKotlinFails(): String
        fun buildVerdictTaskFails(): String
        fun kaptFails(): String
        fun kaptStubGeneratingFails(): String
        fun unitTestsFails(): String
    }

    interface Configuration {
        fun wrongProjectDependencyFails(): String
        fun illegalMethodFails(): String
    }
}
