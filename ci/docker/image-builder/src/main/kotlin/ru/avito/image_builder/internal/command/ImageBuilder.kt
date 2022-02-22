package ru.avito.image_builder.internal.command

import ru.avito.image_builder.internal.docker.Image

internal interface ImageBuilder {

    fun build(): Image
}
