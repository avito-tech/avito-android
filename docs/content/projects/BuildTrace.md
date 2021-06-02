# Build trace Gradle plugin

This plugin is a primitive analog of [Gradle build scan](https://scans.gradle.com/). 
Use it if you can't use a build scan for any reason.

This plugin collects tasks execution time in a trace event format.

![](https://user-images.githubusercontent.com/1104540/117260848-2b323d80-ae58-11eb-901f-9d2e2fda453f.png)

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
Build trace: <path to the project>/outputs/build-trace/build.trace
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

To understand the critical path better see [critical path](internal/CriticalPath.md).  
Tasks on this path are [highlighted in a trace](#build-trace-gradle-plugin).  

You can find them by query:

```sql
SELECT slice.name AS TASK_PATH, slice.ts / 1000000 AS START_MS, slice.dur / 1000000 AS DURATION_MS
FROM slice JOIN args ON slice.arg_set_id = args.arg_set_id
WHERE args.flat_KEY = "args.CRITICAL_PATH"
ORDER BY slice.ts ASC
```

## Known issues

- Tasks' completion time is long after a real time ([#8630](https://github.com/gradle/gradle/issues/8630)). 
In a trace it looks like a task is completed right after the another from the same module.
