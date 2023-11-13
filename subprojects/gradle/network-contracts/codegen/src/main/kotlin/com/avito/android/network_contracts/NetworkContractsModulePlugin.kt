package com.avito.android.network_contracts

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class NetworkContractsModulePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        @Suppress("UNUSED_VARIABLE") val networkContractsExtension =
            target.extensions.create<NetworkContractsModuleExtension>(NetworkContractsModuleExtension.NAME)

        // TODO: will be implemented in https://jr.avito.ru/browse/MA-3631
    }
}
