plugins {
    id("app.cash.licensee")
}

licensee {
    allow("MIT")
    allowUrl("http://www.opensource.org/licenses/mit-license.php")
    allowUrl("https://opensource.org/licenses/MIT")
    allowUrl("http://opensource.org/licenses/MIT")

    allow("Apache-2.0")

    allowUrl("https://www.gnu.org/software/classpath/license.html")
    allowUrl("http://www.gnu.org/software/classpath/license.html")

    allowUrl("https://www.gnu.org/licenses/gpl-2.0.txt")

    allowUrl("https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html")

    allowUrl("https://jgrapht.org/LGPL.html")
    allowUrl("http://jgrapht.org/LGPL.html")
    allowUrl("http://jgrapht.sourceforge.net/LGPL.html")

    // sentry
    allowUrl("http://opensource.org/licenses/BSD-3-Clause")

    allow("EPL-1.0")
    allowUrl("http://www.eclipse.org/legal/epl-v20.html")

    allow("CC0-1.0")
    allowUrl("https://creativecommons.org/licenses/publicdomain/")
    allowUrl("http://creativecommons.org/licenses/publicdomain/")

    allowDependency("me.weishu", "free_reflection", "3.0.1") {
        because(
            "MIT, but not posted correctly: " +
                "https://github.com/tiann/FreeReflection/blob/21797a4b689bbd98e80146240278509a35f3f0f3/LICENSE"
        )
    }

    allowDependency("org.mockito", "mockito-core", "2.23.0") {
        because(
            "MIT, but not posted correctly: " +
                "https://github.com/mockito/mockito/blob/main/LICENSE"
        )
    }

    allowDependency("com.nhaarman.mockitokotlin2", "mockito-kotlin", "2.2.0") {
        because(
            "MIT, but not posted correctly: " +
                "https://github.com/mockito/mockito/blob/main/LICENSE"
        )
    }

    allowDependency("org.jetbrains.teamcity", "teamcity-rest-client", "1.6.2") {
        because(
            "Apache 2.0, but not posted correctly: " +
                "https://github.com/JetBrains/teamcity-rest-client/blob/27410f13ef84fb722ec9c94500c4e0febe17b9ab/LICENSE.md"
        )
    }

    ignoreDependencies("org.hamcrest") {
        because("Scheduled to remove direct dependency MBS-6335")
    }

    allowDependency("net.sf.kxml", "kxml2", "2.3.0")
}
