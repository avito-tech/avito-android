import os
import re

from .bash import execute

OK = 0
TESTS_NOT_FOUND = 5

IMAGE_HASH_PATTERN = re.compile(
    pattern='^Successfully built (.{12})$',
    flags=re.MULTILINE
)

RUN_DETACHED_PRIVILEGED = 'docker run -d --privileged --entrypoint "sleep" {image} "1d"'
EXECUTE_COMMAND_IN_CONTAINER_COMMAND = 'docker exec {container} bash -c "{command}"'
BUILD_COMMAND = 'docker build {build_args} --pull --tag {build_tag} {directory}'
TEST_COMMAND = 'pytest --docker-image "{image}" --verbose --capture=no --cache-clear'
TAG_COMMAND = 'docker tag {old_image} {new_image}:{new_tag}'
PUSH_COMMAND = 'docker push {image}'
REMOVE_COMMAND = 'docker rm -f {container}'
COMMIT_COMMAND = 'docker commit --change=\'ENTRYPOINT ["{entrypoint}"]\' {container}'
LOGIN_COMMAND = 'echo {password} | docker login --username {username} --password-stdin {registry}'
COMMIT_RESULT_IMAGE_PREFIX = 'sha256:'


def build_image(image, directory, build_args=None):
    if build_args is None:
        build_args = {}
    build_args_string = ' '.join([f'--build-arg {arg}={value}' for arg, value in build_args.items()])
    status, output = execute(BUILD_COMMAND.format(build_args=build_args_string, build_tag=image, directory=directory))
    if status != OK:
        raise RuntimeError('Failed to build image')

    return IMAGE_HASH_PATTERN.findall(output.strip())[-1]


def tag_image(old_image, new_image, tag):
    print('Add tag: {tag} to image: {old_image}'.format(tag=tag, old_image=old_image))
    status, output = execute(
        TAG_COMMAND.format(
            old_image=old_image,
            new_image=new_image,
            new_tag=tag
        )
    )
    print(output)

    if status != OK:
        raise RuntimeError('Failed to tag image')


def commit(container, entrypoint):
    status, output = execute(COMMIT_COMMAND.format(entrypoint=entrypoint, container=container))

    if status != OK:
        raise RuntimeError('Failed to commit container: {container}'.format(container=container))

    result = output.strip()

    if result.startswith(COMMIT_RESULT_IMAGE_PREFIX):
        result = result[len(COMMIT_RESULT_IMAGE_PREFIX):]

    return result


def remove(container):
    status, output = execute(REMOVE_COMMAND.format(container=container))

    if status != OK:
        raise RuntimeError('Failed to remove container: {container}'.format(container=container))


def push_image(image):
    print('Pushing {image} to registry'.format(image=image))
    status, output = execute(PUSH_COMMAND.format(image=image))
    print(output)

    if status != OK:
        raise RuntimeError('Failed to push image')


def execute_command_in_container(container, command):
    print('Execute command: {command} in container: {container}'.format(command=command, container=container))
    print(EXECUTE_COMMAND_IN_CONTAINER_COMMAND.format(container=container, command=command))
    status, output = execute(EXECUTE_COMMAND_IN_CONTAINER_COMMAND.format(container=container, command=command))
    print(output)

    if status != OK:
        raise RuntimeError('Execution command: {command} failed in container: {container}. Output: {output}'.format(
            command=command,
            container=container,
            output=output
        ))


def run_detached_privileged_container(image):
    status, output = execute(RUN_DETACHED_PRIVILEGED.format(image=image))

    if status != OK:
        raise RuntimeError('Failed to run detached privileged image')

    return output.strip()


def test(image=''):
    print('Testing image')
    status, output = execute(TEST_COMMAND.format(image=image), 'build')
    if status != OK and status != TESTS_NOT_FOUND:
        raise RuntimeError('Tests failed')


def login(registry):
    if ("DOCKER_LOGIN" in os.environ) and ("DOCKER_PASSWORD" in os.environ):
        username = os.environ["DOCKER_LOGIN"]
        password = os.environ["DOCKER_PASSWORD"]
        status, output = execute(LOGIN_COMMAND.format(username=username, password=password, registry=registry))
        print(output)
        if status != OK:
            raise RuntimeError('Failed to login')
        else:
            return True
    else:
        print('DOCKER_LOGIN & DOCKER_PASSWORD were not provided. Skipping login stage...')
        return False


def hash_tag(image_hash, is_login_successful):
    print('Digest: {digest}'.format(digest=image_hash))
    push_tag = image_hash[:10]

    if not is_login_successful:
        push_tag += "-local"

    print('PushTag: {pushTag}'.format(pushTag=push_tag))
    return push_tag


def push(is_login_successful, push_tag, full_image_name):
    if is_login_successful:
        push_image(full_image_name)
        print('Image {image}:{tag} has been published successfully'.format(image=full_image_name, tag=push_tag))
    else:
        print('Docker login was not successful, use local image {image}:{tag} for debug purposes'.format(
            image=full_image_name, tag=push_tag))


def publish(directory, registry, image):
    full_image_name = '{registry}/{image}'.format(registry=registry, image=image)
    print('Building image: {image}'.format(image=full_image_name))
    image_hash = build_image(full_image_name, directory, { 'DOCKER_REGISTRY' : registry })

    test()

    is_login_successful = login(registry)

    push_tag = hash_tag(image_hash, is_login_successful)

    tag_image(
        old_image=full_image_name,
        new_image=full_image_name,
        tag=push_tag
    )

    push(is_login_successful, push_tag, full_image_name)
