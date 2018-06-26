#!/usr/bin/env -S python -t
# -*- coding: utf-8 -*-

from __future__ import print_function

import json
import sys

# extract the sensor values, e.g.:
# t9-163 2008-12-15 16:32:35 28.26 29.66
# ...

result_dict = dict()
# loop through the file
for line in sys.stdin:
    tokens = line.split(' ')
    if (len(tokens) == 5):
        node = tokens[0]
        timestamp = tokens[1] + ' ' + tokens[2]
        humidity = tokens[3]
        temperature = tokens[4]
        result_dict.update({timestamp: {'temperature': temperature, 'humidity': humidity}})
    else:
        print("Wrong file format, please verify the supplied log file!", file=sys.stderr)

# if no neighbors are found, exit and point out that the file format might be wrong
if len(result_dict.keys()) == 0:
    print("No sensor data found, pls check the filename and fileformat", file=sys.stderr)
    sys.exit(1)

# try to save the values in the database
for key, val in result_dict.iteritems():
    insert_dict = {'node': node, 'timestamp': key}
    for kitem, vitem in val.iteritems():
        insert_dict.update({kitem: vitem})
    print(json.dumps(insert_dict))
