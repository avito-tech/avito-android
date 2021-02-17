package com.avito.android.publish

abstract class KotlinLibraryPublishExtension {

    /**
     * non blank value will modify artifact id of maven coordinates
     * default is project.name
     */
    var artifactId: String = ""
}
