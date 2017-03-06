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
        description='Flow information',
        resolver=lambda *_: {}
      ),
      'dns': GraphQLField(
        type=DnsQueryType,
        description='Dns information',
        resolver=lambda *_: {}
      ),
      'proxy': GraphQLField(
        type=ProxyQueryType,
        description='Proxy Information',
        resolver=lambda *_: {}
      )
    }
  ),
  mutation=GraphQLObjectType(
    name='SpotMutationType',
    fields={
        'flow': GraphQLField(
            type=NetflowMutationType,
            resolver=lambda *_: {}
        ),
        'dns': GraphQLField(
            type=DnsMutationType,
            resolver=lambda *_: {}
        ),
        'proxy': GraphQLField(
            type=ProxyMutationType,
            resolver=lambda *_: {}
        )
    }
  ),
  types=NetflowTypes + DnsTypes + ProxyTypes
)
