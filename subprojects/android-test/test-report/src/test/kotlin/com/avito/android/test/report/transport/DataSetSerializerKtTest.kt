@file:Suppress("IllegalIdentifier")

package com.avito.android.test.report.transport

import com.avito.android.test.report.model.DataSet
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class DataSetSerializerKtTest : PreTransportMappers {

    @Test
    fun `serialize() - produces null - for empty dataSet`() {
        val dataSet: DataSet = object : DataSet {}
        assertThat(dataSet.serialize()).isEqualTo(emptyMap<Any, Any>())
    }

    @Test
    fun `serialize() - produces map with single property - for dataSet with single property (+number)`() {
        class SinglePropertyDataSet(@Suppress("unused") val myProperty: String) : DataSet

        assertThat(SinglePropertyDataSet("myValue").serialize())
            .isEqualTo(mapOf("myProperty" to "myValue"))
    }
}
