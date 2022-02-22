package com.avito.test.report.listener.description

import org.junit.runner.Description

public interface DescriptionMetadataParser {
    public fun parse(desc: Description): DescriptionMetaData
}
