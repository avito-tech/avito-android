package ru.avito.image_builder.internal.docker

internal data class Image(
    val id: ImageId,
    val name: String,
    val tag: String
)
