package com.avito.report.serialize

import com.avito.report.model.Entry
import com.avito.report.model.FileAddress
import com.avito.report.model.Incident
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.Video
import com.avito.report.model.createStubInstance
import com.avito.truth.ResultSubject.Companion.assertThat
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class GsonReportSerializerTest {

    private val serializer = ReportSerializer()

    @Test
    fun `parse - failed with illegal state - empty file`(@TempDir tempDir: File) {
        val emptyFile = File(tempDir, "report.json").apply {
            createNewFile()
        }

        val result = serializer.deserialize(emptyFile)

        assertThat(result)
            .isFailure()
            .withThrowable {
                assertThat(it).hasMessageThat().contains("Report file is empty")
            }
    }

    @Test
    fun `serialize deserialize - success - with stub TestRuntimeDataPackage`(@TempDir tempDir: File) {
        val reportFile = File(tempDir, "report.json")

        serializer.serialize(
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(),
            reportFile = reportFile
        )

        val result = serializer.deserialize(reportFile)

        assertThat(result).isSuccess()
    }

    @Test
    fun `serialize deserialize  - success - TestRuntimeDataPackage with FileAddress in video`(@TempDir tempDir: File) {
        val reportFile = File(tempDir, "report.json")

        serializer.serialize(
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                video = Video(
                    fileAddress = FileAddress.File("someName")
                )
            ),
            reportFile = reportFile
        )

        val result = serializer.deserialize(reportFile)

        assertThat(result).isSuccess().withValue {
            val fileAddress = it.video?.fileAddress
            assertThat(fileAddress).isInstanceOf<FileAddress.File>()
            assertThat((fileAddress as? FileAddress.File)?.fileName).isEqualTo("someName")
        }
    }

    @Test
    fun `serialize deserialize  - success - TestRuntimeDataPackage with FileAddress in entry`(@TempDir tempDir: File) {
        val reportFile = File(tempDir, "report.json")

        serializer.serialize(
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                incident = Incident.createStubInstance(
                    entryList = listOf(
                        Entry.File.createStubInstance(
                            fileAddress = FileAddress.File(fileName = "someName"),
                            fileType = Entry.File.Type.img_png
                        )
                    )
                )
            ),
            reportFile = reportFile
        )

        val result = serializer.deserialize(reportFile)

        assertThat(result).isSuccess().withValue {
            val entry = it.incident?.entryList?.get(0)

            assertThat(entry).isNotNull()
            assertThat(entry!!.type).isEqualTo("img_png")

            val fileAddress = (entry as? Entry.File)?.fileAddress
            assertThat(fileAddress).isInstanceOf<FileAddress.File>()
            assertThat((fileAddress as? FileAddress.File)?.fileName).isEqualTo("someName")
        }
    }
}
