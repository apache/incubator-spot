from graphql import (
    GraphQLSchema,
    GraphQLObjectType,
    GraphQLField
)

from flow import QueryType as NetflowQueryType, MutationType as NetflowMutationType, TYPES as NetflowTypes
from dns import QueryType as DnsQueryType, MutationType as DnsMutationType, TYPES as DnsTypes
from proxy import QueryType as ProxyQueryType, MutationType as ProxyMutationType, TYPES as ProxyTypes

SpotSchema = GraphQLSchema(
  query=GraphQLObjectType(
    name='SpotQueryType',
    fields={
      'flow': GraphQLField(
        type=NetflowQueryType,
        description='Flow is a network protocol that collects IP traffic information and monitors network traffic',
        resolver=lambda *_: {}
      ),
      'dns': GraphQLField(
        type=DnsQueryType,
        description='Domain Name System (DNS) Log Records contains the requests in between clients and DNS servers',
        resolver=lambda *_: {}
      ),
      'proxy': GraphQLField(
        type=ProxyQueryType,
        description='Proxy Logs contains the requests in between clients and Proxy servers',
        resolver=lambda *_: {}
      )
    }
  ),
  mutation=GraphQLObjectType(
    name='SpotMutationType',
    fields={
        'flow': GraphQLField(
            type=NetflowMutationType,
            description='Flow related mutation operations',
            resolver=lambda *_: {}
        ),
        'dns': GraphQLField(
            type=DnsMutationType,
            description='DNS related mutation operations',
            resolver=lambda *_: {}
        ),
        'proxy': GraphQLField(
            type=ProxyMutationType,
            description='Proxy related mutation operations',
            resolver=lambda *_: {}
        )
    }
  ),
  types=NetflowTypes + DnsTypes + ProxyTypes
)
