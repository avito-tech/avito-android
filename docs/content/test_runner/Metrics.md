# Test runner metrics

Metrics available at:

`$namespace.testrunner.$buildId.$instrumentationConfigName.`

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

![Metrics](https://user-images.githubusercontent.com/1105133/105228737-fb467f00-5b73-11eb-801a-da494182f431.png)
