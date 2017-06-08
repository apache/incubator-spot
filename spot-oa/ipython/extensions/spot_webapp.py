import sys
from os import path

sys.path.append(path.dirname(path.dirname(path.dirname(__file__))))

from api.graphql.webapp import load_jupyter_server_extension
