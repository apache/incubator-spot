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

check_dir () {
    if [[ -d "${1}" ]]; then
            log_cmd "${1} already exists"
    else
            log_cmd "Creating ${1}"
            mkdir ${1}
    fi
}

# check for root
if [[ "$EUID" -ne 0 ]]; then

        log_cmd "Non root user detected, exiting now"
        exit 1

fi

# make spot directories
check_dir ${spot_dir}
check_dir ${spot_bin}

# set permissions
log_cmd "Setting permissions on ${spot_dir}"
chown -R ${spot_user}:${spot_user} ${spot_dir}
chmod 0755 ${spot_bin}

# add directory to env
if [[ -f "${spot_env_file}" ]]; then
        log_cmd "${spot_env_file} already exists"
else
        log_cmd "Creating ${spot_env_file}"
        echo ${spot_env} >> ${spot_env_file}
fi

log_cmd "Spot local setup complete"
