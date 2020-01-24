import pytest
import testinfra

check_output = testinfra.get_host(
    'local://'
).check_output


class CommandLineArguments:
    def __init__(self, docker_image):
        self.docker_image = docker_image


@pytest.fixture()
def host(request):
    arguments = _parse_command_line_arguments(request)

    image_id = arguments.docker_image or check_output('docker build -q %s', request.param)

    container_id = check_output(
        'docker run -d --entrypoint tail %s -f /dev/null', image_id
    )

    def teardown():
        check_output('docker rm -f %s', container_id)

    request.addfinalizer(teardown)

    return testinfra.get_host('docker://' + container_id)


def _parse_command_line_arguments(request):
    option_docker_image = request.config.getoption('--docker-image')

    return CommandLineArguments(
        docker_image=option_docker_image
    )


def pytest_addoption(parser):
    parser.addoption(
        '--docker-image',
        action='store',
        type='string',
        help='Login for admin bitbucket user',
        required=False
    )


def pytest_generate_tests(metafunc):
    if 'host' in metafunc.fixturenames:

        marker = metafunc.definition.get_closest_marker('docker')
        if marker is None:
            raise Exception('docker marker is required for infrastructure tests')

        path = marker.kwargs.get('path')
        if path is None:
            path = '.'

        metafunc.parametrize(
            'host',
            [path],
            indirect=True,
            scope='module'
        )
