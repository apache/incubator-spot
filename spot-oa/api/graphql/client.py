import IPython
import json
import urllib3
import os

class GraphQLClient:
    def __init__(self, url=None):
        self.url = url or 'http://localhost:{}/graphql'.format(self.get_nbserver_info()['port'])
        self.variables = None

    def get_nbserver_info(self):
        profile_loc = IPython.config.get_config()['ProfileDir']['location']
        nbserver_pid = os.getppid()
        nbserver_file = os.path.join(profile_loc, 'security', 'nbserver-{}.json'.format(nbserver_pid))

        try:
            return json.load(open(nbserver_file))
        except:
            return {}

    def set_query(self, query):
        self.query = query

    def set_variables(self, variables):
        self.variables = variables

    def send_query(self):
        assert(self.url is not None)
        assert(type(self.url) is str)
        assert(self.query is not None)
        assert(type(self.query) is str)

        data = {
            'query': self.query
        }

        if self.variables is not None and type(self.variables) is dict:
            data['variables'] = self.variables

        encoded_data = json.dumps(data).encode('utf-8')

        http = urllib3.PoolManager()

        response = http.request(
            'POST',
            self.url,
            body=encoded_data,
            headers={
                'Accept': 'application/json',
                'Content-type': 'application/json'
            }
        )

        try:
            return json.loads(response.data.decode('utf-8'))
        except:
            return {
                'errors': [
                    {
                        'status': response.status,
                        'message': 'Failed to contact GraphQL endpoint. Is "{}" the correct URL?'.format(self.url)
                    }
                ]
            }

    @classmethod
    def request(cls, query, variables=None, url=None):
        client = cls(url)

        client.set_query(query)
        if variables is not None:
            client.set_variables(variables)

        return client.send_query()
