import java.util.jar.Attributes

plugins {
    id("kotlin")
    id("convention.kotlin-base")
}

tasks.withType(Jar::class.java).configureEach {
    manifest {
        attributes(
            mapOf(
                // To access a build version in runtime through class.java.`package`.implementationVersion
                Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version
            )
        )
    }
}
