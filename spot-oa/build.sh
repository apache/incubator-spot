#!/bin/bash

local_path=`pwd`
install_path=/opt/spot/

# functions

log_cmd () {

    printf "\n****SPOT.OA.install.sh****\n"
    date +"%y-%m-%d %H:%M:%S"
    printf "$1\n\n"
}

# end functions

if [ ! -d ${install_path} ]; then
    log_cmd "${install_path} not created, Please run spot-setup/install.sh first"
    exit 1    
fi

if [ -z ${local_path}/requirements.txt ]; then
    pip install -r requirements.txt
fi

cd ui
npm install

log_cmd "spot-oa dependencies installed"
cleanup
