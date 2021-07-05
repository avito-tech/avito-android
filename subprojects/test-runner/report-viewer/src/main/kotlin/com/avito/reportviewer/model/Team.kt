package com.avito.reportviewer.model

import java.io.Serializable

public data class Team(val name: String) : Serializable {

    public companion object {
        public val UNDEFINED: Team = Team("undefined")
    }
}
