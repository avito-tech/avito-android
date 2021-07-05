package com.avito.reportviewer.model

import com.avito.test.model.TestName

public fun CrossDeviceRunTest.Companion.createStubInstance(
    name: TestName = TestName("com.test.Test", "test"),
    status: CrossDeviceStatus = CrossDeviceStatus.Success
): CrossDeviceRunTest = CrossDeviceRunTest(name, status)
