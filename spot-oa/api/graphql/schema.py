from graphql import (
    GraphQLSchema,
    GraphQLObjectType,
    GraphQLField
)

from flow import QueryType as NetflowQueryType, MutationType as NetflowMutationType
from dns import QueryType as DnsQueryType, MutationType as DnsMutationType

SpotSchema = GraphQLSchema(
  query=GraphQLObjectType(
    name='SpotQueryType',
    fields={
      'flow': GraphQLField(
        type= NetflowQueryType,
        description='Flow information',
        resolver=lambda *_: {}
      ),
      'dns': GraphQLField(
        type= DnsQueryType,
        description='Dns information',
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
        )
    }
  )
)
