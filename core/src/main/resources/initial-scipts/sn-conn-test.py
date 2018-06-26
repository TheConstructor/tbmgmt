#!/usr/bin/env -S python -t
# -*- coding: utf-8 -*-

from __future__ import print_function

import json
import sys

# extract source and neighbors, e.g.:
# 113 124
# 113 11
# 113 9
# 113 7
# 124 117
# 124 113
# ...

result_dict = dict()
# loop through the file
for line in sys.stdin:
    (source, neighbor) = line.split()
    if source not in result_dict.keys():
        result_dict[source] = list()
    result_dict[source].append(neighbor)

# try to save the values in the database
for source in result_dict.keys():
    for neighbor in result_dict[source]:
        print(json.dumps({
            'source': "00:00:00:00:00:" + hex(int(source)).lstrip("0x"),
            'destination': "00:00:00:00:00:" + hex(int(neighbor)).lstrip("0x"),
            'loss': 1
        }))
