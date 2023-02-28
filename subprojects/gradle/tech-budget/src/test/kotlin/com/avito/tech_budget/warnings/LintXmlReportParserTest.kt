package com.avito.tech_budget.warnings

import com.avito.android.tech_budget.internal.lint_issues.upload.LintXmlReportParser
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

class LintXmlReportParserTest {
    companion object {
        const val projectDir = "/Users/Shared/projects/avito"
    }

    @Nested
    inner class ParseIssues {
        @Test
        fun `when report is empty - then empty list`() {
            val emptyXml = createXmlReport(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <issues format="6" by="lint 7.2.2">
                
                </issues>
                """
            )

            val result = LintXmlReportParser(projectDir).parseXmlReport(listOf(emptyXml))
            assertThat(result).isEmpty()
        }

        @Test
        fun `when report has lint issues - then fill list`() {
            val xmlReport = createXmlReport(
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <issues format="6" by="lint 7.2.2">
                    <issue
                        id="DS_TCH_BDGT_ARCHITECTURE [Trivial]"
                        severity="Information"
                        message="Экран без архитектуры"
                        category="Correctness:DS_TECH_BUDGET">
                        <location
                            file="$projectDir/app/job/profile/src/main/java/work_profile/profile/WorkProfileFragment.kt"
                            line="19"
                            column="16"/>
                    </issue>
                    
                    <issue
                        id="DS_TCH_BDGT_ARCHITECTURE [Mvi]"
                        severity="Information"
                        message="Применяется архитектура MVI"
                        category="Correctness:DS_TECH_BUDGET">
                        <location
                            file="$projectDir/app/abuse/src/main/java/abuse/category/AbuseCategoryMviActivity.kt"
                            line="35"
                            column="7"/>
                    </issue>
                
                    <issue
                        id="DS_TCH_BDGT_ARCHITECTURE [Mvi]"
                        severity="Information"
                        message="Применяется архитектура MVI"
                        category="Correctness:DS_TECH_BUDGET">
                        <location
                            file="$projectDir/app/abuse/src/main/java/abuse/details/AbuseDetailsMviActivity.kt"
                            line="42"
                            column="7"/>
                    </issue>
                </issues>
                """
            )

            val result = LintXmlReportParser(projectDir).parseXmlReport(listOf(xmlReport))
            assertThat(result).hasSize(3)
        }

        @Test
        fun `when report has lint issues - then parse xml to model`() {
            val xmlReport = createXmlReport(
                """
                <issues format="6" by="lint 7.2.2">
                    <issue
                        id="DS_TCH_BDGT_ARCHITECTURE [Trivial]"
                        severity="Information"
                        message="Экран без архитектуры"
                        category="Correctness:DS_TECH_BUDGET"
                        priority="2">
                        <location
                            file="$projectDir/app/job/profile/src/main/java/work_profile/profile/WorkProfileFragment.kt"
                            line="19"
                            column="16"/>
                    </issue>
                </issues>
                """
            )

            val result = LintXmlReportParser(projectDir).parseXmlReport(listOf(xmlReport))
            assertThat(result).hasSize(1)

            val lintIssue = result.single()
            assertThat(lintIssue.ruleId).isEqualTo("DS_TCH_BDGT_ARCHITECTURE [Trivial]")
            assertThat(lintIssue.message).isEqualTo("Экран без архитектуры")
            assertThat(lintIssue.severity).isEqualTo("Information")
            assertThat(lintIssue.issueFileColumn).isEqualTo(16)
            assertThat(lintIssue.issueFileLine).isEqualTo(19)
            assertThat(lintIssue.issueFileName).isEqualTo(
                "/app/job/profile/src/main/java/work_profile/profile/WorkProfileFragment.kt"
            )
            assertThat(lintIssue.moduleName).isEqualTo(":app:job:profile")
        }

        @Test
        fun `when report has lint issues root - then parse xml to model`() {
            val xmlReport = createXmlReport(
                """
                <issues format="6" by="lint 7.2.2">
                    <issue
                        id="UnknownIssueId"
                        severity="Warning"
                        message="Unknown issue id"
                        category="Lint"
                        priority="1">
                        <location
                            file="/Users/Shared/projects/avito/common/printable-text/build.gradle"/>
                    </issue>
                </issues>
                """
            )

            val result = LintXmlReportParser(projectDir).parseXmlReport(listOf(xmlReport))
            assertThat(result).hasSize(1)

            val lintIssue = result.single()
            assertThat(lintIssue.ruleId).isEqualTo("UnknownIssueId")
            assertThat(lintIssue.message).isEqualTo("Unknown issue id")
            assertThat(lintIssue.severity).isEqualTo("Warning")
            assertThat(lintIssue.issueFileColumn).isNull()
            assertThat(lintIssue.issueFileLine).isNull()
            assertThat(lintIssue.issueFileName).isEqualTo("/common/printable-text/build.gradle")
            assertThat(lintIssue.moduleName).isEqualTo(":common:printable-text")
        }

        @Test
        fun `when report has same issues - then add only distinct`() {
            val xmlReport = createXmlReport(
                """
                <issues format="6" by="lint 7.2.2">
                    <issue
                        id="UnknownIssueId"
                        severity="Warning"
                        message="Unknown issue id"
                        category="Lint"
                        priority="1">
                        <location
                            file="/Users/Shared/projects/avito/common/printable-text/build.gradle"/>
                    </issue>
                    <issue
                        id="UnknownIssueId"
                        severity="Warning"
                        message="Unknown issue id"
                        category="Lint"
                        priority="1">
                        <location
                            file="/Users/Shared/projects/avito/common/printable-text/build.gradle"/>
                    </issue>
                </issues>
                """
            )

            val result = LintXmlReportParser(projectDir).parseXmlReport(listOf(xmlReport))
            assertThat(result).hasSize(1)
        }

        @Test
        fun `when several reports - then merge distinct issues`() {
            val xmlReport1 = createXmlReport(
                """
                <issues format="6" by="lint 7.2.2">
                    <issue
                        id="UnknownIssueId"
                        severity="Warning"
                        message="Unknown issue id"
                        category="Lint"
                        priority="1">
                        <location
                            file="/Users/Shared/projects/avito/common/printable-text/build.gradle"/>
                    </issue>
                </issues>
                """
            )
            val xmlReport2 = createXmlReport(
                """
                <issues format="6" by="lint 7.2.2">
                    <issue
                        id="DS_TCH_BDGT_ARCHITECTURE [Trivial]"
                        severity="Information"
                        message="Экран без архитектуры"
                        category="Correctness:DS_TECH_BUDGET"
                        priority="2">
                        <location
                            file="$projectDir/app/job/profile/src/main/java/work_profile/profile/WorkProfileFragment.kt"
                            line="19"
                            column="16"/>
                    </issue>
                    <issue
                        id="UnknownIssueId"
                        severity="Warning"
                        message="Unknown issue id"
                        category="Lint"
                        priority="1">
                        <location
                            file="/Users/Shared/projects/avito/common/printable-text/build.gradle"/>
                    </issue>
                </issues>
            """
            )

            val result = LintXmlReportParser(projectDir).parseXmlReport(listOf(xmlReport1, xmlReport2))
            assertThat(result).hasSize(2)
        }

        private fun createXmlReport(content: String): File {
            val tempFile = kotlin.io.path.createTempFile().toFile()
            tempFile.writeText(content.trimIndent())
            return tempFile
        }
    }

    @Nested
    inner class GetModuleName {
        private val projectDir = "/Users/Shared/projects/avito"

        @Test
        fun `when file inside of src - then detect module`() {
            val filePath =
                "/Users/Shared/projects/avito/avito-app/job/work-profile/" +
                    "src/main/java/com/avito/android/work_profile/profile/WorkProfileHostFragment.kt"
            val moduleName = LintXmlReportParser.getModuleNameByPath(filePath, projectDir)
            assertThat(moduleName).isEqualTo(":avito-app:job:work-profile")
        }

        @Test
        fun `when file in root - then detect module`() {
            val filePath = "/Users/Shared/projects/avito/common/printable-text/build.gradle"
            val moduleName = LintXmlReportParser.getModuleNameByPath(filePath, projectDir)
            assertThat(moduleName).isEqualTo(":common:printable-text")
        }

        @Test
        fun `when directory - then detect module`() {
            val filePath = "/Users/Shared/projects/avito/common/printable-text"
            val moduleName = LintXmlReportParser.getModuleNameByPath(filePath, projectDir)
            assertThat(moduleName).isEqualTo(":common:printable-text")
        }

        @Test
        fun `when directory with slash - then detect module`() {
            val filePath = "/Users/Shared/projects/avito/common/printable-text/"
            val moduleName = LintXmlReportParser.getModuleNameByPath(filePath, projectDir)
            assertThat(moduleName).isEqualTo(":common:printable-text")
        }

        @Test
        fun `when unknown structure - then detect as unknown`() {
            val filePath = projectDir
            val moduleName = LintXmlReportParser.getModuleNameByPath(filePath, projectDir)
            assertThat(moduleName).isEqualTo("unknown")
        }
    }
}
