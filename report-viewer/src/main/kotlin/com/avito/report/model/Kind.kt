package com.avito.report.model

enum class Kind(val tmsId: String) {
    UNIT("unit"),
    UI_COMPONENT("ui-component"),
    INTEGRATION("integration"),
    E2E("e2e"),
    MANUAL("manual"),

    /**
     * выступает как NULL OBJECT, на случай если встретится старый отчет в котором kind не выставлено значение
     */
    UNKNOWN("unknown");

    companion object {
        fun fromTmdId(tmsId: String): Kind? = values().find { it.tmsId == tmsId }
    }
}
