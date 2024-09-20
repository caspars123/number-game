FROM postgres:12.1

ADD ./docker-entrypoint-initdb.d /docker-entrypoint-initdb.d

CMD docker-entrypoint.sh -c 'max_connections=200'
