# Build metrics

???+ warning
    This plugin is internal and not recommended to use.  
    See alternatives: 
    [Gradle Enterprise](https://gradle.com/gradle-enterprise-solution-overview/), 
    [Talaiot](https://github.com/cdsap/Talaiot)

## How to start

1. Configuring the extension

    Set two mandatory properties `buildType` and `environment`. Without them plugin won't work
    
    ```kotlin
    buildMetrics {
        environment.set(BuildEnvironment.CI)
        buildType.set("any string")
    }
    ```

2. Add mandatory gradle properties for statsd and graphite. See modules `subprojects:gradle:statsd-config`, `subprojects:gradle:graphite-config`

### How to disable plugin

Add project property `avito.build.metrics.enabled=false`

#### How to disable concrete metric sending

In Gradle extension we have appropriate flag for each metric e.g.

```kotlin
    buildMetrics {
        sendJvmMetrics.set(false)
    }
```

## Metrics

All metrics sent to graphite or statsd depending on our intention. Contact us for details.

### What is the metric final path

The final metrics path is constructed from:

- namespace prefix
    - `avito.stats.namespace` for statsd metrics
    - `avito.graphite.namespace` for graphite metrics
- plugin prefix. It's static and equal to `builds`
- the unique metric path. Part which different for all metrics

We use tags. And all metrics will have `build_type` and `env` tags which you set up in extension.

### What kinds of metrics does plugin send

All actual kinds of metrics you could find by looking at `BuildMetric` inheritors.

#### Gradle

##### Build cache

- [Http build cache](https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_configure_remote) errors:

- Remote cache statistics:
Shows count of cacheable tasks that were requested from the remote cache.
This is the same as **Performance** | **Build cache** | **Remote cache** | **Operations** | **Hit\Miss** in build scan.

##### Common build metrics

- initialization with configuration time in ms
- total build time in ms

##### Tasks metrics

- Cumulative time of all tasks in ms

##### Slowest tasks

These metrics give different aggregates for tasks to highlight the slowest ones.

- top slowest concrete tasks
- cumulative time of top slowest task types
- cumulative time of tasks in top slowest modules

##### Critical path

These metrics describe a critical path.
To understand the critical path better read [critical path docs](CriticalPath.md).

Send tasks in the critical path in ms

##### PackageApplication

Elapsed time from build start till Android app build task finished in ms

#### Runtime

##### JVM metrics

We send heap and metaspace used and committed memory in KiB.

This reflects what you can find by [jcmd PID GC.heap_info](https://www.baeldung.com/java-heap-size-cli#jcmd).  
All values are measured in Kb and sent as time metrics.

##### OS metrics

Send used and total memory in KiB
