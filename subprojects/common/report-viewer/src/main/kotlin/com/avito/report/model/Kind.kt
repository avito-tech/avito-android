package com.avito.report.model

enum class Kind(val tmsId: String) {
    UNIT("unit"),
    UI_COMPONENT("ui-component"),
    INTEGRATION("integration"),
    E2E("e2e"),

    /**
     * see TestManagementSystem doc
     */
    UI_COMPONENT_STUB("ui-component-stub"),
    E2E_STUB("e2e-stub"),
    MANUAL("manual"),

    /**
     * 'null object'
     * for backward compatibility and unexpected cases
     */
    UNKNOWN("unknown");

    companion object {
        fun fromTmdId(tmsId: String): Kind? = values().find { it.tmsId == tmsId }
    }
}
