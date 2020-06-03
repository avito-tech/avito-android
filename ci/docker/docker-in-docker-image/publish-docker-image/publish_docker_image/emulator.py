import os

from .docker import \
    run_detached_privileged_container, \
    execute_command_in_container, \
    commit, \
    build_image, \
    tag_image, \
    hash_tag, \
    test, \
    login, \
    remove, \
    push


def publish_emulator(directory, registry, image, versions):
    full_image_names = []
    for version in versions:
        image_name = '{image}-{version}'.format(image=image, version=version)
        full_image = '{registry}/{image_name}'.format(registry=registry, image_name=image_name)
        print('Building image: {image}'.format(image=full_image))

        not_prepared_image = build_image(
            image=full_image,
            directory=directory,
            build_args={
                'SDK_VERSION': version,
                'EMULATOR_ARCH': "x86" if int(version) < 28 else "x86_64"
            }
        )
        container = run_detached_privileged_container(
            image=not_prepared_image
        )

        execute_command_in_container(
            container=container,
            command='VERSION={version} ./prepare_snapshot.sh'.format(version=version)
        )

        prepared_image = commit(
            container=container,
            entrypoint='./entrypoint.sh'
        )

        remove(container=container)

        full_image = '{registry}/{image_name}'.format(
            registry=registry,
            image_name=image_name
        )

        is_login_successful = login(registry)
        push_tag = hash_tag(prepared_image, is_login_successful)

        tag_image(
            old_image=prepared_image,
            new_image=full_image,
            tag=push_tag
        )

        full_image_with_tag = "{full_image}:{push_tag}".format(
            full_image=full_image,
            push_tag=push_tag
        )

        test(image=full_image_with_tag)

        push(is_login_successful, push_tag, full_image)

        full_image_names.append(full_image_with_tag)

    with open("build/images.txt", "w") as output:
        for full_image_name in full_image_names:
            output.write(full_image_name)
            output.write(os.linesep)
