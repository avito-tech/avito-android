package com.avito.android.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

import org.junit.Assert.assertEquals

object SerializeUtils {

    @Throws(Exception::class)
    fun <T : Serializable?> serializeDeserialize(value: T?): T? {
        val storage = ByteArrayOutputStream()
        val out = ObjectOutputStream(storage)

        out.writeObject("start") // to check boundaries errors
        out.writeObject(value)
        out.writeObject("end") // to check boundaries errors
        out.flush()
        out.close()

        val bytes = storage.toByteArray()

        val inputStream = ObjectInputStream(ByteArrayInputStream(bytes))

        assertEquals("start", inputStream.readObject())

        @Suppress("UNCHECKED_CAST")
        val deserialized = inputStream.readObject() as T?
        assertEquals("end", inputStream.readObject())

        return deserialized
    }
}
