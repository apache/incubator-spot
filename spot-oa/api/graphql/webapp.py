#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
if __name__=='__main__':
    import sys
    from os import path

    sys.path.append(path.dirname(path.dirname(path.dirname(__file__))))

from flask import Flask, Blueprint
from flask_graphql import GraphQLView
import os

from schema import SpotSchema

app = Flask(__name__)

blueprint = Blueprint('graphql_api', __name__)
blueprint.add_url_rule('/graphql', strict_slashes=False, view_func=GraphQLView.as_view('graphql', schema=SpotSchema, graphiql=os.environ.get('SPOT_DEV')=='1'))

app.register_blueprint(blueprint)

if __name__=='__main__':
    port = int(sys.argv[1]) if len(sys.argv)>1 else 8889

    app.run(host='0.0.0.0', port=port)

def load_jupyter_server_extension(nb_app):
    import tornado.web
    import tornado.wsgi

    wsgi_app = tornado.wsgi.WSGIContainer(app)
    nb_app.web_app.add_handlers(r'.*', [
        (r'/graphql.*', tornado.web.FallbackHandler, dict(fallback=wsgi_app))
    ])

    nb_app.log.info('Apache Spot server extension loaded')
    if os.environ.get('SPOT_DEV')=='1':
        nb_app.log.warn('Apache Spot server running in dev mode (environment var SPOT_DEV=1)')
