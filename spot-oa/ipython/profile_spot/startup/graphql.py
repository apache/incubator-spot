import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(__file__)))))

from api.graphql.client import GraphQLClient
