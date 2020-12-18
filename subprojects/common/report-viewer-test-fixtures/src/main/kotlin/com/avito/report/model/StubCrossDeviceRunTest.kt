package com.avito.report.model

fun CrossDeviceRunTest.Companion.createStubInstance(
    name: TestName = TestName("com.test.Test.test"),
    status: CrossDeviceStatus = CrossDeviceStatus.Success
) = CrossDeviceRunTest(name, status)
