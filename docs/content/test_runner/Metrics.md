# Test runner metrics

![grafana](https://user-images.githubusercontent.com/1105133/106182950-a2e53200-61b0-11eb-9615-f892fa879c84.png)

Metrics available at:

`$namespace.testrunner.$buildId.$instrumentationConfigName.`

### `device-utilization.median`

Median of all effective device time relative to total device claimed time (in percent `0-100`)

```mermaid
graph LR
    id1[Device started] --> id2[Test claimed a device] 
    subgraph ef1[effective part 1]
    id2 --> id3[Test started] --> id4[Test finished]
    end
    id4 --> id5[Waiting for another intention]
    id5 --> id6[Another test run]
    subgraph ef2[effective part 2]
    id6
    end
    id6 --> id7[Device finished]
    
```

### `initial-delay`

Single stat per test suite (instrumentation configuration)

```mermaid
graph LR
    id1[Test runner start] -->|ms|id2[First test execution start]
```

### `end-delay`

Single stat per test suite

```mermaid
graph LR
    id1[Last test execution ended] -->|ms|id2[Test Runner finished]
```

### `queue-median`

Median of tests queue time

```mermaid
graph LR
    id1[Test runner start] -->|ms|id2[Test claimed a device]
```

### `install-median`

Median of installation times

```mermaid
graph LR
    id1[Test claimed a device] -->|ms|id2[Test execution start]
```

### `suite`

Single stat per test suite

```mermaid
graph LR
    id1[First test execution start] -->|ms|id2[Last test execution ended]
```

### `total`

Single stat per test suite

```mermaid
graph LR
    id1[Test runner start] -->|ms|id2[Test runner finished]
```

## `tests.status.lost.`

Tests count with lost status with categorized reasons:

### `not-reported`

Don't know exact reason, delta between initial test suite and reported tests

### `no-file`

Test report data pulled from device and then pushed to report service. \
These are cases when file was not found on device for some reason

### `parse-errors`

If test file was pulled, but there was a parsing error

## `adb`

Lower level metrics from AdbDevice, to observe device connection issues. \
It is count metrics, aggregated by time windows

### `get-sdk-property`.[ `success` / `attempt-fail` / `failure` ]

### `install-application`.[ `success` / `attempt-fail` / `failure` ]

### `get-alive-device`.[ `success` / `attempt-fail` / `failure` ]

### `clear-package`.[ `success` / `attempt-fail` / `failure` ]

### `pull`.[ `success` / `attempt-fail` / `failure` ]

### `clear-directory`.[ `success` / `nothing` / `attempt-fail` / `failure` ]

### `list`.[ `success` / `nothing` / `attempt-fail` / `failure` ]

### `run-test`.[ `passed` / `ignored` / `error` / `infrastructure-err` / `failed-on-start` / `failed-instrum-parse` ]
