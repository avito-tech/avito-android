package com.avito.android.build_verdict

internal object VerdictCases {

    interface Execution {
        fun customTaskFails(): String
        fun buildVerdictTaskFails(): String
        fun compileKotlinFails(): String
        fun kaptFails(): String
        fun kaptStubGeneratingFails(): String
        fun unitTestsFails(): String
    }

    interface Configuration {
        fun wrongProjectDependencyFails(): String
        fun illegalMethodFails(): String
    }
}
