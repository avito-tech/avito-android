package com.avito.android.http.nupokati

import com.avito.android.Result
import com.avito.android.http.nupokati.model.ArtifactUploadResult
import com.avito.android.http.nupokati.model.TestResultSaveResult
import com.avito.reportviewer.model.ReportCoordinates
import java.nio.file.Path

public interface NupokatiV4Client {

    /**
     * Upload artifact to Nupokati
     *
     * @param project Package name / project identifier
     * @param platform Platform name ("android")
     * @param version Version number
     * @param buildNumber Build number
     * @param file Artifact file to upload
     * @return Upload result
     */
    public fun uploadArtifact(
        project: String,
        platform: String,
        version: String,
        buildNumber: Int,
        storeName: String? = null,
        file: Path,
    ): Result<ArtifactUploadResult>

    /**
     * Save test results to Nupokati
     *
     * @param project Package name / project identifier
     * @param platform Platform name ("android")
     * @param version Version number
     * @param buildNumber Build number
     * @param reportUrl URL to the test report
     * @param reportCoordinates Test report coordinates
     * @return Save operation result
     */
    public fun saveTestResult(
        project: String,
        platform: String,
        version: String,
        buildNumber: Int,
        reportUrl: String,
        reportCoordinates: ReportCoordinates,
    ): Result<TestResultSaveResult>
}
