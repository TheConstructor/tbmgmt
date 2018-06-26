#!/usr/bin/env -S python -t
# -*- coding: utf-8 -*-

from __future__ import print_function

import json
import re
import sys

# extract the ping statistics, e.g.:
#   --- paracel.mi.fu-berlin.de ping statistics ---
#   7 packets transmitted, 7 received, 0% packet loss, time 5999ms
#   rtt min/avg/max/mdev = 15.866/16.084/16.489/0.227 ms
re_host = re.compile(r"^--- (.+) ping statistics ---$")
# Mac OS does not provide time-total
re_stat = re.compile(
        r"^(\d+) packets transmitted, (\d+)(?: packets)? received,.* ([.0-9]+)% packet loss(?:, time (\d+)ms)?$")
re_rtt = re.compile(
    r"^(?:rtt min/avg/max/mdev|round-trip min/avg/max/stddev) = (\d+\.\d+)/(\d+\.\d+)/(\d+\.\d+)/(\d+\.\d+) ms$")
result_dict = dict()
# loop through the file
for line in sys.stdin:
    host_match = re_host.match(line)
    if host_match:
        # save the interesting values in a dictionary
        result_dict.update({'host': host_match.group(1)})
        continue
    stat_match = re_stat.match(line)
    if stat_match:
        # save the interesting values in a dictionary
        result_dict.update({'transmitted': stat_match.group(1), 'received':
            stat_match.group(2), 'loss': stat_match.group(3),
                            'time': stat_match.group(4)})
        continue
    rtt_match = re_rtt.match(line)
    if rtt_match:
        # save the interesting values in a dictionary
        result_dict.update({'rtt_min': rtt_match.group(1), 'rtt_avg':
            rtt_match.group(2), 'rtt_max': rtt_match.group(3),
                            'rtt_mdev': rtt_match.group(4)})
        continue

# if the file didn't contain all desired values exit the program without
# changing the database
if len(result_dict.keys()) != 9:
    # silently ignore unreachable hosts
    if 'loss' in result_dict and result_dict['loss'].startswith('100'):
        print("100% packet loss: host is unreachable and silently ignored.", file=sys.stderr)
        sys.exit(0)
    else:
        print("Could not detect all required values in the input file.", file=sys.stderr)
        print("Make sure that the content of the file matches the regular expressions!", file=sys.stderr)
        sys.exit(1)

print(json.dumps(result_dict))
