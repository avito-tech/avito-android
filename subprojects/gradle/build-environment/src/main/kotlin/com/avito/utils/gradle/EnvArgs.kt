package com.avito.utils.gradle

import com.avito.kotlin.dsl.ProjectProperty
import com.avito.kotlin.dsl.PropertyScope.ROOT_PROJECT
import org.gradle.api.Project
import java.io.Serializable
import java.util.concurrent.TimeUnit

// Used in build.gradle's of avito
public val Project.envArgs: EnvArgs by ProjectProperty.lazy<EnvArgs>(scope = ROOT_PROJECT) { project ->
    EnvArgsImpl(project)
}

public interface EnvArgs {

    public sealed class Build : Serializable {
        /**
         * todo change to string after refactor [TestRunEnvironment.teamcityBuildId]
         */
        public abstract val id: Int
        public abstract val type: String

        /** True for local developer builds (not running on TeamCity). */
        public val isLocal: Boolean get() = this is Local

        /** True for builds in an isolated remote environment created on demand. */
        public val isSandbox: Boolean get() = this is Sandbox

        public data class Local(
            override val id: Int,
            override val type: String,
        ) : Build()

        public data class Teamcity(
            override val id: Int,
            public val url: String,
            public val number: String,
            override val type: String
        ) : Build()

        public data class Sandbox(
            override val id: Int,
            override val type: String,
        ) : Build()

        internal companion object {
            val generatedId: Int = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()
        }
    }

    public val build: Build
}
