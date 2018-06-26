#!/bin/bash

if [ -n "$http_proxy" ]; then
    prefix="http://"
    prox=${http_proxy#$prefix}
    ip=$(echo $prox | cut -f1 -d:)
    port=$(echo $prox | cut -f2 -d:)
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml << EOF
<settings>
  <proxies>
   <proxy>
      <id>example-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>$ip</host>
      <port>$port</port>
    </proxy>
  </proxies>
</settings>
EOF
fi

mvn versions:set -DnewVersion=$TBMGMT_VERSION && mvn -DskipITs=true clean install

