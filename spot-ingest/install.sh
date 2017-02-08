#!/bin/bash

nfdump_vers=1.1
wshark_vers=2.2.3
local_path=`pwd`
source_path=/tmp/ingest_src
install_path=/opt/spot/bin
dependencies=(tar wget screen python)
missing_dep=()
wget_cmd="wget -nc --no-check-certificate"
untar_cmd="tar -xvf"
host_os=""

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

install_tshark () {
    if [ "${host_os}" == "debian" ]; then
        log_cmd "installing dependencies for tshark installation"
        check_bin make bzip2 pkg-config libsmi flex bison byacc
        install_pkg libpcap-dev heimdal-dev libc-ares-dev 
    elif [ "${host_os}" == "rhel" ]; then
        check_bin make bzip2 gcc bison
        install_pkg glib2-devel flex-devel libsmi-devel libpcap-devel
    fi

    ${wget_cmd} https://1.na.dl.wireshark.org/src/wireshark-${wshark_vers}.tar.bz2 -P ${source_path}/
    ${untar_cmd} ${source_path}/wireshark-${wshark_vers}.tar.bz2 -C ${source_path}/
    cd ${source_path}/wireshark-${wshark_vers}
    log_cmd "compiling tshark"
    ./configure --prefix=${install_path} --enable-wireshark=no
    make
    make install
    cd ..

    log_cmd "tshark build complete"
    tshark -v
}

install_nfdump () {
    log_cmd "installing spot-nfdump"
    ${wget_cmd} https://github.com/Open-Network-Insight/spot-nfdump/archive/${nfdump_vers}.tar.gz -P ${source_path}/
    ${untar_cmd} ${source_path}/${nfdump_vers}.tar.gz -C ${source_path}/
    cd ${source_path}/spot-nfdump-*/
    source ./install_nfdump.sh ${install_path}
    cd ${local_path}
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
    log_cmd "${source_path} not created, Please run spot-setup/local_setup.sh first"
    exit 1    
fi

# check basic dependencies
check_bin ${dependencies[@]}
install_pkg

if type tshark >/dev/null 2>&1; then
        log_cmd "tshark found"
    else
        log_cmd "tshark missing"
        install_tshark
fi

if type nfdump >/dev/null 2>&1; then
    log_cmd "nfdump found"
else
    log_cmd "missing nfdump"
    install_nfdump
fi

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

log_cmd "spot-ingest dependencies installed"
cleanup
