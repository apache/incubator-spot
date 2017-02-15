from collections import OrderedDict
from datetime import date, datetime
from graphql import GraphQLScalarType

def coerce_date(value):
    if isinstance(value, date):
        return value
    elif isinstance(value, datetime):
        return value.date()
    elif isinstance(value, int):
        return date.fromtimestamp(value)
    else:
        return datetime.strptime(str(value), '%Y-%m-%d').date()

def parse_date_literal(ast):
    return datetime.strptime(ast.value, '%Y-%m-%d')

SpotDateType = GraphQLScalarType(
    name='Date',
    description='The `Date` scalar type represents date values in the format yyyy-mm-dd.',
    serialize=coerce_date,
    parse_value=coerce_date,
    parse_literal=parse_date_literal)

def coerce_datetime(value):
    if isinstance(value, datetime):
        return value
    elif isinstance(value, int):
        return datetime.fromtimestamp(value)
    else:
        return datetime.strptime(str(value), '%Y-%m-%d %H:%M:%S')

def parse_datetime_literal(ast):
    return datetime.strptime(ast.value, '%Y-%m-%d %H:%M:%S')

SpotDatetimeType = GraphQLScalarType(
    name='Datetime',
    description='The `Datetime` scalar type represents datetime values in the format yyyy-mm-dd hh:mm:ss.',
    serialize=coerce_datetime,
    parse_value=coerce_datetime,
    parse_literal=parse_datetime_literal)

def coerce_ip(value):
    return str(value)

import socket
def parse_ip_literal(ast):
    socket.inet_aton(ast.value)

    return ast.value

SpotIpType = GraphQLScalarType(
    name='Ip',
    description='The `Ip` scalar type represents a network ip in dot-decimal format.',
    serialize=coerce_ip,
    parse_value=coerce_ip,
    parse_literal=parse_ip_literal)
