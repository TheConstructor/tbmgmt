#!/usr/bin/env bash
set -eu

status () {
  echo "---> ${@}" >&2
}

set -x
: LDAP_ROOTPASS=${LDAP_ROOTPASS}
: LDAP_DOMAIN=${LDAP_DOMAIN}
: LDAP_ORGANISATION=${LDAP_ORGANISATION}

if [ ! -e /var/lib/ldap/docker_bootstrapped ]; then
  status "configuring slapd for first run"

  cat <<EOF | debconf-set-selections
slapd slapd/internal/generated_adminpw password ${LDAP_ROOTPASS}
slapd slapd/internal/adminpw password ${LDAP_ROOTPASS}
slapd slapd/password2 password ${LDAP_ROOTPASS}
slapd slapd/password1 password ${LDAP_ROOTPASS}
slapd slapd/dump_database_destdir string /var/backups/slapd-VERSION
slapd slapd/domain string ${LDAP_DOMAIN}
slapd shared/organization string ${LDAP_ORGANISATION}
slapd slapd/backend string HDB
slapd slapd/purge_database boolean true
slapd slapd/move_old_database boolean true
slapd slapd/allow_ldap_v2 boolean false
slapd slapd/no_configuration boolean false
slapd slapd/dump_database select when needed
EOF
cat >> /etc/ldap/ldap.conf <<EOF
BASE    dc=testbed,dc=home
URI     ldap://127.0.0.1
EOF

  dpkg-reconfigure -f noninteractive slapd
  touch /var/lib/ldap/docker_bootstrapped
/usr/sbin/slapd -h "ldap:///" -u openldap -g openldap -d 0 &
sleep 3
ldapadd -x -D cn=admin,dc=testbed,dc=home -w $LDAP_ROOTPASS -f ldapuser.ldif
ldapadd -x -D cn=admin,dc=testbed,dc=home -w $LDAP_ROOTPASS -f users.ldif
killall slapd
sleep 3
else
  status "found already-configured slapd"
fi

status "starting slapd"
set -x


/usr/sbin/slapd -h "ldap:///" -u openldap -g openldap -d 0 
