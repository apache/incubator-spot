#!/bin/bash

local_path=`pwd`
install_path=/opt/spot
host_os=""


# functions

log_cmd () {

    printf "\n****SPOT.ML.build.sh****\n"
    date +"%y-%m-%d %H:%M:%S"
    printf "$1\n\n"
}
       
# end functions

check_os

if [ ! -d ${install_path} ]; then
    log_cmd "${install_path} not created, Please run spot-setup/install.sh first"
    exit 1    
fi

# build
log_cmd 'assembling spot-ml jar'
sbt assembly

log_cmd "spot-ml dependencies installed"
log_cmd 'run `sudo ./post_build.sh` to install in /opt/spot/'
