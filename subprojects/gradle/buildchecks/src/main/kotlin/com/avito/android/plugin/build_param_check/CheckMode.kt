package com.avito.android.plugin.build_param_check

import com.avito.utils.buildFailer
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
                is CheckResult.Failed -> project.logger.error(result.message)
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

    abstract fun check(project: Project, block: () -> CheckResult)
}
