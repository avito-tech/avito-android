package com.avito.report.model

import java.io.Serializable

/**
 * todo extract to public api module (used in avito build scripts)
 */
public data class Team(val name: String) : Serializable {

    public companion object {
        public val UNDEFINED: Team = Team("undefined")
    }
}
