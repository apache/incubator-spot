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
from api.graphql.common import SpotDateType, SpotDatetimeType, SpotIpType, SpotOperationOutputType
import api.resources.flow as Flow

ScoreInputType = GraphQLInputObjectType(
    name='NetflowScoreInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the score process. Defaults to today'
        ),
        'score': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLInt),
            description='A score value, 1->High, 2->Medium, 3->Low'
        ),
        'srcIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Source IP to score'
        ),
        'dstIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Destination IP to score'
        ),
        'srcPort': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Source port to score'
        ),
        'dstPort': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Destination port to score'
        )
    }
)

ThreatDetailsInputType = GraphQLInputObjectType(
    name='NetflowThreatDetailsInputType',
    fields={
        'firstSeen': GraphQLInputObjectField(
            type=SpotDatetimeType,
            description='First time two IPs were seen on a particular day of flow traffic data'
        ),
        'lastSeen': GraphQLInputObjectField(
            type=SpotDatetimeType,
            description='Last time two IPs were seen on a particular day of flow traffic data'
        ),
        'srcIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Source IP address'
        ),
        'dstIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Destination IP address'
        ),
        'srcPort': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Source port'
        ),
        'dstPort': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Destination port'
        ),
        'connections': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Number of connections on a particular day of flow traffic data'
        ),
        'maxPkts': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Maximum number of packets tranferred on a single connection'
        ),
        'avgPkts': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Average number of packets transferred bwteen IPs'
        ),
        'maxBytes': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Maximum number of bytes tranferred on a single connection'
        ),
        'avgBytes': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Average number of bytes transferred bwteen IPs'
        )
    }
)

CreateStoryboardInputType = GraphQLInputObjectType(
    name='NetflowCreateStoryboardInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the storyboard being created. Defaults to today'
        ),
        'ip': GraphQLInputObjectField(
            type=GraphQLNonNull(SpotIpType),
            description='High risk IP address'
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
            type=GraphQLNonNull(GraphQLList(ThreatDetailsInputType)),
            description='Threat details. See NetflowThreatInformation.details'
        ),
        'first': GraphQLInputObjectField(
            type=GraphQLInt,
            description='The number of records to return'
        )
    }
)

def _score_connection(args):
    results = []

    _input = args.get('input')
    for cmd in _input:
        result = Flow.score_connection(
            date=cmd['date'], score=cmd['score'],
            src_ip=cmd.get('srcIp'), src_port=cmd.get('srcPort'),
            dst_ip=cmd.get('dstIp'), dst_port=cmd.get('dstPort')
        )

        results.append({'success': result})

    return results

def _create_storyboard(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    ip = _input.get('ip')
    threat_details = _input.get('threatDetails')
    title = _input.get('title')
    text = _input.get('text')
    first = _input.get('first')

    result = Flow.create_storyboard(date=_date, ip=ip, title=title, text=text, expanded_search=threat_details, top_results=first)

    return {'success': result}


def _reset_scored_connections(args):
    _date = args.get('date', date.today()) 

    result = Flow.reset_scored_connections(date=_date)

    return {'success': result}


MutationType = GraphQLObjectType(
    name='NetflowMutationType',
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
            resolver=lambda root, args, *_: _score_connection(args)
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
