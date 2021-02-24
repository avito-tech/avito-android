plugins.withId("kotlin") {

    @Suppress("UnstableApiUsage")
    extensions.getByType<JavaPluginExtension>().run {
        withSourcesJar()
        withJavadocJar()
    }
}
