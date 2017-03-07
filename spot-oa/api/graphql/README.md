# Apache Spot (incubating) - GraphQL API

Provides an endpoint to send GraphQL queries to access and modify data.

1. Install Spot OA. Follow this [guide](../../README.md#installation).
2. Start Spot OA Web Server.
    1. From spot-oa dir, run ./runIpython.sh
3. Apache Spot will deploy a GraphQL endpoint under /graphql URL

## Development mode and GraphiQL UI

When Spot OA Web server is started in [development mode](../../ui/README.md#developmentdebugging-process), a GraphiQL UI will be enabled at /graphql URL. Use
this tool to play with our GraphQL API and to explore the entities we have made available.
