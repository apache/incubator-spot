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
import re
import subprocess
import pandas as pd
import datetime

filepath = sys.argv[1]
source_db_name = sys.argv[2]
source_table_name = sys.argv[3]
dest_db_name = sys.argv[4]
dest_table_name = sys.argv[5]

# filepath = '~/spot-csv-data/flow/ingest_summary/is_201610.csv'
# source_db_name = 'spot_migration'
# source_table_name = 'flow_ingest_summary_tmp'
# dest_db_name = 'migrated'
# dest_table_name = 'flow_ingest_summary'

df = pd.read_csv(filepath)

s = df.iloc[:,0]
l_dates = list(s.unique())
l_dates = map(lambda x: x[0:10].strip(), l_dates)
l_dates = filter(lambda x: re.match('\d{4}[-/]\d{2}[-/]\d{1}', x), l_dates)
s_dates = set(l_dates)

for date_str in s_dates:
    date_dt = datetime.datetime.strptime(date_str, '%Y-%m-%d')
    print 'Processing day: ', date_str, date_dt.year, date_dt.month, date_dt.day

    records = df[df['date'].str.contains(date_str)]
    filename = "ingest_summary_{0}{1}{2}.csv".format(date_dt.year, date_dt.month, date_dt.day)
    records.to_csv(filename, index=False)

    insert_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, source_db_name, source_table_name)
    load_to_table_cmd = "hive -e \"{0}\"".format(insert_cmd)
    print load_to_table_cmd
    subprocess.call(load_to_table_cmd, shell=True)

    insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT tdate, total FROM {5}.{6}".format(dest_db_name, dest_table_name, date_dt.year, date_dt.month, date_dt.day, source_db_name, source_table_name)
    load_to_table_cmd = "hive -e \"{0}\"".format(insert_cmd)
    print load_to_table_cmd
    subprocess.call(load_to_table_cmd, shell=True)

    os.remove(filename)

