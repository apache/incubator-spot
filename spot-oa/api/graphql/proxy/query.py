from datetime import date
from graphql import (
    GraphQLObjectType,
    GraphQLArgument,
    GraphQLNonNull,
    GraphQLField,
    GraphQLString,
    GraphQLInt,
    GraphQLList
)

from api.graphql.common import SpotDateType, SpotDatetimeType, SpotIpType, IngestSummaryType
import api.resources.proxy as Proxy

SuspiciousType = GraphQLObjectType(
    name='ProxySuspiciousType',
    fields={
        'datetime': GraphQLField(
            type=SpotDatetimeType,
            description='Start time of the request',
            resolver=lambda root, *_: '{} {}'.format(root.get('tdate') or '1970-01-01', root.get('time') or '00:00:00')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='Clent\'s ip',
            resolver=lambda root, *_: root.get('clientip')
        ),
        'host': GraphQLField(
            type=GraphQLString,
            description='Target host of the request',
            resolver=lambda root, *_: root.get('host')
        ),
        'requestMethod': GraphQLField(
            type=GraphQLString,
            description='HTTP method',
            resolver=lambda root, *_: root.get('reqmethod')
        ),
        'userAgent': GraphQLField(
            type=GraphQLString,
            description='Client\'s user agent',
            resolver=lambda root, *_: root.get('useragent')
        ),
        'responseContentType': GraphQLField(
            type=GraphQLString,
            description='HTTP response content type',
            resolver=lambda root, *_: root.get('resconttype')
        ),
        'duration': GraphQLField(
            type=GraphQLInt,
            description='Duration of the request',
            resolver=lambda root, *_: root.get('duration')
        ),
        'username': GraphQLField(
            type=GraphQLString,
            description='Username used for authetication',
            resolver=lambda root, *_: root.get('username')
        ),
        'webCategory': GraphQLField(
            type=GraphQLString,
            description='Web category',
            resolver=lambda root, *_: root.get('webcat')
        ),
        'referer': GraphQLField(
            type=GraphQLString,
            description='Request\'s referer',
            resolver=lambda root, *_: root.get('referer')
        ),
        'responseCode': GraphQLField(
            type=GraphQLInt,
            description='HTTP response code',
            resolver=lambda root, *_: root.get('respcode') or 0
        ),
        'uriPort': GraphQLField(
            type=GraphQLInt,
            description='URI\'s port',
            resolver=lambda root, *_: root.get('uriport')
        ),
        'uriPath': GraphQLField(
            type=GraphQLString,
            description='URI\'s path',
            resolver=lambda root, *_: root.get('uripath')
        ),
        'uriQuery': GraphQLField(
            type=GraphQLString,
            description='URI\'s query',
            resolver=lambda root, *_: root.get('uriquery')
        ),
        'serverIp': GraphQLField(
            type=SpotIpType,
            description='Server\'s ip',
            resolver=lambda root, *_: root.get('serverip')
        ),
        'serverToClientBytes': GraphQLField(
            type=GraphQLInt,
            description='Number of bytes sent from appliance to client',
            resolver=lambda root, *_: root.get('scbytes')
        ),
        'clientToServerBytes': GraphQLField(
            type=GraphQLInt,
            description='Number of bytes sent from client to appliance',
            resolver=lambda root, *_: root.get('csbytes')
        ),
        'uri': GraphQLField(
            type=GraphQLString,
            description='The original URL requested',
            resolver=lambda root, *_: root.get('fulluri')
        ),
        'score': GraphQLField(
            type=GraphQLInt,
            description='Score value assigned by machine learning algorithm',
            resolver=lambda root, *_: root.get('ml_score') or 0
        ),
        'uriRep': GraphQLField(
            type=GraphQLString,
            description='URI\'s reputation',
            resolver=lambda root, *_: root.get('uri_rep')
        ),
        'responseCodeLabel': GraphQLField(
            type=GraphQLString,
            description='HTTP response code label',
            resolver=lambda root, *_: root.get('respcode_name')
        ),
        'networkContext': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('network_context')
        )
    }
)

EdgeDetailsType = GraphQLObjectType(
    name='ProxyEdgeDetailsType',
    fields={
        'datetime': GraphQLField(
            type=GraphQLString,
            description='Start time of the request',
            resolver=lambda root, *_: '{} {}'.format(root.get('tdate') or '1970-01-01', root.get('time') or '00:00:00')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='Clent\'s ip',
            resolver=lambda root, *_: root.get('clientip')
        ),
        'host': GraphQLField(
            type=GraphQLString,
            description='Target host of the request',
            resolver=lambda root, *_: root.get('host')
        ),
        'webCategory': GraphQLField(
            type=GraphQLString,
            description='Web category',
            resolver=lambda root, *_: root.get('webcat')
        ),
        'responseCode': GraphQLField(
            type=GraphQLInt,
            description='HTTP response code',
            resolver=lambda root, *_: root.get('respcode') or 0
        ),
        'responseCodeLabel': GraphQLField(
            type=GraphQLString,
            description='HTTP response code label',
            resolver=lambda root, *_: root.get('respcode_name')
        ),
        'requestMethod': GraphQLField(
            type=GraphQLString,
            description='HTTP method',
            resolver=lambda root, *_: root.get('reqmethod')
        ),
        'userAgent': GraphQLField(
            type=GraphQLString,
            description='Client\'s user agent',
            resolver=lambda root, *_: root.get('useragent')
        ),
        'responseContentType': GraphQLField(
            type=GraphQLString,
            description='HTTP response content type',
            resolver=lambda root, *_: root.get('resconttype')
        ),
        'referer': GraphQLField(
            type=GraphQLString,
            description='Request\'s referer',
            resolver=lambda root, *_: root.get('referer')
        ),
        'uriPort': GraphQLField(
            type=GraphQLInt,
            description='URI\'s port',
            resolver=lambda root, *_: root.get('uriport')
        ),
        'serverIp': GraphQLField(
            type=SpotIpType,
            description='Server\'s ip',
            resolver=lambda root, *_: root.get('serverip')
        ),
        'serverToClientBytes': GraphQLField(
            type=GraphQLInt,
            description='Number of bytes sent from appliance to client',
            resolver=lambda root, *_: root.get('scbytes')
        ),
        'clientToServerBytes': GraphQLField(
            type=GraphQLInt,
            description='Number of bytes sent from client to appliance',
            resolver=lambda root, *_: root.get('csbytes')
        ),
        'uri': GraphQLField(
            type=GraphQLString,
            description='The original URL requested',
            resolver=lambda root, *_: root.get('fulluri')
        )
    }
)

ScoredRequestType = GraphQLObjectType(
    name='ProxyScoredRequestType',
    fields={
        'datetime': GraphQLField(
            type=SpotDateType,
            description='Date and time of user score',
            resolver=lambda root, *_: root.get('tdate') or '1970-01-01'
        ),
        'uri': GraphQLField(
            type=SpotIpType,
            description='Requested URI',
            resolver=lambda root, *_: root.get('fulluri')
        ),
        'score': GraphQLField(
            type=GraphQLInt,
            description='Score value. 1->High, 2->Medium, 3->Low',
            resolver=lambda root, *_: root.get('uri_sev') or 0
        )
    }
)

CommentType = GraphQLObjectType(
    name='ProxyCommentType',
    fields={
        'uri': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('p_threat')
        ),
        'title': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('title')
        ),
        'text': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('text')
        )
    }
)

ThreatsInformationType = GraphQLObjectType(
    name='ProxyThreatsType',
    fields={
        'list': GraphQLField(
            type=GraphQLList(ScoredRequestType),
            description='List of requests that have been scored',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference to retrieve the list of scored requests. Defaults to today'
                )
            },
            resolver=lambda root, args, *_: Proxy.get_scored_requests(date=args.get('date', date.today()))
        ),
        'comments': GraphQLField(
            type=GraphQLList(CommentType),
            description='A list of comments about threats',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference to retrieve the list of high risk comments. Defaults to today'
                )
            },
            resolver=lambda root, args, *_: Proxy.story_board(date=args.get('date', date.today()))
        )
    }
)

ThreatDetailsType = GraphQLObjectType(
    name='ProxyThreatDetailsType',
    fields={
        'datetime': GraphQLField(
            type=SpotDatetimeType,
            resolver=lambda root, *_: '{} {}'.format(root.get('p_date') or '1970-01-01', root.get('p_time') or '00:00:00')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            resolver=lambda root, *_: root.get('clientip')
        ),
        'username': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('username')
        ),
        'duration': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('duration')
        ),
        'uri': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('fulluri')
        ),
        'webCategory': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('webcat')
        ),
        'responseCode': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('respcode')
        ),
        'requestMethod': GraphQLField(
            type=GraphQLString,
            description='Http Method',
            resolver=lambda root, *_: root.get('reqmethod')
        ),
        'userAgent': GraphQLField(
            type=GraphQLString,
            description='Client\'s user agent',
            resolver=lambda root, *_: root.get('useragent')
        ),
        'responseContentType': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('resconttype')
        ),
        'referer': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('referer')
        ),
        'uriPort': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('uriport')
        ),
        'serverIp': GraphQLField(
            type=SpotIpType,
            resolver=lambda root, *_: root.get('serverip')
        ),
        'serverToClientBytes': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('scbytes')
        ),
        'clientToServerBytes': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('csbytes')
        )
    }
)

IncidentProgressionRequestType = GraphQLObjectType(
    name='ProxyIncidentProgressionRequestType',
    fields={
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='Client\'s ip',
            resolver=lambda root, *_: root.get('clientip')
        ),
        'referer': GraphQLField(
            type=GraphQLString,
            description='URI that refers to Threat\'s URI',
            resolver=lambda root, *_: root.get('referer')
        ),
        'requestMethod': GraphQLField(
            type=GraphQLString,
            description='HTTP Request Method',
            resolver=lambda root, *_: root.get('reqmethod')
        ),
        'responseContentType': GraphQLField(
            type=GraphQLString,
            description='Response Content Type',
            resolver=lambda root, *_: root.get('resconttype')
        )
    }
)

IncidentProgressionType = GraphQLObjectType(
    name='ProxyIncidentProgressionType',
    fields={
        'uri': GraphQLField(
            type=GraphQLString,
            description='Threat\'s URI',
            resolver=lambda root, *_: root.get('fulluri')
        ),
        'refererFor': GraphQLField(
            type=GraphQLList(GraphQLString),
            description='A list of URI who whose referer is the threat\'s URI',
            resolver=lambda root, *_: root.get('referer_for')
        ),
        'requests': GraphQLField(
            type=GraphQLList(IncidentProgressionRequestType),
            description='A list of requests made to Threat\'s URI',
            resolver=lambda root, *_: root.get('requests')
        )
    }
)

TimelineType = GraphQLObjectType(
    name='ProxyTimelineType',
    fields={
        'startDatetime': GraphQLField(
            type=SpotDatetimeType,
            resolver=lambda root, *_: root.get('tstart') or '1970-01-01 00:00:00'
        ),
        'endDatetime': GraphQLField(
            type=SpotDatetimeType,
            resolver=lambda root, *_: root.get('tend') or '1970-01-01 00:00:00'
        ),
        'duration': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('duration')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            resolver=lambda root, *_: root.get('clientip')
        ),
        'responseCode': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('respcode')
        ),
        'responseCodeLabel': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('respcode_name')
        )
    }
)

ThreatInformationType = GraphQLObjectType(
    name='ProxyThreatInformation',
    fields={
        'details': GraphQLField(
            type=GraphQLList(ThreatDetailsType),
            description='Detailed information about a high risk threat',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for incident progression information. Defaults to today'
                ),
                'uri': GraphQLArgument(
                    type=GraphQLNonNull(GraphQLString),
                    description='Threat\'s URI'
                )
            },
            resolver=lambda root, args, *_: Proxy.expanded_search(date=args.get('date', date.today()), uri=args.get('uri'))
        ),
        'incidentProgression': GraphQLField(
            type=IncidentProgressionType,
            description='Incident progression information',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for incident progression information. Defaults to today'
                ),
                'uri': GraphQLArgument(
                    type=GraphQLNonNull(GraphQLString),
                    description='Threat\'s uri'
                )
            },
            resolver=lambda root, args, *_: Proxy.incident_progression(date=args.get('date', date.today()), uri=args.get('uri'))
        ),
        'timeline': GraphQLField(
            type=GraphQLList(TimelineType),
            description='Time based information about this threat',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for time line information. Defaults to today'
                ),
                'uri': GraphQLArgument(
                    type=GraphQLNonNull(GraphQLString),
                    description='Threat\'s URI'
                )
            },
            resolver=lambda root, args, *_: Proxy.time_line(date=args.get('date', date.today()), uri=args.get('uri'))
        )
    }
)

QueryType = GraphQLObjectType(
    name='ProxyQueryType',
    fields={
        'suspicious': GraphQLField(
            type=GraphQLList(SuspiciousType),
            description='Proxy Suspicious requests',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference to retrieve the list of suspicious requests. Defaults to today'
                ),
                'uri': GraphQLArgument(
                    type=GraphQLString,
                    description='URI of interest'
                ),
                'clientIp': GraphQLArgument(
                    type=SpotIpType,
                    description='Client\'s ip'
                )
            },
            resolver=lambda root, args, *_: Proxy.suspicious_requests(date=args.get('date', date.today()), uri=args.get('uri'), ip=args.get('clientIp'))
        ),
        'edgeDetails': GraphQLField(
            type=GraphQLList(EdgeDetailsType),
            description='HTTP requests to a particular uri',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference to retrieve the list of requests made by client. Defaults to today'
                ),
                'uri': GraphQLArgument(
                    type=GraphQLNonNull(GraphQLString),
                    description='URI of interest'
                ),
                'clientIp': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Client\'s ip'
                )
            },
            resolver=lambda root, args, *_: Proxy.details(date=args.get('date', date.today()), uri=args.get('uri'), ip=args.get('clientIp'))
        ),
        'threats': GraphQLField(
            type=ThreatsInformationType,
            description='Advanced inforamtion about threats',
            resolver=lambda *_ : {}
        ),
        'threat': GraphQLField(
            type=ThreatInformationType,
            description='Advanced inforamtion about a single threat',
            resolver=lambda *_:{}
        ),
        'ingestSummary': GraphQLField(
            type=GraphQLList(IngestSummaryType),
            description='Total of ingested http requests',
            args={
                'startDate': GraphQLArgument(
                    type=GraphQLNonNull(SpotDateType),
                    description='Start date'
                ),
                'endDate': GraphQLArgument(
                    type=GraphQLNonNull(SpotDateType),
                    description='End date'
                )
            },
            resolver=lambda root, args, *_: Proxy.ingest_summary(start_date=args.get('startDate'), end_date=args.get('endDate'))
        )
    }
)

TYPES = []
