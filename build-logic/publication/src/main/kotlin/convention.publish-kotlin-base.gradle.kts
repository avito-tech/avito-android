plugins.withId("kotlin") {
    extensions.getByType<JavaPluginExtension>().run {

        @Suppress("UnstableApiUsage")
        withSourcesJar()
    }
}
