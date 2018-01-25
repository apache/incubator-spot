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
    GraphQLList,
    GraphQLString,
    GraphQLInt,
    GraphQLNonNull,
    GraphQLInputObjectType,
    GraphQLInputObjectField
)

from api.graphql.common import SpotDateType, SpotDatetimeType, SpotIpType, SpotOperationOutputType
import api.resources.proxy as Proxy

ScoreInputType = GraphQLInputObjectType(
    name='ProxyScoreInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the score process. Defaults to today'
        ),
        'score': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLInt),
            description='A score value, 1->High, 2->Medium, 3->Low'
        ),
        'uri': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='Full URI'
        )
    }
)

ThreatDetailsInputType = GraphQLInputObjectType(
    name='ProxyThreatDetailsInputType',
    fields={
        'datetime': GraphQLInputObjectField(
            type=SpotDatetimeType,
            description='Start time of the request'
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Client\'s IP address'
        ),
        'username': GraphQLInputObjectField(
            type=GraphQLString,
            description='Username used for authetication'
        ),
        'duration': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Connection duration'
        ),
        'uri': GraphQLInputObjectField(
            type=GraphQLString,
            description='The original URI requested'
        ),
        'webCategory': GraphQLInputObjectField(
            type=GraphQLString,
            description='Web content categories'
        ),
        'responseCode': GraphQLInputObjectField(
            type=GraphQLInt,
            description='HTTP response code'
        ),
        'requestMethod': GraphQLInputObjectField(
            type=GraphQLString,
            description='HTTP request method'
        ),
        'userAgent': GraphQLInputObjectField(
            type=GraphQLString,
            description='Client\'s user agent'
        ),
        'responseContentType': GraphQLInputObjectField(
            type=GraphQLString,
            description='HTTP response content type (MIME)'
        ),
        'referer': GraphQLInputObjectField(
            type=GraphQLString,
            description='The address of the webpage that linked to the resource being requested'
        ),
        'uriPort': GraphQLInputObjectField(
            type=GraphQLInt,
            description='URI port'
        ),
        'serverIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='The address of the webpage that linked to the resource being requested'
        ),
        'serverToClientBytes': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Number of bytes sent from appliance to client'
        ),
        'clientToServerBytes': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Number of bytes sent from client to appliance'
        )
    }
)

CreateStoryboardInputType = GraphQLInputObjectType(
    name='ProxyCreateStoryboardInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the storyboard being created. Defaults to today'
        ),
        'uri': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='Threat UI'
        ),
        'title': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='Threat title'
        ),
        'text': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='Threat description'
        ),
        'threatDetails': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLList(GraphQLNonNull(ThreatDetailsInputType))),
            description='Threat details. See ProxyThreatInformation.details'
        ),
        'first': GraphQLInputObjectField(
            type=GraphQLInt,
            description='The number of records to return'
        )
    }
)

def _score_connections(args):
    results = []

    _input = args.get('input')
    for cmd in _input:
        _date = cmd.get('date', date.today())
        score = cmd.get('score')
        uri = cmd.get('uri')

        result = Proxy.score_request(date=_date, score=score, uri=uri)

        results.append({'success': result})

    return results
 

def _create_storyboard(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    uri = _input.get('uri')
    title = _input.get('title')
    text = _input.get('text')
    threat_details = _input.get('threatDetails')
    first = _input.get('first')

    result = Proxy.create_storyboard(date=_date, uri=uri, title=title, text=text, expanded_search=threat_details, top_results=first)

    return {'success': result}


def _reset_scored_connections(args):
    _date = args.get('date', date.today()) 

    result = Proxy.reset_scored_connections(date=_date)

    return {'success': result}


MutationType = GraphQLObjectType(
    name='ProxyMutationType',
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
            resolver=lambda root, args, *_: _score_connections(args)
        ),
        'createStoryboard': GraphQLField(
            type=SpotOperationOutputType,
            args={
                'input': GraphQLArgument(
                    type=GraphQLNonNull(CreateStoryboardInputType),
                    description='Request Spot to create an entry on storyboard for a particular threat'
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
