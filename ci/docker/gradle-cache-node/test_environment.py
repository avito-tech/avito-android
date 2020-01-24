import pytest
from hamcrest import assert_that, contains_string


# noinspection PyPep8Naming
@pytest.mark.usefixtures("host")
@pytest.mark.docker()
class TestEnvironment(object):

    def test_build_cache_node_installed(self, host):
        assert_that(
            host.run('java -jar build-cache-node.jar --help').stderr,
            contains_string('Usage: build-cache-node')
        )
