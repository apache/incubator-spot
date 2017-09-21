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
from datetime import date
from graphql import (
    GraphQLObjectType,
    GraphQLField,
    GraphQLArgument,
    GraphQLString,
    GraphQLInt,
    GraphQLNonNull,
    GraphQLList,
    GraphQLInputObjectType,
    GraphQLInputObjectField
)

from api.graphql.common import SpotDateType, SpotIpType, SpotOperationOutputType
import api.resources.dns as Dns

ScoreInputType = GraphQLInputObjectType(
    name='DnsScoreType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the scoring process. Defaults to today'
        ),
        'score': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLInt),
            description='A score value, 1->High, 2->Medium, 3->Low'
        ),
        'dnsQuery': GraphQLInputObjectField(
            type=GraphQLString,
            description='Dns query name to score'
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Client IP to score'
        )
    }
)

ThreatDetailsInputType = GraphQLInputObjectType(
    name='DnsThreatDetailsInputType',
    fields={
        'total': GraphQLInputObjectField(
            type=GraphQLInt,
            description='The number of time an IP sent a dns query'
        ),
        'dnsQuery': GraphQLInputObjectField(
            type=GraphQLString,
            description='DNS query name'
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Client IP address'
        )
    }
)

CreateStoryboardInputType = GraphQLInputObjectType(
    name='DnsCreateStoryboardInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the storyboard being created. Defaults to today'
        ),
        'dnsQuery': GraphQLInputObjectField(
            type=GraphQLString,
            description='Threat dns query name'
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Threat client IP'
        ),
        'title': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='Threat title'
        ),
        'text': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='Threat title description'
        ),
        'threatDetails': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLList(GraphQLNonNull(ThreatDetailsInputType))),
            description='Threat details. See DnsThreatInformation.details'
        )
    }
)

def _score_records(args):
    results = []

    _input = args.get('input')
    for cmd in _input:
        _date = cmd.get('date', date.today())
        dns_query = cmd.get('dnsQuery', '')
        client_ip = cmd.get('clientIp', '')
        query_score = cmd.get('score') if dns_query else 0
        client_ip_score = cmd.get('score') if client_ip else 0

        result = Dns.score_connection(date=_date, dns=dns_query, ip=client_ip, dns_sev=query_score, ip_sev=client_ip_score)

        results.append({'success': result})

    return results

def _create_storyboard(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    dns_query = _input.get('dnsQuery', '')
    client_ip = _input.get('clientIp', '')
    threat_details = _input.get('threatDetails')
    title = _input.get('title')
    text = _input.get('text')

    result = Dns.create_storyboard(
        date=_date, query=dns_query, ip=client_ip,
        title=title, text=text,
        expanded_search=threat_details)

    return {'success': result}


def _reset_scored_connections(args):
    _date = args.get('date', date.today()) 

    result = Dns.reset_scored_connections(date=_date)

    return {'success': result}


MutationType = GraphQLObjectType(
    name='DnsMutationType',
    fields={
        'score': GraphQLField(
            type=GraphQLList(SpotOperationOutputType),
            description='Sets a score value to connections',
            args={
                'input': GraphQLArgument(
                    type=GraphQLNonNull(GraphQLList(GraphQLNonNull(ScoreInputType))),
                    description='Score criteria'
                )
            },
            resolver=lambda root, args, *_: _score_records(args)
        ),
        'createStoryboard': GraphQLField(
            type=SpotOperationOutputType,
            description='Request Spot to create an entry on storyboard for a particular threat',
            args={
                'input': GraphQLArgument(
                    type=GraphQLNonNull(CreateStoryboardInputType),
                    description='Threat information'
                )
            },
            resolver=lambda root, args, *_: _create_storyboard(args)
        ),
        'resetScoredConnections': GraphQLField(
            type=SpotOperationOutputType,
            description='Resets all scored connections for a certain day',
            args={
                'date': GraphQLArgument(
                    type=GraphQLNonNull(SpotDateType),
                    description='Date to clean'
                )
            },
            resolver=lambda root, args, *_: _reset_scored_connections(args)
        )
    }
)
