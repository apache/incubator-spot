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

        
# end functions

check_os

if [ ! -d ${install_path} ]; then
    log_cmd "${install_path} not created, Please run spot-setup/install.sh first"
    exit 1    
fi

# build
log_cmd 'assembling spot-ml jar'
sbt assembly
cp ./target/scala-2.10/spot-ml-assembly-1.1.jar ${install_path}/jar/
cp ./ml_ops.sh ${install_path}/bin

log_cmd "spot-ml dependencies installed"
cleanup
