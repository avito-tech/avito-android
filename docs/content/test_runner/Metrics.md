# Test runner metrics

![grafana](https://user-images.githubusercontent.com/1105133/106182950-a2e53200-61b0-11eb-9615-f892fa879c84.png)

Metrics available at:

`$namespace.testrunner.$projectName.$instrumentationConfigName.`

### `devices.living`

Sum of all time from creation of device till finishing across all devices

```mermaid
graph LR
    subgraph ef1[Living]
    id1[Device started] --> id2[Device finished]
    end 
```

### `devices.working`

Sum of all time when device executing tests across all devices

```mermaid
graph LR
    id1[Device started] --> id2 
    subgraph ef1[working]
    id2[Received a test intention] --> id3[Test started] --> id4[Test finished]
    end
    id4 --> id5[Waiting for another intention] --> id6
    subgraph ef2[working]
    id6[Another test run]
    end
    id6 --> id7[Device finished]
    
```

### `devices.idle`

Sum of all time when device do nothing across all devices

```mermaid
graph LR
    id1[Device started] --> id2 
    id2[Received a test intention] --> id3[Test started] --> id4[Test finished] --> id5
    subgraph ef2[idle]
    id5[Waiting for another intention] --> id6[Device finished]
    end
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

Test report data pulled from device and then pushed to report service.  
These are cases when file was not found on device for some reason

### `instr-parsing`

If fail when parse `am instrument` output

### `instr-start`

If fail when staring `am instrument`

### `instr-timeout`

If fail with timeout when executing `am instrument`

### `instr-unexpected`

If catch unexpected error when executing `am instrument`

## `adb`

Lower level metrics from AdbDevice, to observe device connection issues.  
It is count metrics, aggregated by time windows

### `get-sdk-property`.[ `success` / `error` / `failure` ]

### `install-application`.[ `success` / `error` / `failure` ]

### `get-alive-device`.[ `success` / `error` / `failure` ]

### `clear-package`.[ `success` / `error` / `failure` ]

### `pull`.[ `success` / `error` / `failure` ]

### `clear-directory`.[ `success` /  `error` / `failure` ]

### `list`.[ `success` / `error` / `failure` ]

### `run-test`.[ `passed` / `ignored` / `error` / `infrastructure-err` / `failed-on-start` / `failed-instrum-parse` ]
