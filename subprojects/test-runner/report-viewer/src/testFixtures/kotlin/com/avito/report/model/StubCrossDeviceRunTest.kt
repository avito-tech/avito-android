package com.avito.report.model

public fun CrossDeviceRunTest.Companion.createStubInstance(
    name: TestName = TestName("com.test.Test", "test"),
    status: CrossDeviceStatus = CrossDeviceStatus.Success
): CrossDeviceRunTest = CrossDeviceRunTest(name, status)
