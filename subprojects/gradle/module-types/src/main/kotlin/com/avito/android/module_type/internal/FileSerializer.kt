package com.avito.android.module_type.internal

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

internal object FileSerializer {

    fun write(obj: Serializable, output: File) {
        ObjectOutputStream(FileOutputStream(output)).use {
            it.writeObject(obj)
        }
    }

    fun read(input: File): Any {
        return ObjectInputStream(FileInputStream(input)).use {
            it.readObject()
        }
    }
}
