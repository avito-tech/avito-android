ARG DOCKER_REGISTRY
FROM ${DOCKER_REGISTRY}/android/python:3.9-slim

COPY requirements.txt .

RUN apt-get update && \
    apt-get install --no-install-recommends --yes \
    sudo \
    openssh-client \
    ca-certificates \
    git && \
    rm -rf /var/lib/apt/lists/*

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 8000

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
