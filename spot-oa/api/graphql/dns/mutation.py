from datetime import date
from graphql import (
    GraphQLObjectType,
    GraphQLField,
    GraphQLArgument,
    GraphQLString,
    GraphQLInt,
    GraphQLNonNull,
    GraphQLInputObjectType,
    GraphQLInputObjectField
)

from api.graphql.common import SpotDateType, SpotIpType, SpotOperationOutputType
from api.resources.dns import Dns

ScoreInputType = GraphQLInputObjectType(
    name='DnsScoreType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the scoring process. Defaults to today'
        ),
        'score': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLInt),
            description='A detailed search criteria for the score process'
        ),
        'dnsQuery': GraphQLInputObjectField(
            type=GraphQLString,
            description='Dns query to score'
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Client\'s ip to score'
        )
    }
)

AddCommentInputType = GraphQLInputObjectType(
    name='DnsAddCommentInputType',
    fields={
        'date': GraphQLInputObjectField(
            type=SpotDateType,
            description='A reference date for the add comment process. Defaults to today'
        ),
        'dnsQuery': GraphQLInputObjectField(
            type=GraphQLString,
            description='Reference dns query for the comment'
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Reference client ip for the comment'
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

def _score_record(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    score = _input.get('score')
    dns_query = _input.get('dnsQuery')
    client_ip = _input.get('clientIp')

    if Dns.dns_score(date=_date, score=score, dns_query=dns_query, client_ip=client_ip) is None:
        return {'success':True}
    else:
        return {'success':False}

def _add_comment(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    dns_query = _input.get('dnsQuery')
    client_ip = _input.get('clientIp')
    title = _input.get('title')
    text = _input.get('text')

    if Dns.save_comment(date=_date, dns_query=dns_query, client_ip=client_ip, title=title, text=text) is None:
        return {'success':True}
    else:
        return {'success':False}

MutationType = GraphQLObjectType(
    name='DnsMutationType',
    fields={
        'score': GraphQLField(
            type=SpotOperationOutputType,
            args={
                'input': GraphQLArgument(
                    type=GraphQLNonNull(ScoreInputType),
                    description='Score criteria'
                )
            },
            resolver=lambda root, args, *_: _score_record(args)
        ),
        'addComment': GraphQLField(
            type=SpotOperationOutputType,
            args={
                'input': GraphQLArgument(
                    type=GraphQLNonNull(AddCommentInputType),
                    description='Comment info'
                )
            },
            resolver=lambda root, args, *_: _add_comment(args)
        )
    }
)
