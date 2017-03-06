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
            type=SpotDatetimeType
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType
        ),
        'username': GraphQLInputObjectField(
            type=GraphQLString
        ),
        'duration': GraphQLInputObjectField(
            type=GraphQLInt
        ),
        'uri': GraphQLInputObjectField(
            type=GraphQLString
        ),
        'webCategory': GraphQLInputObjectField(
            type=GraphQLString
        ),
        'responseCode': GraphQLInputObjectField(
            type=GraphQLInt
        ),
        'requestMethod': GraphQLInputObjectField(
            type=GraphQLString,
            description='Http Method'
        ),
        'userAgent': GraphQLInputObjectField(
            type=GraphQLString,
            description='Client\'s user agent'
        ),
        'responseContentType': GraphQLInputObjectField(
            type=GraphQLString
        ),
        'referer': GraphQLInputObjectField(
            type=GraphQLString
        ),
        'uriPort': GraphQLInputObjectField(
            type=GraphQLInt
        ),
        'serverIp': GraphQLInputObjectField(
            type=SpotIpType
        ),
        'serverToClientBytes': GraphQLInputObjectField(
            type=GraphQLInt
        ),
        'clientToServerBytes': GraphQLInputObjectField(
            type=GraphQLInt
        )
    }
)

CreateStoryboardInputType = GraphQLInputObjectType(
    name='ProxyCreateStoryboardInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the add comment process. Defaults to today'
        ),
        'uri': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='Reference URI for the comment'
        ),
        'title': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='A title for the comment'
        ),
        'text': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLString),
            description='A description text for the comment'
        ),
        'threatDetails': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLList(GraphQLNonNull(ThreatDetailsInputType))),
        ),
        'first': GraphQLInputObjectField(
            type=GraphQLInt
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

MutationType = GraphQLObjectType(
    name='ProxyMutationType',
    fields={
        'score': GraphQLField(
            type=GraphQLList(SpotOperationOutputType),
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
                    description='Generates every data needed to move a threat to the storyboard'
                )
            },
            resolver=lambda root, args, *_: _create_storyboard(args)
        )
    }
)
