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
from graphql import (
    GraphQLSchema,
    GraphQLObjectType,
    GraphQLField
)

from flow import QueryType as NetflowQueryType, MutationType as NetflowMutationType, TYPES as NetflowTypes
from dns import QueryType as DnsQueryType, MutationType as DnsMutationType, TYPES as DnsTypes
from proxy import QueryType as ProxyQueryType, MutationType as ProxyMutationType, TYPES as ProxyTypes

SpotSchema = GraphQLSchema(
  query=GraphQLObjectType(
    name='SpotQueryType',
    fields={
      'flow': GraphQLField(
        type=NetflowQueryType,
        description='Flow is a network protocol that collects IP traffic information and monitors network traffic',
        resolver=lambda *_: {}
      ),
      'dns': GraphQLField(
        type=DnsQueryType,
        description='Domain Name System (DNS) Log Records contains the requests in between clients and DNS servers',
        resolver=lambda *_: {}
      ),
      'proxy': GraphQLField(
        type=ProxyQueryType,
        description='Proxy Logs contains the requests in between clients and Proxy servers',
        resolver=lambda *_: {}
      )
    }
  ),
  mutation=GraphQLObjectType(
    name='SpotMutationType',
    fields={
        'flow': GraphQLField(
            type=NetflowMutationType,
            description='Flow related mutation operations',
            resolver=lambda *_: {}
        ),
        'dns': GraphQLField(
            type=DnsMutationType,
            description='DNS related mutation operations',
            resolver=lambda *_: {}
        ),
        'proxy': GraphQLField(
            type=ProxyMutationType,
            description='Proxy related mutation operations',
            resolver=lambda *_: {}
        )
    }
  ),
  types=NetflowTypes + DnsTypes + ProxyTypes
)
