from graphql import (
    GraphQLSchema,
    GraphQLObjectType,
    GraphQLField
)

from flow import QueryType as NetflowQueryType, MutationType as NetflowMutationType

SpotSchema = GraphQLSchema(
  query=GraphQLObjectType(
    name='SpotQueryType',
    fields={
      'flow': GraphQLField(
        type= NetflowQueryType,
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
        )
    }
  )
)
