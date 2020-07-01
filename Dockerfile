#Version 0.0.1
FROM tbmgmt_comsys_testbed:latest

MAINTAINER Matthias Vill "m_vill04@wwu.de"

ENV TBMGMT_VERSION=0.1.0

COPY pom.xml versions-maven-plugin-rules.xml /build/
COPY core/ /build/core/
COPY experiment-control/ /build/experiment-control/
COPY test-support/ /build/test-support/
COPY web/ /build/web/

RUN groupadd testbed-group

RUN addgroup --gid 2000 testbed-user \
    && useradd -u 2000 -g 2000 -m -s /bin/bash testbed-user \
    && chown -R testbed-user /build


COPY build.sh /build/build.sh
WORKDIR /build
RUN chmod +x ./build.sh
RUN ./build.sh

FROM tbmgmt_comsys_testbed:latest

ENV TBMGMT_VERSION=0.1.0

RUN groupadd testbed-group

RUN addgroup --gid 2000 testbed-user \
    && useradd -u 2000 -g 2000 -m -s /bin/bash testbed-user \
    && mkdir /install \
    && chown -R testbed-user /install

COPY --from=0 /build/*/target/*.jar /build/*/target/*.war /install/

RUN mkdir -p /srv/scripts/
#COPY scripts /srv/scripts/
#COPY setup-hostgroup-template /srv/setup-hostgroup-template
#RUN chmod 777 /srv/scripts/*

WORKDIR /home/testbed-user

# https://spring.io/guides/gs/spring-boot-docker/ does recommend the next line
# VOLUME /tmp
