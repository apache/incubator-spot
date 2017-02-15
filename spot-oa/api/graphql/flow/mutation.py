from datetime import date
from graphql import (
    GraphQLObjectType,
    GraphQLField,
    GraphQLArgument,
    GraphQLInt,
    GraphQLBoolean,
    GraphQLNonNull,
    GraphQLInputObjectType,
    GraphQLInputObjectField
)

from api.graphql.common import SpotDateType, SpotIpType
from api.resources.flow import Flow

SimpleScoreInputType = GraphQLInputObjectType(
    name='NetflowSimpleScoreInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the score process. Defaults to today'
        ),
        'score': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLInt),
            description='A detailed search criteria for the score process'
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

QuickScoreInputType = GraphQLInputObjectType(
    name='NetflowQuickScoreInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the score process. Defaults to today'
        ),
        'score': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLInt),
            description='A detailed search criteria for the score process'
        ),
        'ip': GraphQLInputObjectField(
            type=GraphQLNonNull(SpotIpType),
            description='Ip to look for'
        )
    }
)

ScoreOutputType = GraphQLObjectType(
    name='NetflowScoreOutputType',
    fields={
        'success': GraphQLField(
            type=GraphQLBoolean,
            description='True after connections have been scored',
            resolver=lambda root, *_: root.get('success')
        )
    }
)

def _score_connection(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    score = _input.get('score')
    srcIp = _input.get('srcIp')
    dstIp = _input.get('dstIp')
    srcPort = _input.get('srcPort')
    dstPort = _input.get('dstPort')

    if Flow.score_connection(date=_date, score=score, Ipsrc=srcIp, Ipdst=dstIp, srcport=srcPort, dstport=dstPort) is None:
        return {'success':True}
    else:
        return {'success':False}

MutationType = GraphQLObjectType(
    name='NetflowMutationType',
    fields={
        'score': GraphQLField(
            type=ScoreOutputType,
            args={
                'input': GraphQLArgument(
                    type=SimpleScoreInputType,
                    description='Score criteria'
                )
            },
            resolver=lambda root, args, *_: _score_connection(args)
        )
    }
)
