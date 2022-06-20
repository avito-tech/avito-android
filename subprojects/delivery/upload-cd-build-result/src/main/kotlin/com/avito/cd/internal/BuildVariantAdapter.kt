package com.avito.cd.internal

import com.avito.cd.model.BuildVariant
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

internal class BuildVariantAdapter : TypeAdapter<BuildVariant>() {

    override fun write(out: JsonWriter, buildVariant: BuildVariant?) {
        if (buildVariant == null) {
            out.nullValue()
        } else {
            out.value(buildVariant.name)
        }
    }

    override fun read(input: JsonReader): BuildVariant {
        return BuildVariant(input.nextString())
    }
}
