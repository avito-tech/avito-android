package com.avito.report.model

/**
 * Test data that can be parsed even without actual test run
 *
 * todo avito specific properties shouldn't be in generic model
 */
public interface TestStaticData : AvitoSpecificTestStaticData {

    public val name: TestName

    public val device: DeviceName

    public val description: String?

    public val dataSetNumber: Int?

    public val flakiness: Flakiness
}
