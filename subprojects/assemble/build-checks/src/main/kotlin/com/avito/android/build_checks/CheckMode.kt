package com.avito.android.build_checks

import com.avito.android.build_checks.internal.CheckResult
import com.avito.logger.Logger
import com.avito.utils.BuildFailer

// TODO: support mode in all checks
public enum class CheckMode {

    NONE {
        override fun check(
            buildFailer: BuildFailer,
            logger: Logger,
            block: () -> CheckResult
        ) {
            // do nothing
        }
    },
    WARNING {
        override fun check(
            buildFailer: BuildFailer,
            logger: Logger,
            block: () -> CheckResult
        ) {
            when (val result = block()) {
                is CheckResult.Failed -> logger.warn(result.message)
            }
        }
    },
    FAIL {
        override fun check(
            buildFailer: BuildFailer,
            logger: Logger,
            block: () -> CheckResult
        ) {
            when (val result = block()) {
                is CheckResult.Failed -> buildFailer.failBuild(result.message)
            }
        }
    };

    internal abstract fun check(
        buildFailer: BuildFailer,
        logger: Logger,
        block: () -> CheckResult
    )
}
