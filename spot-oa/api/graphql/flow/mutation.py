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
            description='Source ip'
        ),
        'dstIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Destination ip'
        ),
        'srcPort': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Source port'
        ),
        'dstPort': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Destination port'
        )
    }
)

ThreatDetailsInputType = GraphQLInputObjectType(
    name='NetflowThreatDetailsInputType',
    fields={
        'firstSeen': GraphQLInputObjectField(
            type=SpotDatetimeType,
            description='First time two ips were seen on one day data of network traffic'
        ),
        'lastSeen': GraphQLInputObjectField(
            type=SpotDatetimeType,
            description='Last time two ips were seen on one day data of network trafic'
        ),
        'srcIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Source ip'
        ),
        'dstIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Destination ip'
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
            description='Number of connections on one day of network traffic'
        ),
        'maxPkts': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Maximum number of packets tranferred on a single connection'
        ),
        'avgPkts': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Average number of packets transferred bwteen ips'
        ),
        'maxBytes': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Maximum number of bytes tranferred on a single connection'
        ),
        'avgBytes': GraphQLInputObjectField(
            type=GraphQLInt,
            description='Average number of bytes transferred bwteen ips'
        )
    }
)

CreateStoryboardInputType = GraphQLInputObjectType(
    name='NetflowCreateStoryboardInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the add comment process. Defaults to today'
        ),
        'ip': GraphQLInputObjectField(
            type=GraphQLNonNull(SpotIpType),
            description='Reference IP for the comment'
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
            type=GraphQLNonNull(GraphQLList(ThreatDetailsInputType)),
        ),
        'first': GraphQLInputObjectField(
            type=GraphQLInt
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

MutationType = GraphQLObjectType(
    name='NetflowMutationType',
    fields={
        'score': GraphQLField(
            type=GraphQLList(SpotOperationOutputType),
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
