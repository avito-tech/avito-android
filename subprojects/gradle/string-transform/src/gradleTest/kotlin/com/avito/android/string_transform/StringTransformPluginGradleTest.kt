package com.avito.android.string_transform

import com.android.aapt.Resources
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

internal class StringTransformPluginGradleTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setUp(@TempDir dir: File) {
        projectDir = dir
    }

    @Test
    fun `plugin root task - succeeds without outputs - when no pipelines are declared`() {
        givenProject(
            """
            transformStrings {
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val outputsDir = File(
            projectDir,
            "app/build/outputs/transformStrings"
        )
        assertThat(outputsDir.exists()).isFalse()
    }

    @Test
    fun `plugin apk transform - rewrites content and renames path - when exact and case-expanded rules are declared`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                        caseExpanded(
                            "markpart",
                            "nextpart",
                            com.avito.android.string_transform.GeneratedForm.LOWER,
                        )
                    }
                }
            }
            """.trimIndent()
        ) { _ ->
            val assetDir = resolve("src/main/assets/markpart")
            assetDir.mkdirs()
            assetDir.resolve("payload.txt").writeText("samplevalue markpart")
        }

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )
        val outputApk = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/apk/transformed-unsigned.apk"
        )

        assertThat(reportFile.exists()).isTrue()
        assertThat(outputApk.exists()).isTrue()
        assertApkSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 2,
            exactDeclarations = 1,
            caseExpandedDeclarations = 1,
        )
        assertThat(apkHasEntry(outputApk, "assets/nextpart/payload.txt")).isTrue()
        assertThat(apkHasEntry(outputApk, "assets/markpart/payload.txt")).isFalse()
        assertThat(readApkEntry(outputApk, "assets/nextpart/payload.txt")).isEqualTo("changedvalue nextpart")
    }

    @Test
    fun `plugin tasks - register variant task and report - when exact flavored variant matches`() {
        givenProject(
            """
            android {
                flavorDimensions += "tier"
                productFlavors {
                    create("free") {
                        dimension = "tier"
                    }
                    create("paid") {
                        dimension = "tier"
                    }
                }
            }

            transformStrings {
                create("alpha") {
                    variant("paidRelease")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        ) { _ ->
            resolve("src/main/assets").mkdirs()
            resolve("src/main/assets/samplevalue.txt").writeText("samplevalue")

            resolve("src/main/res/raw").mkdirs()
            resolve("src/main/res/raw/samplevalue.xml").writeText("samplevalue")

            resolve("src/main/res/xml").mkdirs()
            resolve("src/main/res/xml/samplevalue_config.xml").writeText(
                """
                <samplevalueNode>samplevalue</samplevalueNode>
                """.trimIndent()
            )

            resolve("src/main/res/values").mkdirs()
            resolve("src/main/res/values/branding.xml").writeText(
                """
                <resources>
                    <string name="samplevalue_title">samplevalue</string>
                </resources>
                """.trimIndent()
            )

            resolve("src/main/java/com/example/samplevalue").mkdirs()
            resolve("src/main/java/com/example/samplevalue/Holder.java").writeText(
                """
                package com.example.samplevalue;

                public class Holder {
                    public static final String samplevalueField = "samplevalue";

                    public static void samplevalueMethod(String samplevalueParam) {
                        String samplevalueLocal = samplevalueField + samplevalueParam;
                    }
                }
                """.trimIndent()
            )
        }

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val apkReportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/paidRelease/report/transform-report.json"
        )
        val aabReportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/paidRelease/aab/report/transform-report.json"
        )
        val outputAab = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/paidRelease/aab/transformed-unsigned.aab"
        )
        assertThat(apkReportFile.exists()).isTrue()
        assertThat(aabReportFile.exists()).isTrue()
        assertThat(outputAab.exists()).isTrue()
        assertApkSuccessfulReportText(
            report = apkReportFile.readText(),
            pipeline = "alpha",
            variant = "paidRelease",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
        assertAabSuccessfulReportText(
            report = aabReportFile.readText(),
            pipeline = "alpha",
            variant = "paidRelease",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )

        ZipFile(outputAab).use { zip ->
            assertThat(zip.getEntry("base/assets/changedvalue.txt")).isNotNull()
            assertThat(zip.getEntry("base/assets/samplevalue.txt")).isNull()
            assertThat(zip.readEntryText("base/assets/changedvalue.txt")).isEqualTo("changedvalue")

            assertThat(zip.getEntry("base/res/raw/changedvalue.xml")).isNotNull()
            assertThat(zip.getEntry("base/res/raw/samplevalue.xml")).isNull()
            assertThat(zip.readEntryText("base/res/raw/changedvalue.xml")).isEqualTo("changedvalue")

            assertThat(zip.getEntry("base/res/xml/changedvalue_config.xml")).isNotNull()
            assertThat(zip.getEntry("base/res/xml/samplevalue_config.xml")).isNull()

            val resourcesPb = Resources.ResourceTable.parseFrom(
                zip.readEntryBytes("base/resources.pb")
            )
            assertThat(resourcesPb.asTextFormat()).contains("changedvalue_title")
            assertThat(resourcesPb.asTextFormat()).contains("changedvalue")
            assertThat(resourcesPb.asTextFormat()).doesNotContain("samplevalue_title")

            val compiledXml = Resources.XmlNode.parseFrom(
                zip.readEntryBytes("base/res/xml/changedvalue_config.xml")
            )
            assertThat(compiledXml.asTextFormat()).contains("changedvalue")
            assertThat(compiledXml.asTextFormat()).doesNotContain("samplevalue")

            val dexFile = readDexFile(
                bytes = zip.readEntryBytes("base/dex/classes.dex"),
                tempDirectory = projectDir.resolve("build/aab-dex-inspection"),
                name = "classes.dex",
            )
            val holderClass = dexFile.classes.single { it.type == "Lcom/example/changedvalue/Holder;" }

            assertThat(holderClass.fields.single().name).isEqualTo("changedvalueField")
            assertThat(holderClass.methods.any { it.name == "changedvalueMethod" }).isTrue()
        }
    }

    @Test
    fun `plugin mapping transform - publishes transformed mapping - when release variant produces original mapping`() {
        givenProject(
            """
            android {
                buildTypes {
                    release {
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard.pro"
                        )
                    }
                }
            }

            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        ) { _ ->
            resolve("src/main/java/com/example/samplevalue").mkdirs()
            resolve("src/main/java/com/example/samplevalue/Holder.java").writeText(
                """
                package com.example.samplevalue;

                public class Holder {
                    public static final String samplevalueField = "samplevalue";
                }
                """.trimIndent()
            )
        }

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/mapping/report/transform-report.json"
        )
        val outputMapping = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/mapping/transformed-mapping.txt"
        )

        assertThat(reportFile.exists()).isTrue()
        assertThat(outputMapping.exists()).isTrue()
        assertThat(outputMapping.readText()).contains("changedvalue")
        assertThat(outputMapping.readText()).doesNotContain("samplevalue")
        assertMappingSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
    }

    @Test
    fun `plugin mapping transform - does not publish mapping output - when release variant has no original mapping`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/mapping/report/transform-report.json"
        )
        val outputMapping = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/mapping/transformed-mapping.txt"
        )

        assertThat(reportFile.exists()).isFalse()
        assertThat(outputMapping.exists()).isFalse()
    }

    @Test
    fun `plugin apk transform - renames path - when exact rule matches file name`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        ) { _ ->
            val assetsDir = resolve("src/main/assets")
            assetsDir.mkdirs()
            assetsDir.resolve("samplevalue.txt").writeText("samplevalue")
        }

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )
        val outputApk = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/apk/transformed-unsigned.apk"
        )

        assertThat(reportFile.exists()).isTrue()
        assertThat(outputApk.exists()).isTrue()
        assertApkSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
        assertThat(apkHasEntry(outputApk, "assets/samplevalue.txt")).isFalse()
        assertThat(apkHasEntry(outputApk, "assets/changedvalue.txt")).isTrue()
        assertThat(readApkEntry(outputApk, "assets/changedvalue.txt")).isEqualTo("changedvalue")
    }

    @Test
    fun `plugin report - writes rule summary - when exact and case-expanded rules are declared`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                        caseExpanded(
                            "token",
                            "value",
                            com.avito.android.string_transform.GeneratedForm.LOWER,
                            com.avito.android.string_transform.GeneratedForm.UPPER,
                            com.avito.android.string_transform.GeneratedForm.UPPER_FIRST,
                        )
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )

        assertThat(reportFile.exists()).isTrue()
        assertApkSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 4,
            exactDeclarations = 1,
            caseExpandedDeclarations = 1,
        )
    }

    @Test
    fun `plugin report - uses final pipeline configuration - when rules are added after create call`() {
        givenProject(
            """
            transformStrings {
                val pipeline = create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
                pipeline.rules {
                    caseExpanded(
                        "token",
                        "value",
                        com.avito.android.string_transform.GeneratedForm.UPPER_FIRST,
                        com.avito.android.string_transform.GeneratedForm.LOWER,
                    )
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )

        assertThat(reportFile.exists()).isTrue()
        assertApkSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 3,
            exactDeclarations = 1,
            caseExpandedDeclarations = 1,
        )
    }

    @Test
    fun `plugin tasks - use final pipeline configuration - when variant is set after create call`() {
        givenProject(
            """
            transformStrings {
                val pipeline = create("alpha") {
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
                pipeline.variant("release")
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )

        assertThat(reportFile.exists()).isTrue()
        assertApkSuccessfulReportText(
            report = reportFile.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
    }

    @Test
    fun `plugin root task - executes all matched variant tasks - when multiple pipelines target the same variant`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
                create("beta") {
                    variant("release")
                    rules {
                        caseExpanded(
                            "token",
                            "value",
                            com.avito.android.string_transform.GeneratedForm.LOWER,
                        )
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val alphaReport = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )
        val betaReport = File(
            projectDir,
            "app/build/outputs/transformStrings/beta/release/report/transform-report.json"
        )

        assertThat(alphaReport.exists()).isTrue()
        assertThat(betaReport.exists()).isTrue()
        assertApkSuccessfulReportText(
            report = alphaReport.readText(),
            pipeline = "alpha",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 1,
            caseExpandedDeclarations = 0,
        )
        assertApkSuccessfulReportText(
            report = betaReport.readText(),
            pipeline = "beta",
            variant = "release",
            totalRules = 1,
            exactDeclarations = 0,
            caseExpandedDeclarations = 1,
        )
    }

    @Test
    fun `plugin configuration cache - reuses entry - when configuration is unchanged`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            configurationCache = true,
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        gradlew(
            projectDir,
            ":app:transformStrings",
            configurationCache = true,
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful().configurationCachedReused()
    }

    @Test
    fun `plugin registration - fails build - when pipeline variant is not configured`() {
        givenProject(
            """
            transformStrings {
                create("invalid") {
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:help",
            expectFailure = true,
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("String-transform pipeline 'invalid' does not declare variant")
    }

    @Test
    fun `plugin registration - fails build - when rule declaration is malformed`() {
        givenProject(
            """
            transformStrings {
                create("invalid") {
                    variant("release")
                    rules {
                        exact("", "target")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:help",
            expectFailure = true,
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("transformStrings rule 'from' value must not be empty")
    }

    @Test
    fun `plugin registration - fails build - when case-expanded rule has no generated forms`() {
        givenProject(
            """
            transformStrings {
                create("invalid") {
                    variant("release")
                    rules {
                        caseExpanded("source", "target")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:help",
            expectFailure = true,
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("transformStrings caseExpanded rule must declare at least one generated form")
    }

    // This permissive behavior is accepted only for v1
    // TODO (rm): introduce explicit validation and a clear error later.
    @Test
    fun `plugin registration - allows pipeline name - when pipeline name contains spaces`() {
        givenProject(
            """
            transformStrings {
                create("bad name") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()
    }

    // This permissive behavior is accepted only for v1
    // TODO (rm): introduce explicit validation and a clear error later.
    @Test
    fun `plugin tasks - do not register variant task - when exact variant name is unknown`() {
        givenProject(
            """
            transformStrings {
                create("missing") {
                    variant("missingRelease")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:tasks",
            "--all",
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).doesNotContain("transformStringsMissingMissingRelease")
    }

    @Test
    fun `plugin tasks - do not register unrelated variant task - when only release variant is selected`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:tasks",
            "--all",
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("transformStringsAlphaRelease")
        assertThat(output).contains("transformStringsAlphaReleaseBundle")
        assertThat(output).doesNotContain("transformStringsAlphaDebug")
        assertThat(output).doesNotContain("transformStringsAlphaDebugBundle")
    }

    @Test
    fun `plugin tasks - register androidTest variant task - when debug variant exposes androidTest component`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("debug")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        ) { _ ->
            val androidTestSourcesDir = resolve("src/androidTest/java/com/example")
            androidTestSourcesDir.mkdirs()
            androidTestSourcesDir.resolve("SamplePlaceholderTest.java").writeText(
                """
                package com.example;

                public class SamplePlaceholderTest {
                }
                """.trimIndent()
            )
        }

        val output = gradlew(
            projectDir,
            ":app:tasks",
            "--all",
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("transformStringsAlphaDebug")
        assertThat(output).contains("transformStringsAlphaDebugBundle")
        assertThat(output).contains("transformStringsAlphaDebugAndroidTest")
    }

    @Test
    fun `plugin tasks - do not register androidTest variant task - when release variant has no androidTest`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        val output = gradlew(
            projectDir,
            ":app:tasks",
            "--all",
            useTestFixturesClasspath = true,
        ).output

        assertThat(output).contains("transformStringsAlphaRelease")
        assertThat(output).doesNotContain("transformStringsAlphaReleaseAndroidTest")
    }

    @Test
    fun `plugin root task - succeeds without report - when exact variant name is unknown`() {
        givenProject(
            """
            transformStrings {
                create("missing") {
                    variant("missingRelease")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        )

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/missing/missingRelease/report/transform-report.json"
        )
        assertThat(reportFile.exists()).isFalse()
    }

    @Test
    fun `plugin warnings - print warning and record diagnostic - when matched pipeline contains overlapping rules`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("token", "value")
                        exact("tok", "prefix")
                    }
                }
            }
            """.trimIndent()
        )

        val result = gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        )

        assertThat(result.output).contains("overlap and remain order-sensitive")
        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )
        assertReportJsonMatches(
            report = reportFile.readText(),
            expectedRegex = literalJsonPattern("""
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 2,
                    "declarationCounts": {
                        "exact": 2,
                        "caseExpanded": 0
                    }
                },
                "phases": [
                    {
                        "name": "variant-apk-outputs-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-resolution",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "variant-apk-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-unpack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "arsc-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "binary-axml-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "dex-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "kotlin-module-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "residual-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "rename",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "metadata-cleanup",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-repack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "output-publication",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "WARNING",
                        "message": "Rules 'token' and 'tok' overlap and remain order-sensitive.",
                        "affectedPhase": null,
                        "affectedPath": null
                    }
                ]
            }
            """),
        )
    }

    @Test
    fun `plugin warnings - record diagnostic - when unsupported so file matches replacement literal`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        ) { _ ->
            val nativeLibDir = resolve("src/main/jniLibs/arm64-v8a")
            nativeLibDir.mkdirs()
            nativeLibDir.resolve("libnative.so").writeBytes("prefix samplevalue suffix".toByteArray())
        }

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )
        assertReportJsonMatches(
            report = reportFile.readText(),
            expectedRegex = literalJsonPattern("""
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 1,
                    "declarationCounts": {
                        "exact": 1,
                        "caseExpanded": 0
                    }
                },
                "phases": [
                    {
                        "name": "variant-apk-outputs-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-resolution",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "variant-apk-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-unpack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "arsc-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "binary-axml-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "dex-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "kotlin-module-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "residual-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "rename",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "metadata-cleanup",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-repack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "output-publication",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "WARNING",
                        "message": "Skipped unsupported binary file during content transform because '.so' files are not supported for content transform yet. Matched literals: samplevalue",
                        "affectedPhase": "residual-text-transform",
                        "affectedPath": "lib/arm64-v8a/libnative.so"
                    }
                ]
            }
            """),
        )
    }

    @Test
    fun `plugin apk transform - preserves STORED compression for native library - when useLegacyPackaging is false`() {
        givenProject(
            """
            android {
                packaging {
                    jniLibs {
                        useLegacyPackaging = false
                    }
                }
            }

            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("samplevalue", "changedvalue")
                    }
                }
            }
            """.trimIndent()
        ) { _ ->
            val nativeLibDir = resolve("src/main/jniLibs/arm64-v8a")
            nativeLibDir.mkdirs()
            nativeLibDir.resolve("libnative.so").writeBytes(ByteArray(1024) { it.toByte() })
        }

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        // Precondition: verify input APK has .so as STORED
        val inputApkDir = File(projectDir, "app/build/outputs/apk/release")
        val inputApk = inputApkDir.listFiles()?.singleOrNull { it.extension == "apk" }
        assertThat(inputApk).isNotNull()
        ZipFile(inputApk!!).use { zip ->
            val soEntry = zip.getEntry("lib/arm64-v8a/libnative.so")
            assertThat(soEntry).isNotNull()
            assertThat(soEntry.method).isEqualTo(ZipEntry.STORED)
        }

        // Verify: output APK preserves STORED compression for .so
        val outputApk = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/apk/transformed-unsigned.apk"
        )
        assertThat(outputApk.exists()).isTrue()
        ZipFile(outputApk).use { zip ->
            val soEntry = zip.getEntry("lib/arm64-v8a/libnative.so")
            assertThat(soEntry).isNotNull()
            assertThat(soEntry.method).isEqualTo(ZipEntry.STORED)
        }
    }

    @Test
    fun `plugin warnings - record diagnostic - when rename rule target contains unsupported filename characters`() {
        givenProject(
            """
            transformStrings {
                create("alpha") {
                    variant("release")
                    rules {
                        exact("markpart", "nextpart/name")
                    }
                }
            }
            """.trimIndent()
        ) { _ ->
            val assetsDir = resolve("src/main/assets")
            assetsDir.mkdirs()
            assetsDir.resolve("payload.txt").writeText("unchanged")
        }

        gradlew(
            projectDir,
            ":app:transformStrings",
            useTestFixturesClasspath = true,
        ).assertThat().buildSuccessful()

        val reportFile = File(
            projectDir,
            "app/build/outputs/transformStrings/alpha/release/report/transform-report.json"
        )
        assertReportJsonMatches(
            report = reportFile.readText(),
            expectedRegex = literalJsonPattern("""
            {
                "pipeline": "alpha",
                "variant": "release",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "release"
                },
                "rules": {
                    "totalRules": 1,
                    "declarationCounts": {
                        "exact": 1,
                        "caseExpanded": 0
                    }
                },
                "phases": [
                    {
                        "name": "variant-apk-outputs-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-resolution",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "variant-apk-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-unpack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "arsc-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "binary-axml-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "dex-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "kotlin-module-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "residual-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "rename",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "metadata-cleanup",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-repack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "output-publication",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [
                    {
                        "severity": "WARNING",
                        "message": "Rename rule 'markpart' -> 'nextpart/name' is ignored for paths because target contains unsupported filename characters.",
                        "affectedPhase": "rename",
                        "affectedPath": null
                    }
                ]
            }
            """),
        )
    }

    private fun givenProject(
        transformConfiguration: String,
        appMutator: File.(AndroidAppModule) -> Unit = {},
    ) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.string-transform")
                    },
                    enableKotlinAndroidPlugin = false,
                    buildGradleExtra = transformConfiguration,
                    useKts = true,
                    mutator = appMutator,
                )
            ),
            useKts = true,
        ).generateIn(projectDir)
    }

    private fun assertApkSuccessfulReportText(
        report: String,
        pipeline: String,
        variant: String,
        totalRules: Int,
        exactDeclarations: Int,
        caseExpandedDeclarations: Int,
    ) {
        assertReportJsonMatches(
            report = report,
            expectedRegex = literalJsonPattern("""
            {
                "pipeline": "$pipeline",
                "variant": "$variant",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "$variant"
                },
                "rules": {
                    "totalRules": $totalRules,
                    "declarationCounts": {
                        "exact": $exactDeclarations,
                        "caseExpanded": $caseExpandedDeclarations
                    }
                },
                "phases": [
                    {
                        "name": "variant-apk-outputs-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-resolution",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "variant-apk-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-apk-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-unpack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "arsc-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "binary-axml-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "dex-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "kotlin-module-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "residual-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "rename",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "metadata-cleanup",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "apk-repack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "output-publication",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [\E\s*\Q]
            }
            """),
        )
    }

    private fun assertAabSuccessfulReportText(
        report: String,
        pipeline: String,
        variant: String,
        totalRules: Int,
        exactDeclarations: Int,
        caseExpandedDeclarations: Int,
    ) {
        assertReportJsonMatches(
            report = report,
            expectedRegex = literalJsonPattern("""
            {
                "pipeline": "$pipeline",
                "variant": "$variant",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "$variant"
                },
                "rules": {
                    "totalRules": $totalRules,
                    "declarationCounts": {
                        "exact": $exactDeclarations,
                        "caseExpanded": $caseExpandedDeclarations
                    }
                },
                "phases": [
                    {
                        "name": "variant-aab-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-aab-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "bundle-unpack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "resources-pb-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "protobuf-xml-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "dex-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "kotlin-module-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "residual-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "rename",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "metadata-cleanup",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "bundle-repack",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "output-publication",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [\E\s*\Q]
            }
            """),
        )
    }

    private fun assertMappingSuccessfulReportText(
        report: String,
        pipeline: String,
        variant: String,
        totalRules: Int,
        exactDeclarations: Int,
        caseExpandedDeclarations: Int,
    ) {
        assertReportJsonMatches(
            report = report,
            expectedRegex = literalJsonPattern("""
            {
                "pipeline": "$pipeline",
                "variant": "$variant",
                "artifact": {
                    "moduleIdentity": ":app",
                    "variantIdentity": "$variant"
                },
                "rules": {
                    "totalRules": $totalRules,
                    "declarationCounts": {
                        "exact": $exactDeclarations,
                        "caseExpanded": $caseExpandedDeclarations
                    }
                },
                "phases": [
                    {
                        "name": "variant-mapping-artifact-observation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "input-mapping-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "mapping-text-transform",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "mapping-structural-sanity-validation",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    },
                    {
                        "name": "output-publication",
                        "status": "SUCCESS",
                        "durationMillis": \E\d+\Q
                    }
                ],
                "diagnostics": [\E\s*\Q]
            }
            """),
        )
    }

    private fun assertReportJsonMatches(report: String, expectedRegex: String) {
        assertThat(report.trim()).containsMatch(expectedRegex.trimIndent())
    }

    private fun literalJsonPattern(jsonBody: String): String {
        return "(?s)^\\Q${jsonBody.trimIndent()}\\E$"
    }

    private fun apkHasEntry(apkFile: File, path: String): Boolean {
        ZipFile(apkFile).use { zip ->
            return zip.getEntry(path) != null
        }
    }

    private fun readApkEntry(apkFile: File, path: String): String {
        ZipFile(apkFile).use { zip ->
            val entry = checkNotNull(zip.getEntry(path)) {
                "APK entry not found: $path"
            }
            return zip.getInputStream(entry)
                .bufferedReader(StandardCharsets.UTF_8)
                .use { it.readText() }
        }
    }
}
