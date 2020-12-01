---
title: IDE Speedup
type: docs
---

# IDE Speedup

To increase IDE performance you need to disable all unnecessary stuff and tune some settings.

## Increase memory heap

[Increasing memory heap](https://www.jetbrains.com/help/idea/increasing-memory-heap.html)

## Enable remote build

[Удаленная сборка (Mirakle)]({{< ref "/docs/assemble/RemoteBuild.md" >}})

## Disable unnecessary modules

{{<avito section>}}

We develop all our Android applications in a single [monorepo](https://en.wikipedia.org/wiki/Monorepo). If you work only with some apps you can disable others.
To do so, disable relevant [Gradle properties on a user level](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) 
in `~/.gradle/gradle.properties`:

```properties
syncAvito=false
sync<app module>
```

These flags are used in a simple way in `settings.gradle`:

```groovy
if (syncAvito.toBoolean()) {
    include(":avito")
    ...
} else {
    logger.lifecycle("Avito modules are disabled")
}
```

Also, you can disable specific modules manually in IDE: [Unloading modules](https://www.jetbrains.com/help/idea/unloading-modules.html)

## Disable extra work on Gradle sync

- **Preferences > Experimental > Only sync the active variant**
- **Preferences > Experimental > Do not build Gradle task list during Gradle sync**

## Reuse Gradle daemons

Gradle can run [multiple daemons](https://docs.gradle.org/5.0/userguide/gradle_daemon.html#sec:why_is_there_more_than_one_daemon_process_on_my_machine)
if there are no compatible ones. You can see current Gradle daemons by this command: 

```bash
./gradlew --status
```

To make Gradle daemons more reusable, you need to reuse the same JDK for all Gradle projects:

1. Make sure you use an embedded JDK in Android Studio: [](https://developer.android.com/studio/intro/studio-config#jdk)
1. Specify this JDK in [Gradle properties on a user level](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties) 
in `~/.gradle/gradle.properties`:

```properties
org.gradle.java.home=<path to the embedded JDK>
```

## Disable unneeded plugins

Android Studio comes with a lot of plugins. Ideally, they shouldn't affect the performance if you don't use them.
To be sure and to avoid any possible overhead it's better to disable that you don't use. 
These are only the possible options:

- Android APK Support
- Android Games
- Android NDK
- App Links
- Assistant
- CVS, hg4idea, Subversion integration
- Firebase ...
- GitHub
- Google ...
- Task management
- Terminal
- Test recorder, TestNG
- YAML

## Exclude project from indexing and antiviruses

Gradle generates many files during a build. It's extra work for antiviruses and any search indexing.\
Exclude these directories:

- The project directory
- Android SDK
- Android Studio
- ~/.gradle
- ~/.android
- ~/.m2
- ~/.gradle-profiler
- ~/gradle-profiler

{{< tabs "indexing" >}}
{{< tab "macOS" >}}

Add an exclusion to [**System preferences > Spotlight > Privacy**](https://support.apple.com/guide/mac-help/change-spotlight-preferences-mchlp2811/mac).

tip: press _Cmd + Shift + **.**_ to see hidden files in Finder

{{< /tab >}}
{{< tab "Windows" >}}

[Add an exclusion to Windows Security](https://support.microsoft.com/en-us/help/4028485/windows-10-add-an-exclusion-to-windows-security)

{{< /tab >}}
{{< /tabs >}}

## Ignore unneeded files

You can hide and ignore generated files from the Editor. 
We assume that any little bit can help IDE.
 
**Preferences > Editor > File Types > Ignore Files and Folders**

- `.gradle` 
- `build`

{{< hint warning>}}
Some intermediate build files are still needed for development.

- `generated/source` contains `BuildConfig`.
- `apt`, `kapt`, `kaptKotlin` contains files from annotation processors.

They will be unreachable in an editor and can break a refactoring.
{{< /hint >}}

## Free more memory

Java are greedy for RAM. The worst case scenario is [memory swapping](https://en.wikipedia.org/wiki/Paging#Implementations).\
Check it out, what applications can be shrinked.

## Enable power save mode

The last resort is a power save mode. It disables code inspections and syntax highlighting in a current file.\
Use **File > Power Save Mode** or find an icon ![](https://resources.jetbrains.com/help/img/idea/2020.1/icons.ide.hectorOn@2x.png) 
in a [status bar](https://www.jetbrains.com/help/idea/guided-tour-around-the-user-interface.html#status-bar).
