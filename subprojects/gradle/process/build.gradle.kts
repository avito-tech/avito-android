plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.funktionaleTry)

    //todo жирная зависимость ради единственного Commandline.translateCommandline(source)
    implementation(gradleApi())
}
