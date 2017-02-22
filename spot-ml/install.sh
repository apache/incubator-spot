#!/bin/bash

local_path=`pwd`
source_path=/tmp/ml_src
install_path=/opt/spot/
dependencies=(tar wget)
missing_dep=()
wget_cmd="wget -nc --no-check-certificate"
untar_cmd="tar -xvf"
host_os=""
sbt_tar='https://dl.bintray.com/sbt/native-packages/sbt/0.13.13/sbt-0.13.13.tgz'


# functions

log_cmd () {

    printf "\n****SPOT.ML.Install.sh****\n"
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

sbt_install () {
    log_cmd 'installing sbt for ${host_os}'    
    if [[ ${1} == 'debian' ]]; then
            echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
            sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
            sudo apt-get update
            sudo apt-get install sbt
    elif [[ ${1} == 'rhel']]; then
            curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
            sudo yum -y install sbt
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

if [ ! -d ${install_path} ]; then
    log_cmd "${install_path} not created, Please run spot-setup/local_setup.sh first"
    exit 1    
fi

# check basic dependencies
check_bin ${dependencies[@]}
install_pkg

# install
if type sbt >/dev/null 2>&1; then
        log_cmd "sbt found"
else
        sbt_install ${host_os}
fi

# build
log_cmd 'assembling spot-ml jar'
sbt assembly
cp ./target/scala-2.10/spot-ml-assembly-1.1.jar ${install_path}/jar/
cp ./ml_ops.sh ${install_path}/bin

log_cmd "spot-ml dependencies installed"
cleanup
