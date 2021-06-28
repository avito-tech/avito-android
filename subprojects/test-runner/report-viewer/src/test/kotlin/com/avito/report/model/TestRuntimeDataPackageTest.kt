package com.avito.report.model

import com.avito.report.serialize.createReportGson
import com.github.salomonbrys.kotson.fromJson
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestRuntimeDataPackageTest {

    @Test
    fun `serialize deserialize test`() {
        val testRuntimeDataPackage = TestRuntimeDataPackage.createStubInstance()

        val gson = createReportGson()
        val json = gson.toJson(testRuntimeDataPackage)
        val result = gson.fromJson<TestRuntimeDataPackage>(json)
        assertThat(result).isEqualTo(testRuntimeDataPackage)
    }
}
