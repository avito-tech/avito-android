package com.avito.android.model

public data class CdBuildConfig(
    val schemaVersion: Long,
    val project: NupokatiProject,
    val outputDescriptor: OutputDescriptor,
    val releaseVersion: String,
    val deployments: List<Deployment>
) {

    public data class OutputDescriptor(
        val path: String,
        val skipUpload: Boolean
    )

    public sealed class Deployment {

        public data class GooglePlay(
            val artifactType: AndroidArtifactType,
            val buildVariant: String,
            val track: Track
        ) : Deployment()

        /**
         * Deploy artifacts to QApps.
         * Uses UploadToQapps build step to find them.
         */
        public data class Qapps(
            /**
             * Send artifacts as release versions.
             * Non-release artifacts are stored for a limited time.
             */
            val isRelease: Boolean
        ) : Deployment()

        public data class Unknown(val type: String) : Deployment()

        public enum class Track {
            alpha,
            internal,
            beta,
            production
        }
    }
}
