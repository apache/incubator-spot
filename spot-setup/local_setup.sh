#!/bin/bash

# Create local directories for Apache Spot (Incubating)
# This script needs to be run with root priviliges or it will fail to create the /opt/ directories

spot_dir=/opt/spot
spot_bin=${spot_dir}/bin
spot_env="export PATH=\$PATH:${spot_bin}"
spot_env_file=/etc/profile.d/spot.sh
spot_user=$1

# functions

log_cmd () {

    printf "\n****SPOT.SETUP.local_setup.sh****\n"
    date +"%y-%m-%d %H:%M:%S"
    printf "$1\n\n"

}

create_dir () {
    if [[ -d "${1}" ]]; then
            log_cmd "${1} already exists"
    else
            log_cmd "Creating ${1}"
            mkdir ${1}
    fi
}

check_root () {
# checking for root as many of these functions interact with system owned directories
if [[ "$EUID" -ne 0 ]]; then

        log_cmd "Non root user detected, Please run as root or with sudo"
        exit 1

fi
}

set_env () {
# add directory to env
if [[ -f "${spot_env_file}" ]]; then
        log_cmd "${spot_env_file} already exists"
else
        log_cmd "Creating ${spot_env_file}"
        echo ${spot_env} >> ${spot_env_file}
fi
}

check_root

# make spot directories
create_dir ${spot_dir}
create_dir ${spot_bin}

set_env

log_cmd "Spot local setup complete"
