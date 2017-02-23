#!/bin/bash

local_path=`pwd`
source_path=/tmp/ingest_src
install_path=/opt/spot/
dependencies=(tar wget screen python make gcc m4 automake autoconf flex byacc)
missing_dep=()
host_os=""

# functions

log_cmd () {
        printf "\n****SPOT.INGEST.install.sh****\n"
        date +"%y-%m-%d %H:%M:%S"
        printf "$1\n\n"
}

check_os () {
        # detect distribution
        # to add other distributions simply create a test case with installation commands
        if [ -f /etc/redhat-release ]; then
                install_cmd="yum -y install"
                log_cmd "installation command: $install_cmd"
                host_os="rhel"
        elif [ -f /etc/debian_version ]; then
                install_cmd="apt-get install -yq"
                log_cmd "installation command: $install_cmd"
                host_os="debian"
                apt-get update
        fi
}

check_root () {
        # checking for root as many of these functions interact with system owned directories
        if [[ "$EUID" -ne 0 ]]; then
                log_cmd "Non root user detected, Please run as root or with sudo"
                exit 1
        fi  
}

check_bin () {
            # check_bin can be used to verify if a certain binary is already installed
            for item in "$@"; do
                    if type ${item} >/dev/null 2>&1; then
                            log_cmd "${item} found"
                    else
                            missing_dep+=(${item})
                    fi  
            done
    }

install_pkg () {
        # if no parameters this will simply install any $missing_deps
        # if any parameters provided they will be added to $missing_dep
        if [[ "$@" ]]; then
                for item in "$@"; do
                        missing_dep+=(${item})
                done
        fi  

        if [[ "${missing_dep[@]}" ]]; then
                log_cmd "installing ${missing_dep[@]}"
                ${install_cmd} ${missing_dep[@]}
                unset missing_dep[*]
        fi  
}

check_tshark () {
        # check dependencies only, installs in custom location
        log_cmd "installing dependencies for tshark installation"
        if [ "${host_os}" == "debian" ]; then
                check_bin make bzip2 pkg-config libsmi flex bison byacc
                install_pkg libpcap-dev heimdal-dev libc-ares-dev
        elif [ "${host_os}" == "rhel" ]; then
                check_bin make bzip2 gcc bison
                install_pkg glib2-devel flex-devel libsmi-devel libpcap-devel
        fi
}

install_pip () {
        if type pip >/dev/null 2>&1; then\
                log_cmd "pip found"
        else    
                log_cmd "missing pip"
                ${wget_cmd} https://bootstrap.pypa.io/get-pip.py -P ${source_path}/
                python ${source_path}/get-pip.py
                log_cmd "pip installed"
        fi
}
# end functions

check_os

# check basic dependencies
check_bin ${dependencies[@]}
install_pkg
check_tshark
install_pip

log_cmd "dependencies satisfied, please run ./build.sh to complete setup"
