package com.avito.android.proguard_guard.configuration

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import proguard.ClassPath
import proguard.ClassPathEntry
import proguard.ClassSpecification
import proguard.Configuration
import proguard.KeepClassSpecification
import proguard.MemberSpecification
import java.io.File

class ConfigurationSorterTest {

    @Test
    fun `configuration is sorted ascending`() {
        val configuration = Configuration().apply {
            programJars = createClassPath()
            libraryJars = createClassPath()
            keepDirectories = listOf("1", "0")
            keep = listOf(
                KeepClassSpecification(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    createClassSpecification(
                        intValues = 1,
                        stringValues = "1",
                        fieldSpec = listOf(
                            createMemberSpecification(1, "1"),
                            createMemberSpecification(0, "0")
                        ),
                        methodSpec = listOf(
                            createMemberSpecification(1, "1"),
                            createMemberSpecification(0, "0")
                        )
                    ),
                    createClassSpecification(
                        intValues = 1,
                        stringValues = "1",
                        fieldSpec = listOf(
                            createMemberSpecification(1, "1"),
                            createMemberSpecification(0, "0")
                        ),
                        methodSpec = listOf(
                            createMemberSpecification(1, "1"),
                            createMemberSpecification(0, "0")
                        )
                    ),
                ),
                KeepClassSpecification(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    createClassSpecification(1, "1"),
                    createClassSpecification(1, "1")
                )
            )
            whyAreYouKeeping = createClassSpecificationList()
            optimizations = listOf("2", "1", "0")
            assumeNoSideEffects = createClassSpecificationList()
            assumeNoExternalSideEffects = createClassSpecificationList()
            assumeNoEscapingParameters = createClassSpecificationList()
            assumeNoExternalReturnValues = createClassSpecificationList()
            assumeValues = createClassSpecificationList()
            keepPackageNames = listOf("3", "2", "1")
            keepAttributes = listOf("4", "3", "2")
            adaptClassStrings = listOf("5", "4", "3")
            adaptResourceFileNames = listOf("6", "4", "5")
            adaptResourceFileContents = listOf("", "")
            note = listOf("b", "a", "c")
            warn = listOf("z", "x", "y")
        }

        val expectedConfiguration = Configuration().apply {
            programJars = createSortedClassPath()
            libraryJars = createSortedClassPath()
            keepDirectories = listOf("0", "1")
            keep = listOf(
                KeepClassSpecification(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    createClassSpecification(1, "1"),
                    createClassSpecification(1, "1")
                ),
                KeepClassSpecification(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    createClassSpecification(
                        intValues = 1,
                        stringValues = "1",
                        fieldSpec = listOf(
                            createMemberSpecification(0, "0"),
                            createMemberSpecification(1, "1"),
                        ),
                        methodSpec = listOf(
                            createMemberSpecification(0, "0"),
                            createMemberSpecification(1, "1"),
                        )
                    ),
                    createClassSpecification(
                        intValues = 1,
                        stringValues = "1",
                        fieldSpec = listOf(
                            createMemberSpecification(0, "0"),
                            createMemberSpecification(1, "1"),
                        ),
                        methodSpec = listOf(
                            createMemberSpecification(0, "0"),
                            createMemberSpecification(1, "1"),
                        )
                    ),
                ),
            )
            whyAreYouKeeping = createClassSpecificationSortedList()
            optimizations = listOf("0", "1", "2")
            assumeNoSideEffects = createClassSpecificationSortedList()
            assumeNoExternalSideEffects = createClassSpecificationSortedList()
            assumeNoEscapingParameters = createClassSpecificationSortedList()
            assumeNoExternalReturnValues = createClassSpecificationSortedList()
            assumeValues = createClassSpecificationSortedList()
            keepPackageNames = listOf("1", "2", "3")
            keepAttributes = listOf("2", "3", "4")
            adaptClassStrings = listOf("3", "4", "5")
            adaptResourceFileNames = listOf("4", "5", "6")
            adaptResourceFileContents = listOf("", "")
            note = listOf("a", "b", "c")
            warn = listOf("x", "y", "z")
        }

        configuration.sortConfigurationInPlace()

        assertThat(configuration.asString()).isEqualTo(expectedConfiguration.asString())
    }

    @Test
    fun `configuration fields could be null`() {
        val configuration = Configuration().apply {
            programJars = null
            libraryJars = null
            keepDirectories = null
            keep = null
            whyAreYouKeeping = null
            optimizations = null
            assumeNoSideEffects = null
            assumeNoExternalSideEffects = null
            assumeNoEscapingParameters = null
            assumeNoExternalReturnValues = null
            assumeValues = null
            keepPackageNames = null
            keepAttributes = null
            adaptClassStrings = null
            adaptResourceFileNames = null
            adaptResourceFileContents = null
            note = null
            warn = null
        }

        configuration.sortConfigurationInPlace()
        configuration.asString()
    }

    @Test
    fun `configuration fields could be empty`() {
        val configuration = Configuration().apply {
            programJars = ClassPath()
            libraryJars = ClassPath()
            keepDirectories = mutableListOf<String>()
            keep = mutableListOf<KeepClassSpecification>()
            whyAreYouKeeping = mutableListOf<ClassSpecification>()
            optimizations = mutableListOf<String>()
            assumeNoSideEffects = mutableListOf<ClassSpecification>()
            assumeNoExternalSideEffects = mutableListOf<ClassSpecification>()
            assumeNoEscapingParameters = mutableListOf<ClassSpecification>()
            assumeNoExternalReturnValues = mutableListOf<ClassSpecification>()
            assumeValues = mutableListOf<ClassSpecification>()
            keepPackageNames = mutableListOf<String>()
            keepAttributes = mutableListOf<String>()
            adaptClassStrings = mutableListOf<String>()
            adaptResourceFileNames = mutableListOf<String>()
            adaptResourceFileContents = mutableListOf<String>()
            note = mutableListOf<String>()
            warn = mutableListOf<String>()
        }

        configuration.sortConfigurationInPlace()
        configuration.asString()
    }

    @Test
    fun `configuration is sorted after reading from file`(@TempDir dir: File) {
        val configFile = dir.resolve("config")
        configFile.writeText("""
            -optimizations !field/*,!class/merging/*
        """.trimIndent())

        val configuration = parseConfigurationSorted(configFile)

        assertThat(configuration.optimizations).isEqualTo(listOf("!class/merging/*", "!field/*"))
    }

    private fun createClassPath(): ClassPath {
        return ClassPath().apply {
            add(
                ClassPathEntry(
                    File("1"),
                    true
                ).apply {
                    filter = listOf("1")
                }
            )
            add(
                ClassPathEntry(
                    File("1"),
                    true
                ).apply {
                    filter = listOf("1", "0")
                    apkFilter = listOf("1", "0")
                    jarFilter = listOf("1", "0")
                    aarFilter = listOf("1", "0")
                    warFilter = listOf("1", "0")
                    earFilter = listOf("1", "0")
                    jmodFilter = listOf("1", "0")
                    zipFilter = listOf("1", "0")
                }
            )
            add(
                ClassPathEntry(
                    File("1"),
                    true
                )
            )
            add(
                ClassPathEntry(
                    File("0"),
                    true
                )
            )
            add(
                ClassPathEntry(
                    File("0"),
                    false
                )
            )
        }
    }

    private fun createSortedClassPath(): ClassPath {
        return ClassPath().apply {
            add(
                ClassPathEntry(
                    File("0"),
                    false
                )
            )
            add(
                ClassPathEntry(
                    File("0"),
                    true
                )
            )
            add(
                ClassPathEntry(
                    File("1"),
                    true
                )
            )
            add(
                ClassPathEntry(
                    File("1"),
                    true
                ).apply {
                    filter = listOf("0", "1")
                    apkFilter = listOf("0", "1")
                    jarFilter = listOf("0", "1")
                    aarFilter = listOf("0", "1")
                    warFilter = listOf("0", "1")
                    earFilter = listOf("0", "1")
                    jmodFilter = listOf("0", "1")
                    zipFilter = listOf("0", "1")
                }
            )
            add(
                ClassPathEntry(
                    File("1"),
                    true
                ).apply {
                    filter = listOf("1")
                }
            )
        }
    }

    private fun createClassSpecification(
        intValues: Int,
        stringValues: String,
        fieldSpec: List<MemberSpecification>? = null,
        methodSpec: List<MemberSpecification>? = null,
    ): ClassSpecification {
        return ClassSpecification(
            "", intValues, intValues, stringValues, stringValues, stringValues, stringValues,
            fieldSpec, methodSpec
        )
    }

    private fun createMemberSpecification(
        intValues: Int,
        stringValues: String,
    ): MemberSpecification {
        return MemberSpecification(intValues, intValues, stringValues, stringValues, stringValues)
    }

    private fun createClassSpecificationList(): List<ClassSpecification> {
        return listOf(
            createClassSpecification(1, "1"),
            createClassSpecification(1, "0"),
            createClassSpecification(
                intValues = 1,
                stringValues = "1",
                fieldSpec = listOf(
                    createMemberSpecification(1, "1"),
                    createMemberSpecification(1, "0")
                ),
                methodSpec = listOf(
                    createMemberSpecification(1, "1"),
                    createMemberSpecification(1, "0")
                )
            ),
            createClassSpecification(
                intValues = 1,
                stringValues = "0",
                fieldSpec = listOf(
                    createMemberSpecification(1, "1"),
                    createMemberSpecification(1, "0")
                ),
            ),
            createClassSpecification(
                intValues = 1,
                stringValues = "0",
                fieldSpec = listOf(
                    createMemberSpecification(2, "2"),
                    createMemberSpecification(1, "1")
                ),
            ),
        )
    }

    private fun createClassSpecificationSortedList(): List<ClassSpecification> {
        return listOf(
            createClassSpecification(1, "0"),
            createClassSpecification(
                intValues = 1,
                stringValues = "0",
                fieldSpec = listOf(
                    createMemberSpecification(1, "0"),
                    createMemberSpecification(1, "1"),
                ),
            ),
            createClassSpecification(
                intValues = 1,
                stringValues = "0",
                fieldSpec = listOf(
                    createMemberSpecification(1, "1"),
                    createMemberSpecification(2, "2"),
                ),
            ),
            createClassSpecification(1, "1"),
            createClassSpecification(
                intValues = 1,
                stringValues = "1",
                fieldSpec = listOf(
                    createMemberSpecification(1, "0"),
                    createMemberSpecification(1, "1"),
                ),
                methodSpec = listOf(
                    createMemberSpecification(1, "0"),
                    createMemberSpecification(1, "1"),
                )
            ),
        )
    }
}
