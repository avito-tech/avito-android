package com.avito.alertino.model

public data class AlertinoRecipient(
    val name: String
) {

    init {
        require(name.startsWith("#") || name.startsWith("@")) {
            "Alertino recipient name must starts with # or @"
        }
    }
}
