package ru.avito.image_builder.internal

internal class RegistryCredentials(
    val registry: String?,
    val username: String,
    val password: String,
) {
    init {
        require(username.isNotEmpty()) {
            "Username must be not empty"
        }
    }
}
