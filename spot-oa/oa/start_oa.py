#!/bin/env python

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

import argparse
import os
import sys
sys.path.append("../")
import logging

from utils import Util

# get script path.
script_path = os.path.dirname(os.path.abspath(__file__))

def main():

    # input parameters.
    parser = argparse.ArgumentParser(description="Master OA Script")
    parser.add_argument('-d','--date',required=True,dest='date',help='Date data that will be processed by OA  (i.e 20161102)',metavar='')
    parser.add_argument('-t','--type',required=True,dest='type',help='Data type that will be processed by OA (i.e dns, proxy, flow)',metavar='')
    parser.add_argument('-l','--limit',required=True,dest='limit',help='Num of suspicious connections that will be processed by OA.',metavar='')
    args= parser.parse_args()

    start_oa(args)

def start_oa(args):

    # setup the main logger for all the OA process.
    logger = Util.get_logger('OA',create_file=False)

    logger.info("-------------------- STARTING OA ---------------------")
    validate_parameters_values(args,logger)

    # create data type instance.
    module = __import__("{0}.{0}_oa".format(args.type),fromlist=['OA'])

    # start OA.
    oa_process = module.OA(args.date,args.limit,logger)
    oa_process.start()

def validate_parameters_values(args,logger):

    logger.info("Validating input parameter values")

    #date.
    is_date_ok = True if len(args.date) == 8 else False

    # type
    dirs = os.walk(script_path).next()[1]
    is_type_ok = True if args.type in dirs else False

    #limit
    try:
        int(args.limit)
        is_limit_ok = True
    except ValueError:
        is_limit_ok = False

    if not is_date_ok: logger.error("date parameter is not correct, please validate it")
    if not is_type_ok: logger.error("type parameter is not supported, please select a valid type")
    if not is_limit_ok: logger.error("limit parameter is not correct, please select a valid limit")
    if not is_date_ok or not is_type_ok or not is_limit_ok: sys.exit(1)


if __name__=='__main__':
    main()
