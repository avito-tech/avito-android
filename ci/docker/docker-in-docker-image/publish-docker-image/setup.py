import sys

try:
    # noinspection PyUnresolvedReferences
    from setuptools import setup
except ImportError:
    print("Adb waiter now needs setuptools in order to build. Install it using"
          " your package manager (usually python-setuptools) or via pip (pip"
          " install setuptools).")
    sys.exit(1)

__version__ = '0.2'
__author__ = 'Avito Android'

# noinspection PyBroadException
try:
    with open('requirements.txt') as f:
        requirements = f.read().splitlines()
except Exception:
    requirements = []
    print("Error while parsing requirements file")

setup(
    name='publish_docker_image',
    version=__version__,
    url='https://github.com/avito-tech/avito-android/tree/develop/ci',
    description='Publishing and testing android docker images',
    author=__author__,
    author_email='avito-android-fake-account@avito.ru',
    install_requires=requirements,
    package_dir={'publish_docker_image': 'publish_docker_image'},
    packages=['publish_docker_image'],
    include_package_data=True,
    scripts=[
        'bin/publish_docker_image',
    ],
)
