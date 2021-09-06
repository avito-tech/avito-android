plugins.withId("kotlin") {

    extensions.getByType<JavaPluginExtension>().run {
        withSourcesJar()
        withJavadocJar()
    }
}
