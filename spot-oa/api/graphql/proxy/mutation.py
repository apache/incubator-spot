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
from api.resources.proxy import Proxy

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
            type=GraphQLString,
            description='Requested URI'
        ),
        'clientIp': GraphQLInputObjectField(
            type=SpotIpType,
            description='Client\'s ip'
        )
    }
)

AddCommentInputType = GraphQLInputObjectType(
    name='ProxyAddCommentInputType',
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
        )
    }
)

def _score_connection(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    score = _input.get('score')
    uri = _input.get('uri')
    clientIp = _input.get('clientIp')

    return {'success': Proxy.score_request(date=_date, score=score, uri=uri, cllientip=clientIp)}

def _add_comment(args):
    _input = args.get('input')
    _date = _input.get('date', date.today())
    uri = _input.get('uri')
    title = _input.get('title')
    text = _input.get('text')

    if Proxy.save_comment(date=_date, uri=uri, title=title, text=text) is None:
        return {'success':True}
    else:
        return {'success':False}

MutationType = GraphQLObjectType(
    name='ProxyMutationType',
    fields={
        'score': GraphQLField(
            type=SpotOperationOutputType,
            args={
                'input': GraphQLArgument(
                    type=GraphQLNonNull(ScoreInputType),
                    description='Score criteria'
                )
            },
            resolver=lambda root, args, *_: _score_connection(args)
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
