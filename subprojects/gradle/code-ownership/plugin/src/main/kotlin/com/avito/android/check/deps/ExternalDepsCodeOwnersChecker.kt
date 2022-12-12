package com.avito.android.check.deps

import com.avito.android.OwnerSerializer
import com.avito.android.diff.provider.OwnersProvider
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import java.io.File

internal class ExternalDepsCodeOwnersChecker(
    private val ownersSerializer: OwnerSerializer,
    private val validOwnersProvider: OwnersProvider,
) {

    fun check(
        versionsFile: File,
        ownersFile: File,
    ) {
        val mapper = TomlMapper()
        val versionsFileData = mapper.readTree(versionsFile)
        val ownersFileData = mapper.readTree(ownersFile)

        DEPENDENCIES_SECTION_NAMES.forEach { sectionName ->
            val versionsFileSection = versionsFileData[sectionName]
            val ownersFileSection = ownersFileData[sectionName]
            val versionsFileSectionPresent = versionsFileSection != null
            val ownersFileSectionPresent = ownersFileSection != null
            require(versionsFileSectionPresent == ownersFileSectionPresent) {
                "If section exists in versions file, it must also exist in owners file"
            }
            if (versionsFileSection != null && ownersFileSection != null) {
                checkSection(versionsFileSection, ownersFileSection, versionsFile, ownersFile)
            }
        }
    }

    private fun checkSection(
        versionsFileSection: JsonNode,
        ownersFileSection: JsonNode,
        versionsFile: File,
        ownersFile: File
    ) {
        val validOwners = validOwnersProvider.get()
        val validOwnersRaw = validOwners.map(ownersSerializer::serialize)
        val versionsFilePath = versionsFile.absolutePath
        val ownersFilePath = ownersFile.absolutePath

        for (dependencyName in versionsFileSection.fieldNames()) {
            require(ownersFileSection.has(dependencyName)) {
                """
                    Dependency `$dependencyName` should have an owner, but it don't.
                    Add one in $ownersFilePath.
                    Valid owners: $validOwnersRaw
                """.trimIndent()
            }

            val actualOwner = ownersFileSection[dependencyName].textValue()
            require(actualOwner in validOwnersRaw) {
                """
                    Dependency `$dependencyName` should have a valid owner, not `$actualOwner`.
                    Fix this in $ownersFilePath.
                    Valid owners: $validOwnersRaw
                """.trimIndent()
            }
        }

        for (dependencyName in ownersFileSection.fieldNames()) {
            require(versionsFileSection.has(dependencyName)) {
                """
                    Dependency `$dependencyName` have an owner, but is not present in $versionsFilePath.
                    Remove the owner from $ownersFilePath.
                """.trimIndent()
            }
        }
    }

    internal companion object {
        internal val DEPENDENCIES_SECTION_NAMES = listOf("plugins", "libraries")
    }
}
