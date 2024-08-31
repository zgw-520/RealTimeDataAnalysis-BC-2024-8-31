#!/bin/bash

while [ 1 ]; do
    ./sample_web_log.py > test.log

    tmplog="access.`date +'%s'`.log"
    mv ./test.log ./data/$tmplog
    echo "`date +"%F %T"` generating $tmplog succeed"
    sleep 2 
done
