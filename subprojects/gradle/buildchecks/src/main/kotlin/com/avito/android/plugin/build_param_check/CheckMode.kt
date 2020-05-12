package com.avito.android.plugin.build_param_check

import com.avito.utils.buildFailer
import com.avito.utils.logging.ciLogger
import org.gradle.api.Project

enum class CheckMode {
    NONE {
        override fun check(project: Project, block: () -> CheckResult) {
            // do nothing
        }
    },
    WARNING {
        override fun check(project: Project, block: () -> CheckResult) {
            when (val result = block()) {
                is CheckResult.Failed -> project.ciLogger.info(result.message)
            }
        }
    },
    FAIL {
        override fun check(project: Project, block: () -> CheckResult) {
            when (val result = block()) {
                is CheckResult.Failed -> project.buildFailer.failBuild(result.message)
            }
        }
    };

    internal abstract fun check(project: Project, block: () -> CheckResult)
}
