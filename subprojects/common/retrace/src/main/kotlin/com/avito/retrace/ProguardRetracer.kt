package com.avito.retrace

import java.io.File

public interface ProguardRetracer {

    public fun retrace(content: String): String

    public companion object {

        public fun create(mappings: List<File>): ProguardRetracer = ProguardRetracerImpl(mappings)
    }
}
