import subprocess

import sys
from threading import Thread


def _handle_output(input, output, result):
    with input:
        for line in input:
            output.write(line.decode('utf-8'))
            output.flush()
            result.append(line.decode('utf-8'))


def execute(command, cwd=None):
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, cwd=cwd)
    result = []

    stdout_handler = Thread(target=_handle_output, args=[process.stdout, sys.stdout, result])
    stderr_handler = Thread(target=_handle_output, args=[process.stderr, sys.stderr, result])

    stdout_handler.start()
    stderr_handler.start()

    process.wait()

    stdout_handler.join()
    stderr_handler.join()

    return process.returncode, ''.join(result)
