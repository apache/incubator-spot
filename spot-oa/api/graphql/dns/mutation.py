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
            description='Dns query to score'
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Client\'s ip to score'
        )
    }
)

ThreatDetailsInputType = GraphQLInputObjectType(
    name='DnsThreatDetailsInputType',
    fields={
        'total': GraphQLInputObjectField(
            type=GraphQLInt
        ),
        'dnsQuery': GraphQLInputObjectField(
            type=GraphQLString
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType
        )
    }
)

CreateStoryboardInputType = GraphQLInputObjectType(
    name='DnsCreateStoryboardInputType',
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
        ),
        'threatDetails': GraphQLInputObjectField(
            type=GraphQLNonNull(GraphQLList(GraphQLNonNull(ThreatDetailsInputType))),
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

MutationType = GraphQLObjectType(
    name='DnsMutationType',
    fields={
        'score': GraphQLField(
            type=GraphQLList(SpotOperationOutputType),
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
