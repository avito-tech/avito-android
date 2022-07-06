package com.avito.robolectric.runner.description

import com.avito.test.report.listener.description.DescriptionMetaData
import com.avito.test.report.listener.description.DescriptionMetadataParser
import org.junit.runner.Description

internal class RobolectricDescriptionMetaDataParser : DescriptionMetadataParser {

    override fun parse(desc: Description): DescriptionMetaData {
        val result = REGEX.find(desc.displayName)
            ?: throw IllegalArgumentException("Cannot parse description: ${desc.displayName}")

        return DescriptionMetaData(
            environment = "Robolectric-${result.groupValues[2]}",
            testName = result.groupValues[1],
            className = result.groupValues[3]
        )
    }

    private companion object {
        private val REGEX = Regex("([\\s\\S]*)\\[(.*)]\\((.*)\\)")
    }
}
