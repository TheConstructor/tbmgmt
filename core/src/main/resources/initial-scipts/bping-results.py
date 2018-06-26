#!/usr/bin/env -S python -t
# -*- coding: utf-8 -*-

import json
import re
import sys

# extract the ping statistics, e.g.:
# PING 192.168.18.0 (192.168.18.0) 56(84) bytes of data.
# 64 bytes from 192.168.18.6: icmp_seq=1 ttl=64 time=0.000 ms
# 64 bytes from 192.168.18.33: icmp_seq=1 ttl=64 time=3.00 ms (DUP!)
# ...

re_source = re.compile(r"^64 bytes from (.+) icmp_seq=(\d+) ttl=(\d+) time=(\d+.\d+) ms$")
re_destination = re.compile(r"^64 bytes from (.+) icmp_seq=(\d+) ttl=(\d+) time=(\d+.\d+) ms \(DUP!\)$")

result_dict = dict()
# loop through the file
for line in sys.stdin:
    source_match = re_source.match(line)
    if source_match:
        source = source_match.group(1)[:-1]
        continue
    destination_match = re_destination.match(line)
    if destination_match:
        # process the ping replay of a neighbor - save its ip and delay
        destination = destination_match.group(1)[:-1]
        time = destination_match.group(4)
        result_dict.update({destination: time})
        continue

# if no neighbors are found, exit and point out that the file format might be wrong
if len(result_dict.keys()) == 0:
    print "No neighbors found. Please check the supplied file format."
    sys.exit(1)

# try to save the values in the database
for key, val in result_dict.iteritems():
    print(json.dumps({'source': source, 'destination': key, 'time': val}))
