package com.avito.cd

public data class CdBuildConfig(
    val project: NupokatiProject,
    val deployments: List<Deployment>
) {

    public object NupokatiProject {
        public const val id: String = "stub-app"
    }

    public sealed class Deployment {

        public data class Qapps(
            val isRelease: Boolean
        ) : Deployment()
    }
}
