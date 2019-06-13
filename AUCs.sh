#!/bin/bash

# ant train
# ant predict

for t in `cat targets.txt`; do
    echo $t
    grep -v Sample $t.txt | sort -n -k2 -r | awk '{print $2" "$3}' | \
        croc-curve
done
