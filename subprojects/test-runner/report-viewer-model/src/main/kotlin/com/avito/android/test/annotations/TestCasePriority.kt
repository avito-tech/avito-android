package com.avito.android.test.annotations

// todo move to report-viewer module (avito specific)
public enum class TestCasePriority(public val tmsValue: Int) {
    CRITICAL(4), MAJOR(3), NORMAL(2), MINOR(1);

    public companion object {

        public fun fromId(tmsId: Int): TestCasePriority? = values().find { it.tmsValue == tmsId }

        public fun fromName(name: String): TestCasePriority? = values().find { it.name == name.uppercase() }
    }
}
