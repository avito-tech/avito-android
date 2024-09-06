package com.avito.android.network

import com.avito.android.model.network.AvitoOwner
import com.avito.android.model.network.AvitoOwnersClient
import com.avito.android.model.network.AvitoPeoplePerson
import com.avito.android.model.network.OwnerType

class FakeAvitoOwnersClient : AvitoOwnersClient {

    private val speedTeam = AvitoOwner(
        id = "1",
        name = "Speed",
        type = OwnerType.Team,
        parentId = "2",
        channels = emptyList(),
        people = listOf(
            AvitoPeoplePerson(id = "1", email = "speed1@avito.ru"),
            AvitoPeoplePerson(id = "1", email = "speed2@avito.ru")
        )
    )
    private val mobileArchitectureUnit = AvitoOwner(
        id = "2",
        name = "Mobile Architecture",
        type = OwnerType.Unit,
        channels = emptyList(),
        children = mutableListOf(speedTeam),
        people = listOf(
            AvitoPeoplePerson(id = "1", email = "mobarch1@avito.ru"),
            AvitoPeoplePerson(id = "1", email = "mobarch2@avito.ru")
        )
    )

    override fun getAvitoOwners(): List<AvitoOwner> = listOf(speedTeam, mobileArchitectureUnit)
}
