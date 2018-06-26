#!/usr/bin/env -S python -t
# -*- coding: utf-8 -*-

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
re_foo = re.compile(r"^(.*)$")
re_bar = re.compile(r"bar")
result_dict = dict()
# Loop through the file
for line in sys.stdin:
    # The match function only works, if the regular expression matches the
    # complete line. If the regular expression should match a substring of the
    # line (like the normal perl regex), use re_foo.search(line) instead.
    foo_match = re_foo.match(line)
    if foo_match:
        # Save the interesting values in a dictionary
        result_dict.update({'foo': foo_match.group(1)})
        continue
    # Try to match the second regex
    bar_match = re_bar.search(line)
    if bar_match:
        # Save the interesting values in a dictionary
        result_dict.update({'bar': bar_match.group(1)})
        continue

# If the file didn't contain all desired values exit the program without
# changing the database.
# Enter the correct number of values here. You can also ommit this check, if you
# want to allow NULL values in your table. But normally this indicates malformed
# output or wrong regular expressions.
if len(result_dict.keys()) != 2:
    print "Could not detect all required values in the input file."
    print "Make sure that the content of the file matches the regular" \
          "expressions!"
    sys.exit(1)

print(json.dumps(result_dict))
