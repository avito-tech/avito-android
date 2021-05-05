# Build trace Gradle plugin

This plugin is a primitive analog of [Gradle build scan](https://scans.gradle.com/). 
Use it if you can't use a build scan for any reason.

This plugin collects tasks execution time in a trace event format.

![](https://user-images.githubusercontent.com/1104540/80872574-63d68e80-8cbb-11ea-9333-c7f5f8c9e557.png)

## Getting started

Apply the plugin in the root buildscript:

```kotlin
plugins {
    id("com.avito.android.build-trace")
}

buildTrace {
    enabled.set(true)
}
```

--8<--
plugins-setup.md
--8<--

Run a build. You will get a message in a log:

```log
Build trace: <path to the project>/outputs/avito/build-trace/build.trace
```

## Inspecting a trace

The trace file can be opened by multiple tools.

Chrome tracing

`chrome://tracing`

This is a legacy viewer.  

Use WASD keys and search field for navigation.  

[Perfetto](https://perfetto.dev/#viewer)

This is a modern alternative for trace files.

Here you can also make [analytical queries by SQL](https://perfetto.dev/docs/analysis/trace-processor):

```sql
-- Slowest tasks
SELECT slice.name AS TASK_PATH, slice.dur / 1000000 AS DURATION_MS
FROM slice
ORDER BY slice.dur DESC
```

## Critical path

[Critical path](https://en.wikipedia.org/wiki/Critical_path_method) is a set of tasks that define the build duration.  

Tasks on this path are highlighted in a trace.  
You can find them by query:

```sql
SELECT slice.name AS TASK_PATH, slice.ts / 1000000 AS START_MS, slice.dur / 1000000 AS DURATION_MS
FROM slice JOIN args ON slice.arg_set_id = args.arg_set_id
WHERE args.flat_KEY = "args.CRITICAL_PATH"
ORDER BY slice.ts ASC
```

Also there is raw data in `outputs/avito/build-trace/critical_path.json`:

???+ warning
    This report is auxiliary and is a subject of change.

```json
[
    {
        "path": ":app:assembleAndroidTest",
        "type": "org.gradle.api.Task",
        "start": 1620217409525,
        "finish": 1620217409525,
        "predecessors": [":avito:assembleDebugAndroidTest"]
    },
    ...
```

## Known issues

- Tasks' completion time is long after a real time ([#8630](https://github.com/gradle/gradle/issues/8630)). 
In a trace it looks like a task is completed right after the another from the same module.
