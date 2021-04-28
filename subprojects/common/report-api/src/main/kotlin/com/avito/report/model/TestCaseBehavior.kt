package com.avito.report.model

// todo move to report-viewer module (avito specific)
public enum class TestCaseBehavior(public val tmsValue: Int) {
    POSITIVE(2), NEGATIVE(3), UNDEFINED(1);

    public companion object {

        public fun fromId(tmsId: Int): TestCaseBehavior? =
            values().find { it.tmsValue == tmsId }

        public fun fromName(name: String): TestCaseBehavior? =
            values().find { it.name == name.toUpperCase() }
    }
}
