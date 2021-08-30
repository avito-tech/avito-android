package com.avito.test.summary.model

import com.avito.math.Percent
import com.avito.math.fromZeroToOnePercent
import com.avito.math.percentOf
import com.avito.report.model.Team
import com.avito.reportviewer.model.team

public data class CrossDeviceSuite(val crossDeviceRuns: List<CrossDeviceRunTest>) {

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

    val lostOnAnyDeviceCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.LostOnAnyDevice }

    val failedOnAllDevicesCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.FailedOnAllDevices }

    val failedOnAnyDeviceCount: Int
        get() = consistentData.count { it.status is CrossDeviceStatus.FailedOnAnyDevice }

    val lostOnAnyDeviceOfAutomated: Percent
    val successOfAutomated: Percent
    val skippedOnAllDevicesOfAutomated: Percent
    val failedOnAllDevicesOfAutomated: Percent
    val failedOnAnyDeviceOfAutomated: Percent

    init {
        if (automatedCount == 0) {
            val zeroPercents = 0F.fromZeroToOnePercent()
            successOfAutomated = zeroPercents
            skippedOnAllDevicesOfAutomated = zeroPercents
            failedOnAllDevicesOfAutomated = zeroPercents
            failedOnAnyDeviceOfAutomated = zeroPercents
            lostOnAnyDeviceOfAutomated = zeroPercents
        } else {
            successOfAutomated = success.percentOf(automatedCount)
            skippedOnAllDevicesOfAutomated = skippedOnAllDevicesCount.percentOf(automatedCount)
            failedOnAllDevicesOfAutomated = failedOnAllDevicesCount.percentOf(automatedCount)
            failedOnAnyDeviceOfAutomated = failedOnAnyDeviceCount.percentOf(automatedCount)
            lostOnAnyDeviceOfAutomated = lostOnAnyDeviceCount.percentOf(automatedCount)
        }
    }

    public fun filterTeam(team: Team): CrossDeviceSuite =
        CrossDeviceSuite(crossDeviceRuns.filter { it.name.team == team })
}
