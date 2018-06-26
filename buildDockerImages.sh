#!/usr/bin/env bash
docker-compose -p tbmgmt build --pull --force-rm comsys_testbed
docker-compose -p tbmgmt build --force-rm ldap_server
#Image - nothing to build
#docker-compose -p tbmgmt build --pull --force-rm postgresql
docker-compose -p tbmgmt build --force-rm tbmgmt
docker-compose -p tbmgmt build --force-rm web
docker-compose -p tbmgmt build --force-rm experiment-control

docker-compose -p tbmgmt scale postgresql=1
sleep 10
docker-compose -p tbmgmt scale ldap_server=1 web=1 experiment-control=1
