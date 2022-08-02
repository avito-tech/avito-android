package com.avito.android.model.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CdBuildConfigV2(
    @SerialName("schema_version") override val schemaVersion: Long,
    val project: NupokatiProject,
    @SerialName("output_descriptor") override val outputDescriptor: OutputDescriptor,
    @SerialName("release_version") override val releaseVersion: String,
    val deployments: List<Deployment>
) : CdBuildConfig {

    @Serializable
    public sealed class Deployment {

        @Serializable
        @SerialName("google-play")
        public data class GooglePlay(
            @SerialName("artifact_type") val artifactType: AndroidArtifactType,
            // TODO: decouple logic and move the mapping on client side MBSA-636
            // Contract should show a declarative intention, but build variants are more implementation details.
            // They tend to change and it's no use to couple Nupokati and apps in this point.
            @Deprecated("Will be deleted. Try to avoid usage if possible.")
            @SerialName("build_variant") val buildVariant: String,
            val track: Track
        ) : Deployment()

        @Serializable
        @SerialName("ru-store")
        public data class RuStore(
            @SerialName("artifact_type") val artifactType: AndroidArtifactType,
        ) : Deployment()

        /**
         * Deploy artifacts to QApps.
         */
        @Serializable
        @SerialName("qapps")
        public data class Qapps(
            /**
             * Send artifacts as release versions.
             * Non-release artifacts are stored for a limited time.
             */
            @SerialName("is_release") val isRelease: Boolean
        ) : Deployment()

        public enum class Track {
            alpha,
            internal,
            beta,
            production
        }
    }
}
