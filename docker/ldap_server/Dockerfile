#Version 0.0.1
FROM tbmgmt_comsys_testbed:latest
MAINTAINER Tim Loepmeier "loepmeier@wwu.de"

ENV HOME /root

RUN DEBIAN_FRONTEND=noninteractive apt-get -qq update

RUN DEBIAN_FRONTEND=noninteractive apt-get install -yqq slapd ldap-utils

ENV LDAP_ROOTPASS root
ENV LDAP_ORGANISATION Acme Widgets Inc.
ENV LDAP_DOMAIN testbed.home

EXPOSE 389

ADD run.sh /run.sh
ADD ldapuser.ldif /ldapuser.ldif
ADD users.ldif /users.ldif
RUN chmod +x /run.sh

VOLUME /var/lib/ldap
#mount /var/lib/ldap as a data volume

ENTRYPOINT /run.sh
