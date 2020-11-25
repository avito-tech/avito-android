package com.avito.report.model

import com.avito.android.Percent
import com.avito.android.percentOf

data class CrossDeviceSuite(val crossDeviceRuns: List<CrossDeviceRunTest>) {

    private val consistentData: List<CrossDeviceRunTest>
        get() = crossDeviceRuns.filter { it.status !is CrossDeviceStatus.Inconsistent }

    val manualCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.Manual }

    val automatedCount: Int
        get() = consistentData.count { it.status !is CrossDeviceStatus.Manual }

    val success: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.Success }

    val skippedOnAllDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.SkippedOnAllDevices }

    val lostOnSomeDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.LostOnSomeDevices }

    val percentLostOnSomeDevicesOfAutomated: Percent
        get() = lostOnSomeDevicesCount.percentOf(automatedCount)

    val inconsistentCount: Int
        get() = crossDeviceRuns.count { it.status is CrossDeviceStatus.Inconsistent }

    val percentSuccessOfAutomated: Percent = success.percentOf(automatedCount)

    val percentSkippedOnAllDevicesOfAutomated: Percent = skippedOnAllDevicesCount.percentOf(automatedCount)

    val failedOnAllDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.FailedOnAllDevices }

    val percentFailedOnAllDevicesOfAutomated: Percent = failedOnAllDevicesCount.percentOf(automatedCount)

    val failedOnSomeDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.FailedOnSomeDevices }

    val percentFailedOnSomeDevicesOfAutomated: Percent = failedOnSomeDevicesCount.percentOf(automatedCount)

    fun filterTeam(team: Team): CrossDeviceSuite =
        CrossDeviceSuite(crossDeviceRuns.filter { it.name.team == team })
}
