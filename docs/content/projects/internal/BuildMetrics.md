# Build metrics

???+ warning
    This plugin is internal and not recommended to use.  
    See alternatives: 
    [Gradle Enterprise](https://gradle.com/gradle-enterprise-solution-overview/), 
    [Talaiot](https://github.com/cdsap/Talaiot)

## Configuring

```kotlin
buildMetrics {
    metricsPrefix.addAll("prefix") // optional
}
```

### Disabling plugin

Project property `avito.build.metrics.enabled=false`

## Metrics

All metrics can use common placeholders in prefix:

- Namespace: statsd prefix from `avito.stats.namespace` property
- Prefix: from `metricsPrefix` property
- Build status: `success` | `failure`

All mentioned prefixes will be referred in docs as `<...>`.

### Build cache metrics

[Http build cache](https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_configure_remote) errors:

- `<namespace>.[<prefix>].build.cache.errors.[load|store].<http status code>`: errors counter
  
Remote cache statistics:

- `<namespace>.[<prefix>].build.cache.remote.[hit|miss]`: remote cache operations count by environments.  
Shows count of cacheable tasks that were requested from the remote cache.  
This is the same as **Performance** | **Build cache** | **Remote cache** | **Operations** | **Hit\Miss** in build scan.

### Common build metrics

- `<namespace>.[<prefix>].id.<build status>.init_configuration.total` (time in ms): initialization with configuration time
- `<namespace>.[<prefix>].id.<build status>.build-time.total` (time in ms): total build time

### Tasks metrics

- `<namespace>.[<prefix>].build.tasks.cumulative.any` (time in ms):  
  cumulative time of all tasks
  
#### Slowest tasks

These metrics give different aggregates for tasks to highlight the slowest ones.

- `<namespace>.[<prefix>].build.tasks.slow.task.<module>.<task type>` (time in ms):  
  top slowest tasks
- `<namespace>.[<prefix>].build.tasks.slow.type.<task type>` (time in ms):  
  cumulative time of top slowest task types
- `<namespace>.[<prefix>].build.tasks.slow.module.<module>` (time in ms):  
  cumulative time of tasks in top slowest modules

Example:

```mermaid
graph LR
    lib_KotlinCompile(:lib:compileKotlin - 2s) --> lib_bundleAar(:lib:bundleAar - 1s)
    lib_KotlinCompile --> app_KotlinCompile(:app:compileKotlin - 3s)
    app_KotlinCompile --> app_bundleAar(:app:bundleAar - 1s)
    lib_bundleAar --> app_bundleAar
```

- `.tasks.cumulative.any`: 7s
- `.tasks.slow.task.app.KotlinCompile`: 3s
- `.tasks.slow.type.KotlinCompile`: 5s
- `.tasks.slow.module.app`: 4s

#### Critical path

These metrics describe a critical path.
To understand the critical path better see a visualization in a [build trace](../BuildTrace.md#critical-path).

- `<namespace>.[<prefix>].build.tasks.critical.task.<module>.<task type>` (time in ms):  
  tasks in the critical path

### JVM metrics

`<namespace>.[<prefix>].jvm.memory.<jvm process name>.[heap|metaspace].[used|committed]` - in KiB

This reflects what you can find by [jcmd PID GC.heap_info](https://www.baeldung.com/java-heap-size-cli#jcmd).  
All values are measured in Kb and sent as time metrics.

### OS metrics

`<namespace>.[<prefix>].os.memory.[used|total]` - in KiB

### Specific build events

- `<namespace>.[<prefix>].id.<build status>.app-build.<module path>.<task name>.finish` (time in ms): 
  elapsed time from build start till Android app build task finished
