package com.avito.report.model

/**
 * https://avito-tech.github.io/avito-android/docs/test/testcaseincode/
 * todo move to report-viewer module (avito specific)
 */
public enum class Kind(public val tmsId: String) {
    UNIT("unit"),
    UI_COMPONENT("ui-component"),
    INTEGRATION("integration"),
    E2E("e2e"),

    UI_COMPONENT_STUB("ui-component-stub"),
    E2E_STUB("e2e-stub"),
    MANUAL("manual"),

    /**
     * 'null object'
     * for backward compatibility and unexpected cases
     */
    UNKNOWN("unknown");

    public companion object {
        public fun fromTmsId(tmsId: String): Kind? = values().find { it.tmsId == tmsId }
    }
}
