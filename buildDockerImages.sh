#!/usr/bin/env bash
set -e
#docker-compose -p tbmgmt build --pull --force-rm comsys_testbed
#docker-compose -p tbmgmt build --force-rm ldap_server
#Image - nothing to build
#docker-compose -p tbmgmt build --pull --force-rm postgresql
docker-compose -p tbmgmt build --force-rm tbmgmt
docker-compose -p tbmgmt build --force-rm web
docker-compose -p tbmgmt build --force-rm experiment-control

docker-compose -p tbmgmt up -d postgresql ldap_server
sleep 10
docker-compose -p tbmgmt up -d web
#experiment-control
