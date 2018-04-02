#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

HOME_DIR=`pwd`

abort()
{
    echo
    echo "-----------------------------------------------------------"
    echo "      ERROR: Unable to install distributed collector.      "
    echo
    exit 2
}

clear

# .....................................install package `virtualenv`
if type virtualenv > /dev/null 2>&1; then
    printf "\n *  Package 'virtualenv' is already installed!"
else
    printf "\n +  Installing package 'virtualenv'...\n"
    sudo apt-get -y install python-virtualenv
    [ ! $? -eq 0 ] && abort
fi

# .....................................install package `zip`
if type zip > /dev/null 2>&1; then
    printf "\n *  Package 'zip' is already installed!"
else
    printf "\n +  Installing package 'zip'...\n"
    sudo apt-get -y install zip
    [ ! $? -eq 0 ] && abort
fi

printf "\n  |- Create virtual environment without any dependencies...\n"
virtualenv --no-site-packages venv
[ ! $? -eq 0 ] && abort

printf "  |-- Enable virtual environment."
source venv/bin/activate
[ ! $? -eq 0 ] && abort

printf "\n  |-- Install Python packages...\n"
pip install -r streaming-requirements.txt
[ ! $? -eq 0 ] && abort

printf "\n  |-- Build code and create egg file.\n"
python setup.py bdist_egg
[ ! $? -eq 0 ] && abort

rm -r build/ pipelines.egg-info/
printf "\n  |-- Make avro.zip file.\n"

cd venv/lib/python2.7/site-packages/
zip ${HOME_DIR}/dist/avro.zip avro -r
[ ! $? -eq 0 ] && abort

echo
echo "***********************************************************"
echo "*           Installation completed successfully           *"
echo "***********************************************************"
echo
