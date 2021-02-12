plugins {
    id("com.avito.android.libraries")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    @Suppress("MagicNumber")
    maxParallelForks = 8

    failFast = true

    /**
     * fix for retrofit `WARNING: Illegal reflective access by retrofit2.Platform`
     * see square/retrofit/issues/3341
     */
    jvmArgs = listOf("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")

    systemProperty("rootDir", "${project.rootDir}")

    val testProperties = listOf(
        "avito.kubernetes.url",
        "avito.kubernetes.token",
        "avito.kubernetes.cert",
        "avito.kubernetes.namespace",
        "avito.slack.test.channel",
        "avito.slack.test.token",
        "avito.slack.test.workspace",
        "avito.elastic.endpoint",
        "avito.elastic.indexpattern",
        "teamcityBuildId"
    )
    testProperties.forEach { key ->
        val property = if (project.hasProperty(key)) {
            project.property(key)!!.toString()
        } else {
            ""
        }
        systemProperty(key, property)
    }
}

dependencies {
    add("testImplementation", libs.junitJupiterApi)
    add("testImplementation", libs.truth)

    add("testRuntimeOnly", libs.junitJupiterEngine)
    add("testRuntimeOnly", libs.junitPlatformRunner)
    add("testRuntimeOnly", libs.junitPlatformLauncher)

    if (project.name != "truth-extensions") {
        add("testImplementation", project(":subprojects:common:truth-extensions"))
    }
}
