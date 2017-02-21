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

def _score_record(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    score = _input.get('score')
    dnsQuery = _input.get('dnsQuery')
    clientIp = _input.get('clientIp')

    if Dns.dns_score(date=_date, score=score, dnsQuery=dnsQuery, clientIp=clientIp) is None:
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
        )
    }
)
