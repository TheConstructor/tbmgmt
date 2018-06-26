#!/usr/bin/env -S python -t
# -*- coding: utf-8 -*-

from __future__ import print_function

import json
import re
import sys

# The following algorithm simply tries to match the regular expressions on
# every line. If you need to match over multiple lines, feel free to use your
# own parser or use a parser from an external module, if you want to examine XML
# files for example.
# The only thing you need in the end, is a dictionary that contains the column
# names of the SQL table as keys and the parsed values as corresponding values.

# Define regular expressions for the desired values
re_hosts = re.compile(
    r"local (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}) port \d+ connected with (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})")
re_perf = re.compile(r"0.0- *(\d+\.\d+) sec +(\d+\.?\d*) ([KMG]?Bytes) +(\d+\.?\d*) ([KMG]?bits/sec)")
result_dict = dict()
# Loop through the file
for line in sys.stdin:
    hosts_match = re_hosts.search(line)
    if hosts_match:
        # Save the interesting values in a dictionary
        result_dict.update({'local': hosts_match.group(1),
                            'remote': hosts_match.group(2)})
        continue
    perf_match = re_perf.search(line)
    if perf_match:
        # Save the interesting values in a dictionary
        result_dict.update({'seconds': perf_match.group(1),
                            'data_val': perf_match.group(2),
                            'data_unit': perf_match.group(3),
                            'throughput_val': perf_match.group(4),
                            'throughput_unit': perf_match.group(5)})
        continue

# If the file didn't contain all desired values exit the program without
# changing the database.
if len(result_dict.keys()) != 7:
    print("Could not detect all required values in the input file.", file=sys.stderr)
    print("Make sure that the content of the file matches the regular expressions!", file=sys.stderr)
    sys.exit(1)

print(json.dumps(result_dict))
