package com.avito.cd

public class CdBuildConfig {

    public val project: NupokatiProject
        get() = throw UnsupportedOperationException("This code replaces by NupokatiPlugin internals")
    public val deployments: List<Deployment>
        get() = throw UnsupportedOperationException("This code replaces by NupokatiPlugin internals")
    public val releaseVersion: String
        get() = throw UnsupportedOperationException("Use nupokati.releaseVersion()")

    public object NupokatiProject {
        public const val id: String = "stub-app"
    }

    public sealed class Deployment {

        public data class Qapps(
            val isRelease: Boolean
        ) : Deployment()
    }
}
