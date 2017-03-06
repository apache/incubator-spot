from collections import OrderedDict
from datetime import date, datetime
from graphql import (
    GraphQLScalarType,
    GraphQLObjectType,
    GraphQLField,
    GraphQLString,
    GraphQLInt,
    GraphQLBoolean,
    GraphQLList,
    GraphQLNonNull
)
import socket

def coerce_date(value):
    if isinstance(value, date):
        return value
    elif isinstance(value, datetime):
        return value.date()
    elif isinstance(value, int):
        return date.utcfromtimestamp(value)
    else:
        return datetime.strptime(str(value), '%Y-%m-%d').date()

def serialize_date(value):
    return datetime.strptime(value, '%Y-%m-%d').strftime('%Y-%m-%d')

def parse_date_literal(ast):
    return datetime.strptime(ast.value, '%Y-%m-%d')

SpotDateType = GraphQLScalarType(
    name='SpotDateType',
    description='The `Date` scalar type represents date values in the format yyyy-mm-dd.',
    serialize=serialize_date,
    parse_value=coerce_date,
    parse_literal=parse_date_literal)

def coerce_datetime(value):
    if isinstance(value, int):
        value = datetime.utcfromtimestamp(value)
    elif not isinstance(value, datetime):
        value = datetime.strptime(str(value), '%Y-%m-%d %H:%M:%S')

    return value

def serialize_datetime(value):
    if not isinstance(value, datetime):
        value = datetime.strptime(str(value), '%Y-%m-%d %H:%M:%S')

    return value.strftime('%Y-%m-%d %H:%M:%S')

def parse_datetime_literal(ast):
    return datetime.strptime(ast.value, '%Y-%m-%d %H:%M:%S')

SpotDatetimeType = GraphQLScalarType(
    name='SpotDatetimeType',
    description='The `Datetime` scalar type represents datetime values in the format yyyy-mm-dd hh:mm:ss.',
    serialize=serialize_datetime,
    parse_value=coerce_datetime,
    parse_literal=parse_datetime_literal)

def coerce_ip(value):
    return str(value)

def parse_ip_literal(ast):
    socket.inet_aton(ast.value)

    return ast.value

SpotIpType = GraphQLScalarType(
    name='SpotIpType',
    description='The `Ip` scalar type represents a network ip in dot-decimal format.',
    serialize=coerce_ip,
    parse_value=coerce_ip,
    parse_literal=parse_ip_literal)

SpotOperationOutputType = GraphQLObjectType(
    name='SpotOperationOutputType',
    fields={
        'success': GraphQLField(
            type=GraphQLNonNull(GraphQLBoolean),
            description='True after the operation success',
            resolver=lambda root, *_: root.get('success')
        )
    }
)

def create_spot_node_type(name, extra_fields={}):
    def get_fields():
        fields = {
            'name': GraphQLField(
                type=GraphQLNonNull(GraphQLString),
                description='Node name',
                resolver=lambda root, *_: root.get('name')
            ),
            'children': GraphQLField(
                type=GraphQLList(NodeType),
                description='Children list',
                resolver=lambda root, *_: root.get('children')
            )
        }
        fields.update(extra_fields if type(extra_fields) is dict else {})

        return fields

    NodeType = GraphQLObjectType(
        name=name,
        fields=get_fields
    )

    return NodeType

IngestSummaryType = GraphQLObjectType(
    name='SpotIngestSummaryType',
    fields={
        'datetime': GraphQLField(
            type=SpotDatetimeType,
            resolver=lambda root, *_: '{}:00'.format(root.get('tdate'))
        ),
        'total': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('total')
        )
    }
)
