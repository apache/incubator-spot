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

def get_random_ip(cidr):
    import ipaddress
    import random
    net = ipaddress.IPv4Network(cidr)
    return net[ random.randint(0, net.num_addresses-1) ]

def get_email():
    import random
    fn = ['noah','emma','mason','ethan','james','madison','daniel','ray','camille','clark','bruce','diana','flash']
    ln = ['smith','gold','hunt','knight','fisher','cook','clark','kent','wayne','prince','gordon']
    dom = ['example.com','outlook.com','skype.com','hotmail.com','yahoo.com','gmail.com','secure.com','cnn.com','nbc.com','news.com']
    email = fn[random.randrange(0, len(fn))] + '.' + ln[random.randrange(0, len(ln))] + '@' + dom[random.randrange(0, len(dom))]
    return email

def get_rcpt(min_=1, max_=3):
    import random
    cnt = random.randint(min_, max_)
    rcpt = [ get_email() for x in range(0, cnt) ]
    return [ ','.join(rcpt), cnt ]
