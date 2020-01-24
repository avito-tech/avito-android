import os

import fire
import yaml

from .emulator import publish_emulator
from .docker import publish, test

CONFIG_FILE = 'image.yaml'


# noinspection PyClassHasNoInit
class Command:
    @staticmethod
    def publish(directory):
        config_file_path = Command._check_configuration(directory)

        with open(config_file_path, 'r') as config_file:
            config = yaml.load(config_file)
            docker_registry = config['registry']
            if docker_registry == 'DOCKER_REGISTRY':
                docker_registry = os.getenv('DOCKER_REGISTRY', '___MISSED_DOCKER_REGISTRY_ENV___')
            publish(directory, docker_registry, config['image'])

    @staticmethod
    def publish_emulator(directory, emulators):
        emulators_to_publish = str(emulators).split()

        print('Emulators for publishing: {emulators}'.format(emulators=emulators_to_publish))

        config_file_path = Command._check_configuration(directory)

        with open(config_file_path, 'r') as config_file:
            config = yaml.load(config_file)
            docker_registry = config['registry']
            if docker_registry == 'DOCKER_REGISTRY':
                docker_registry = os.getenv('DOCKER_REGISTRY', '___MISSED_DOCKER_REGISTRY_ENV___')
            publish_emulator(directory, docker_registry, config['image'], emulators_to_publish)

    @staticmethod
    def test():
        test()

    @staticmethod
    def _check_configuration(directory):
        if not directory:
            raise ValueError('Directory is not specified')

        config_file_path = os.path.join(directory, CONFIG_FILE)

        if not os.path.isfile(config_file_path):
            raise ValueError('Configuration file not found in specified directory')

        return config_file_path


if __name__ == '__main__':
    fire.Fire(Command)
