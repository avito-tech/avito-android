package com.avito.android.test.util

import com.avito.android.test.UITestConfig
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ClicksTypeRule(private val clickType: UITestConfig.ClickType) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
            object : Statement() {
                override fun evaluate() {
                    val enabled = description
                            .annotations
                            .filterIsInstance<ChangeClickType>()
                            .isNotEmpty()

                    if (enabled) {
                        UITestConfig.clicksType = clickType
                    }
                    try {
                        base.evaluate()
                    } finally {
                        if (enabled) {
                            UITestConfig.clicksType = UITestConfig.defaultClicksType
                        }
                    }
                }
            }
}

annotation class ChangeClickType