#!/usr/bin/env -S python -t
# -*- coding: utf-8 -*-

from __future__ import print_function

import json
import re
import sys

# extract source and neighbors, e.g.:
# source: 192.168.18.14
# 192.168.18.4: 5.000000
# 192.168.18.5: 20.000000
# 192.168.18.11: 0.000000
# ...

re_source = re.compile(r"^source: (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})$")
re_neighbor = re.compile(r"^(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}): (\d+\.\d+)$")

result_dict = dict()
# loop through the file
for line in sys.stdin:
    source_match = re_source.match(line)
    if source_match:
        source = source_match.group(1)
        continue
    neighbor_match = re_neighbor.match(line)
    if neighbor_match:
        neighbor = neighbor_match.group(1)
        loss = neighbor_match.group(2)
        result_dict[neighbor] = loss
        continue

# try to save the values in the database
for key, val in result_dict.iteritems():
    print(json.dumps({'source': source, 'destination': key, 'loss': val}))
