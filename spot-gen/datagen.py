#!/usr/bin/python3

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
import re
import yaml
from datetime import timedelta, datetime
import time
import random


class Generator:
    def __init__(self, config):
        self.set_default_values()
        self.config = config
        self.cache = {}
        self.load_config()

    def set_default_values(self):
        self.timeformat = "%Y-%m-%d %H:%M:%S"
        self.linebreak = "\n"
        self.model = {}
        self.templates = []
        self.replaces = {}

    def load_config(self):
        if 'timeformat' in self.config:
            self.timeformat = self.config['timeformat']
        if 'linebreak' in self.config:
            self.linebreak = self.config['linebreak']

        if 'model' in self.config:
            self.model['users'] = self.load_yaml_from_file(self.config['model']['users'])
            self.model['events'] = self.load_yaml_from_file(self.config['model']['events'])
            self.model['scenarios'] = self.load_yaml_from_file(self.config['model']['scenarios'])
            self.model['activity'] = self.load_yaml_from_file(self.config['model']['activity'])
            return
        for tmpl in self.config['templates']:
            if tmpl['type'] == 'file':
                tmpl['samples'] = self.load_samples_from_file(tmpl['samples'])
            self.templates.append(tmpl)
        for rplc in self.config['replaces']:
            if type(rplc[1]) is str:
                rplc[1] = self.load_samples_from_file(rplc[1])
            self.replaces[rplc[0]] = rplc[1:]

    def load_samples_from_file(self, fname):
        if not os.path.isabs(fname):
            fname = os.path.join(self.config['conf_abs_path'], fname)
        with open(fname, 'r') as f:
            return f.read().splitlines()

    def load_yaml_from_file(self, fname):
        if not os.path.isabs(fname):
            fname = os.path.join(self.config['conf_abs_path'], fname)
        with open(fname, 'r') as f:
            return yaml.load(f.read())

    def do_replace(self, line, replaces):
        regexp = '(' + '|'.join(replaces.keys()) + ')'
        actual_keys = re.findall(regexp, line)
        already_replaced = []
        for key_to_replace in actual_keys:
            replace_to = ''
            if key_to_replace in already_replaced:
                continue
            if key_to_replace in replaces:
                values = replaces[key_to_replace][0]
                if callable(values):
                    replace_to = values(*replaces[key_to_replace][1])
                else:
                    replace_to = values[random.randint(0, len(values)-1)]
            else:
                replace_to = 'DEFINE_ME_IN_CONFIG'
            if key_to_replace.startswith('%_'):
                line = line.replace(key_to_replace, str(replace_to), 1)
            else:
                line = line.replace(key_to_replace, str(replace_to))
                already_replaced.append(key_to_replace)

        return line

    def generate(self, odf, beg_date, end_date):
        if self.model:
            self.generate_model(odf, beg_date, end_date)
        days = (end_date - beg_date).days
        if days > 0:
            for day in range(days, 0, -1):
                base_day = end_date - timedelta(days=day)
                end_of_period = base_day + timedelta(days=1) - timedelta(seconds=1)
                self._generate_for_period(odf, base_day, end_of_period)
        elif days == 0:
            self._generate_for_period(odf, beg_date, end_date)

    def generate_model(self, odf, beg_date, end_date):
        days = (end_date - beg_date).days
        if days > 0:
            for day in range(days, 0, -1):
                base_day = end_date - timedelta(days=day)
                end_of_period = base_day + timedelta(days=1) - timedelta(seconds=1)
                for user in self.model['activity']:
                    scenario = self.model['scenarios'][self.model['activity'][user]]
                    for event in scenario:
                        self._generate_event(base_day, self.model['users'][user], event, self.model['events'])
                self.cache = {}

        elif days == 0:
            self._generate_for_period(odf, beg_date, end_date)

    def _generate_event(self, dt, user, event, template):
        weekday = dt.weekday()
        weekday = '{:%a}'.format(dt)
        if weekday not in event['weekdays'].split(','):
            return
        mintime, maxtime = event['timerange'].split('-')
        mintime_h, mintime_m = mintime.split(':')
        maxtime_h, maxtime_m = maxtime.split(':')
        mintime_h = int(mintime_h)
        maxtime_h = int(maxtime_h)
        hours = maxtime_h - mintime_h
        minutes = 60 * hours
        start = 0
        stop = 1
        if 'frequency' in event:
            (start, stop) = event['frequency']

        def repl(matchobj):
            for m in matchobj.groups():
                (typ, key) = m.split(':')
                if typ == 'U':
                    return user[key]
                elif typ == 'M':
                    print(key)
                    login = user['Login']
                    if login not in self.cache:
                        self.cache[login] = {}
                    if key in self.cache[login]:
                        return self.cache[login][key]
                    else:
                        self.cache[login][key] = str(random.randrange(1000, 9000))
                        return self.cache[login][key]
                else:
                    return 'DEFINE_ME'

        for i in range(start, stop):
            event_time = dt + timedelta(hours=mintime_h, minutes=random.randrange(0, minutes), seconds=random.randrange(0, 60))
            ts = event_time.strftime(self.timeformat) + ' ' + user['Timezone']
            s = re.sub(r'<<([^>]+)>>', repl, template[event['tmpl']])
            s = s.replace('%TS%', ts)
            print(s)

    def _generate_for_period(self, odf, beg_of_period, end_of_period):
        ready_events = []
        for tmpl in self.templates:
            beg = beg_of_period
            end = beg + timedelta(seconds=tmpl['period'])
            while end < end_of_period:
                for sample in tmpl['samples']:
                    msg_cnt = random.randint(tmpl['min'], tmpl['max'])
                    for i in range(0, msg_cnt):
                        ts = random.randint(time.mktime(beg.timetuple()), time.mktime(end.timetuple()))
                        result = self.do_replace(sample, self.replaces)
                        ready_events.append([ts, result])
                beg = end
                end = beg + timedelta(seconds=tmpl['period'])
        if self.config['sort']:
            ready_events.sort()
        for line in ready_events:
            res = line[1].replace('%TS%', time.strftime(self.timeformat, time.localtime(line[0])))
            odf.write(res + self.linebreak)


def main():
    parser = argparse.ArgumentParser(description='Generate sample data')
    parser.add_argument('config', type=argparse.FileType('r'), help='Config file')
    parser.add_argument('--write', type=argparse.FileType('w'), default='-', help='Write to file')
    parser.add_argument('--period', default='1h', help='Generate data for N last days [suffix d] or last N hours [suffix h] or last N minutes [suffix m]')
    parser.add_argument('--sort', action='store_true', default=False, help='Use timestamp to sort events (slow)')
    args = parser.parse_args()

    config = {}
    if args.config:
        conf_abs_path = os.path.dirname(os.path.abspath(args.config.name))
        sys.path.append(conf_abs_path)
        config = yaml.load(args.config)
        config['conf_abs_path'] = conf_abs_path
        config['sort'] = args.sort

    matches = re.match('(\d+)([dhm])', args.period)
    if not matches:
        parser.print_help()

    period = int(matches.group(1))
    period_type = matches.group(2)
    end_date = datetime.now()
    if period_type == 'h':
        beg_date = end_date - timedelta(hours=period)
    elif period_type == 'm':
        beg_date = end_date - timedelta(minutes=period)
    if period_type == 'd':
        end_date = end_date.replace(hour=0, minute=0, second=0, microsecond=0)
        beg_date = end_date - timedelta(days=period)

    g = Generator(config)
    g.generate(args.write, beg_date, end_date)


if __name__ == '__main__':
    main()
