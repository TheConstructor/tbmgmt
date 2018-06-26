FROM tbmgmt_comsys_testbed:latest

VOLUME /export 



RUN DEBIAN_FRONTEND=noninteractive apt-get -q update \
 && DEBIAN_FRONTEND=noninteractive apt-get -qy install dnsmasq wget iptables \
 && mkdir -p /bootstrap
WORKDIR /bootstrap
RUN wget --quiet $MIRROR/debian/dists/$DIST/main/installer-$ARCH/current/images/netboot/debian-installer/$ARCH/linux
RUN wget --quiet $MIRROR/debian/dists/$DIST/main/installer-$ARCH/current/images/netboot/debian-installer/$ARCH/initrd.gz
RUN wget --quiet $MIRROR/debian/dists/$DIST/main/installer-$ARCH/current/images/netboot/debian-installer/$ARCH/pxelinux.0
RUN wget --quiet $MIRROR/debian/dists/$DIST/main/installer-$ARCH/current/images/netboot/debian-installer/$ARCH/boot-screens/ldlinux.c32
ENV TERM=xterm

COPY ./run.sh /run.sh
COPY ./pxe_default /pxe_default 
RUN chmod +x /run.sh
ENTRYPOINT /run.sh
