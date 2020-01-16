package com.avito.report.model

sealed class CreateResult {
    data class Created(val id: String) : CreateResult()
    object AlreadyCreated : CreateResult()
    data class Failed(val exception: Throwable) : CreateResult()
}
