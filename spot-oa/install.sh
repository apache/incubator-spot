#!/bin/bash

local_path=`pwd`
source_path=/tmp/oa_src
install_path=/opt/spot/
dependencies=(tar wget screen python)
missing_dep=()
wget_cmd="wget -nc --no-check-certificate"
untar_cmd="tar -xvf"
host_os=""
num_procs=$(nproc)

# functions

log_cmd () {

    printf "\n****SPOT.INGEST.Install.sh****\n"
    date +"%y-%m-%d %H:%M:%S"
    printf "$1\n\n"
}

check_root () {
    # checking for root as many of these functions interact with system owned directories
    if [[ "$EUID" -ne 0 ]]; then
            log_cmd "Non root user detected, Please run as root or with sudo"
            exit 1
    fi
}

cleanup () {
    log_cmd "executing cleanup"
    rm -rf ${source_path}
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

npm_install () {

        log_cmd 'installing NodeJS 7 for ${host_os}'    
        if [[ ${1} == 'debian' ]]; then
                curl -sL https://deb.nodesource.com/setup_7.x | sudo -E bash -
                sudo apt-get install -y nodejs

        elif [[ ${1} == 'rhel']]; then
                curl -sL https://rpm.nodesource.com/setup_7.x | bash -
                yum install -y nodejs
        fi  
}
# end functions

check_root

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

if [ ! -d ${source_path} ]; then
        mkdir ${source_path}
fi

if [ ! -d ${install_path} ]; then
    log_cmd "${install_path} not created, Please run spot-setup/local_setup.sh first"
    exit 1    
fi

# check basic dependencies
check_bin ${dependencies[@]}
install_pkg

if type pip >/dev/null 2>&1; then
    log_cmd "pip found"
else
    log_cmd "missing pip"
    ${wget_cmd} https://bootstrap.pypa.io/get-pip.py -P ${source_path}/
    python ${source_path}/get-pip.py
    log_cmd "pip installed"
fi

if [ -z ${local_path}/requirements.txt ]; then
    pip install -r requirements.txt
fi

npm_install
cd ui
npm install

log_cmd "spot-ingest dependencies installed"
cleanup
