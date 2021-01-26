# Test runner metrics

![Metrics](https://user-images.githubusercontent.com/1105133/105228737-fb467f00-5b73-11eb-801a-da494182f431.png)

Metrics available at:

`$namespace.testrunner.$buildId.$instrumentationConfigName.`

### `device-utilization.median`

Effective device work time relative to total device claimed time, in percent `0-100` \
Median for all devices in configuration

### `initial-delay`

Time from test runner job start to first test execution started

### `end-delay`

Time from last test execution finished to test runner job finished

### `queue-median`

Median tests queue time (from test suite started to moment test claimed a device)

### `install-median`

Median installation time (from moment test claimed a device to actual test start)

### `suite`

Time from first test execution start to last test execution finished

### `total`

Total job time (suite time with both delays)

## `tests.status.lost.`

Tests count with lost status with categorized reasons:

### `not-reported`

Don't know exact reason, delta between initial test suite and reported tests

### `no-file`

Test report data pulled from device and then pushed to report service \
These are cases when file was not found on device for some reason

### `parse-errors`

If test file was pulled, but there was a parsing error

## Example in grafana:

![grafana](https://user-images.githubusercontent.com/1105133/106182950-a2e53200-61b0-11eb-9615-f892fa879c84.png)

