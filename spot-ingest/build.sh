#!/bin/bash

nfdump_vers=1.1
wshark_vers=2.2.3
local_path=`pwd`
source_path=/tmp/ingest_src
install_path=/opt/spot/
missing_dep=()
wget_cmd="wget -nc --no-check-certificate"
untar_cmd="tar -xvf"
host_os=""
mk_opt="-j `nproc`"

# functions

log_cmd () {
    printf "\n****SPOT.INGEST.build.sh****\n"
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

cleanup () {
    log_cmd "executing cleanup"
    rm -rf ${source_path}
}

install_tshark () {
    ${wget_cmd} https://1.na.dl.wireshark.org/src/wireshark-${wshark_vers}.tar.bz2 -P ${source_path}/
    ${untar_cmd} ${source_path}/wireshark-${wshark_vers}.tar.bz2 -C ${source_path}/
    cd ${source_path}/wireshark-${wshark_vers}
    log_cmd "compiling tshark"
    ./configure --prefix=${install_path} --enable-wireshark=no
    make ${mk_opt} 
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

check_os

if [ ! -d ${source_path} ]; then
        mkdir ${source_path}
fi

if [ ! -d ${install_path} ]; then
        log_cmd "${install_path} not created, Please run spot-setup/local_setup.sh first"
        exit 1        
fi

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

if [ -z ${local_path}/requirements.txt ]; then
    pip install -r requirements.txt
fi

log_cmd "spot-ingest dependencies installed"
cleanup
