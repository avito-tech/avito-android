plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

publish {
    artifactId.set("command-line-executor")
}

dependencies {
    implementation(libs.rxJava)
}
