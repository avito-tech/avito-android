# Critical path

[Critical path](https://en.wikipedia.org/wiki/Critical_path_method) is a set of tasks that define the build duration.
The plugin gives a report with raw data.

It is very similar to what you can find in a build scan in Timeline tab:
![build scan - critical path](https://user-images.githubusercontent.com/1104540/120473066-4b99dd00-c3af-11eb-97e0-fe9641995325.png)

## Getting started

Apply the plugin in the root buildscript:

```kotlin
plugins {
    id("com.avito.android.critical-path")
}

criticalPath {
    enabled.set(true)
}
```

--8<--
plugins-setup.md
--8<--

Run a build. You will get a report in `build/reports/critical-path/` directory.

???+ warning
    This report is auxiliary and is a subject of change.

```json
[
    {
        "path": ":lib:compileKotlin",
        "type": "KotlinCompile",
        "start": 1620217409525,
        "finish": 1620217409525
    }
]
```

See also [build trace](../BuildTrace.md) for visualization.
