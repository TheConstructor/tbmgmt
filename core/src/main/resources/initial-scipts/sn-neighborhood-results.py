#!/usr/bin/env -S python -t
# -*- coding: utf-8 -*-

from __future__ import print_function

import json
import re
import sys

# extract source and neighbors, e.g.:
# ------ source: 117, destination: 113 ------
# csend 113 ping
# sending to 113: ping (5 chars)
# needed 0 retransmissions
# [127.1:csend] OK
# ------ source: 117, destination: 121 ------
# csend 121 ping
# sending to 121: ping (5 chars)
# [127.1:csend] FAILED
# # usage: <address> <message>: send message via DLL
# ------ source: 117, destination: 124 ------
# ...

re_nodes = re.compile(r"^------ source: (\d+), destination: (\d+) ------$")
re_success = re.compile(r"^\[127\.1:csend\] OK$")

result_dict = dict()
# loop through the file
for line in sys.stdin:
    nodes_match = re_nodes.match(line)
    if nodes_match:
        source = nodes_match.group(1)
        destination = nodes_match.group(2)
        continue
    success_match = re_success.match(line)
    if success_match:
        result_dict[destination] = 0
        continue

# try to save the values in the database
for key, val in result_dict.iteritems():
    print(json.dumps({
        'source': "00:00:00:00:00:" + hex(int(source)).lstrip("0x"),
        'destination': "00:00:00:00:00:" + hex(int(key)).lstrip("0x"),
        'loss': val
    }))
