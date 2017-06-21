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

import os
import sys
import subprocess
import pandas as pd

timeline_path = sys.argv[1]
staging_db_name = sys.argv[2]
staging_table_name = sys.argv[3]
dest_db_name = sys.argv[4]
dest_table_name = sys.argv[5]
dt = sys.argv[6]

dt_year, dt_month, dt_day = dt.split('-')

print dt_year, dt_month, dt_day

filename = os.path.splitext(os.path.basename(timeline_path))[0]
hash_code = filename.split('-')[1]
folder = os.path.dirname(timeline_path)
extsearch_path = "{0}/es-{1}.csv".format(folder, hash_code)

print filename, hash_code, folder, extsearch_path

if os.path.isfile(extsearch_path):

    # Get FullURI from extended search file
    es_df = pd.read_csv(extsearch_path, sep='\t')
    fulluri = es_df.iloc[0]['fulluri']
    print fulluri

    # Load timeline to staging table
    insert_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(timeline_path, staging_db_name, staging_table_name)
    load_to_table_cmd = "hive -e \"{0}\"".format(insert_cmd)
    print load_to_table_cmd
    subprocess.call(load_to_table_cmd, shell=True)

    # Insert into new table from staging table and adding FullURI value
    insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT '{5}', tstart, tend, duration, clientip, respcode, '' FROM {6}.{7} where cast(tstart as timestamp) is not null;".format(dest_db_name, dest_table_name, dt_year, dt_month, dt_day, fulluri, staging_db_name, staging_table_name)
    load_to_table_cmd = "hive -e \"{0}\"".format(insert_cmd)
    print load_to_table_cmd
    subprocess.call(load_to_table_cmd, shell=True)

else:
    print "Extended search file {0} doesn't exist. Timeline on file {1} can't be processed".format(extsearch_path, timeline_path)
