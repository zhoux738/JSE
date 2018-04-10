#!/bin/bash

# TODO: We will rewrite this script using Julian in the future. Need Process and JSON API.

cd ci-test

jarfile=CI-@VERSION@.jar # example: CI-0.1.6.jar

cat test-manifest.json | jq --arg jarfile $jarfile '.[] | ["java -jar ", $jarfile, "-f", .script_file, (["\(.module_dirs[] | "-mp", .)"] | join(" "))] | join(" ") | @sh' | while read -r line
do
    # extract the command part
    len=`echo ${#line}`
    slen=$(($len-4))
    comm="${line:2:$slen}"
    # echo $comm
    
    # execute the command
    eval $comm
    
    # validate
    retcode=$?
    if [ $retcode -ne 0 ]
    then
        exit $retcode
    fi
done

exit 0