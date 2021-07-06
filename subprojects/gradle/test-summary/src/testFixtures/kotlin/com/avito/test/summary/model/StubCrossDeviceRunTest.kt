package com.avito.test.summary.model

import com.avito.test.model.TestName
import com.avito.test.model.createStubInstance

public fun CrossDeviceRunTest.Companion.createStubInstance(
    name: TestName = TestName.createStubInstance(),
    status: CrossDeviceStatus = CrossDeviceStatus.Success,
): CrossDeviceRunTest {
    return CrossDeviceRunTest(
        name = name,
        status = status
    )
}
