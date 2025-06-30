# Use the official PostgreSQL image with the latest version
FROM postgres:17.5

# Install build dependencies for pg_cron
RUN apt-get update && \
    apt-get install -y postgresql-17-cron && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN echo "shared_preload_libraries='pg_cron'" >> /usr/share/postgresql/postgresql.conf.sample
RUN echo "cron.database_name='postgres'" >> /usr/share/postgresql/postgresql.conf.sample

# Copy the SQL initialization script into the container
COPY init.sql /docker-entrypoint-initdb.d/

# Set environment variables to configure PostgreSQL
ENV POSTGRES_INITDB_ARGS="--auth-host=scram-sha-256 --auth-local=scram-sha-256"
