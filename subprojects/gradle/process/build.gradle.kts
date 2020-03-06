plugins {
    id("kotlin")
    id("java-test-fixtures")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    //todo жирная зависимость ради единственного Commandline.translateCommandline(source)
    implementation(gradleApi())

    implementation(Dependencies.funktionaleTry)

    testFixturesImplementation(Dependencies.kotlinStdlib)
    testFixturesImplementation(Dependencies.funktionaleTry)
}
