#!/bin/bash
if [ ! -f /.dnsmasq_is_configured ]; then  
	mkdir -p $TFTP_FOLDER/pxelinux.cfg
	mkdir -p $PXE_INDIR
	mkdir -p $DNSMASQ_PXEFOLDER
	mkdir -p $DHCP_HOSTFILE_DIR	
	cp -r /bootstrap/* $TFTP_FOLDER
	ln -s /export/$ROOTFS_FOLDER $TFTP_FOLDER/rootfs	
	sed -i "s#NFSROOT#$NFS_DFLT_ROOTFS_PATH#g" /pxe_default
	sed -i "s#NFSHOME#$NFS_DFLT_HOME_PATH#g" /pxe_default
	sed -i "s#NFSIP#$NFS_IP#g" /pxe_default
	sed -i "s#ROOTFS_FOLDER#$ROOTFS_FOLDER#g" /pxe_default

	cp /pxe_default $PXE_INDIR/default
	touch /hosts
	touch /.dnsmasq_is_configured
fi
rm /var/lib/misc/dnsmasq.leases
touch /var/lib/misc/dnsmasq.leases
IFACE=$(netstat -ie | grep -B1 "$TESTBED_CONTROL_SERVER_IP" | head -n1 | awk '{print $1}')
# DNS-Loockup (so we can use the dns-names e.g. on this machine)
#dnsmasq -q -b -i docker0 --except-interface lo -z  --addn-hosts=/hosts --hostsdir=$TFTP_FOLDER/$HOSTFILE_DIR &
dnsmasq -q -b -i docker0 --except-interface lo -z  --addn-hosts=/hosts --addn-hosts=$TFTP_FOLDER/$HOSTFILE_DIR &
# DHCP, TFTP and DNS-Lookup (so our nodes are served well)
#dnsmasq -d -q -b -E --log-queries --interface=$IFACE --dhcp-range=$DHCP_RANGE,infinite --dhcp-boot=pxelinux.0 --domain=$TESTBED_DOMAIN --enable-tftp --local=/$TESTBED_DOMAIN/ --dhcp-hostsdir=$TFTP_FOLDER/$DHCP_HOSTFILE_DIR --tftp-root=$TFTP_FOLDER --addn-hosts=/hosts --hostsdir=$TFTP_FOLDER/$HOSTFILE_DIR -z
dnsmasq -d -q -b -E --log-queries --interface=$IFACE --dhcp-range=$DHCP_RANGE,infinite --dhcp-boot=pxelinux.0 --domain=$TESTBED_DOMAIN --enable-tftp --local=/$TESTBED_DOMAIN/ --dhcp-hostsfile=$TFTP_FOLDER/$DHCP_HOSTFILE_DIR --tftp-root=$TFTP_FOLDER --addn-hosts=/hosts --addn-hosts=$TFTP_FOLDER/$HOSTFILE_DIR -z

