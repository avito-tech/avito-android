import pytest
from hamcrest import assert_that, contains_string, is_


# noinspection PyPep8Naming
@pytest.mark.usefixtures("host")
@pytest.mark.docker()
class TestEnvironment(object):
    def test_adb_installed(self, host):
        assert_that(
            host.run('adb version').stdout,
            contains_string('Android Debug Bridge version')
        )

    def test_avd_list_emulator_exists(self, host):
        assert_that(
            host.run('avdmanager list avd').stdout,
            contains_string('emulator_')
        )

    def test_emulator_snapshot_exists(self, host):
        assert_that(
            host.run('find /root/.android/avd/emulator_*.avd/snapshots/ci').rc,
            is_(0)
        )
