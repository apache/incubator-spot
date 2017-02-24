#!/bin/bash

local_path=`pwd`
install_path=/opt/spot

# functions

log_cmd () {

    printf "\n****SPOT.ML.post_build.sh****\n"
    date +"%y-%m-%d %H:%M:%S"
    printf "$1\n\n"
}

log_cmd "copying generated files to /opt/spot/"
cp ./target/scala-2.10/spot-ml-assembly-1.1.jar ${install_path}/jar/
cp ./ml_ops.sh ${install_path}/bin

log_cmd "complete"
