from datetime import date
from graphql import (
    GraphQLObjectType,
    GraphQLField,
    GraphQLArgument,
    GraphQLString,
    GraphQLInt,
    GraphQLBoolean,
    GraphQLNonNull,
    GraphQLInputObjectType,
    GraphQLInputObjectField
)

from api.graphql.common import SpotDateType, SpotIpType, SpotOperationOutputType
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

AddCommentInputType = GraphQLInputObjectType(
    name='NetflowAddCommentInputType',
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

def _add_comment(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    ip = _input.get('ip')
    title = _input.get('title')
    text = _input.get('text')

    if Flow.save_comment(date=_date, ip=ip, title=title, text=text) is None:
        return {'success':True}
    else:
        return {'success':False}

MutationType = GraphQLObjectType(
    name='NetflowMutationType',
    fields={
        'score': GraphQLField(
            type=SpotOperationOutputType,
            args={
                'input': GraphQLArgument(
                    type=SimpleScoreInputType,
                    description='Score criteria'
                )
            },
            resolver=lambda root, args, *_: _score_connection(args)
        ),
        'addComment': GraphQLField(
            type=SpotOperationOutputType,
            args={
                'input': GraphQLArgument(
                    type=AddCommentInputType,
                    description='Comment info'
                )
            },
            resolver=lambda root, args, *_: _add_comment(args)
        )
    }
)
