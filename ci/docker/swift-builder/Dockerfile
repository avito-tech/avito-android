FROM swift:5.6.2

RUN \
  apt-get update && \
  apt-get install -y \
    sudo \
    zip \
    libssl-dev && \
  apt-get clean && \
  apt-get purge

# Entrypoint script will allow us run as non-root in the container.
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
