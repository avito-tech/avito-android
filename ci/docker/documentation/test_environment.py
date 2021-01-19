import pytest
from hamcrest import assert_that, contains_string


# noinspection PyPep8Naming
@pytest.mark.usefixtures("host")
@pytest.mark.docker()
class TestEnvironment(object):

    def test_hugo_is_extended_version(self, host):
        hugo_version = host.run('mkdocs --version').stdout
        assert_that(
            hugo_version,
            contains_string('mkdocs, version')
        )
