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
from datetime import date, datetime
from graphql import (
    GraphQLObjectType,
    GraphQLField,
    GraphQLArgument,
    GraphQLNonNull,
    GraphQLList,
    GraphQLString,
    GraphQLInt,
    GraphQLFloat,
    GraphQLUnionType,
    GraphQLInterfaceType
)

from api.graphql.common import SpotDateType, SpotDatetimeType, SpotIpType, IngestSummaryType
import api.resources.dns as Dns

SuspiciousType = GraphQLObjectType(
    name='DnsSuspiciousType',
    fields={
        'frameTime': GraphQLField(
            type=SpotDatetimeType,
            description='Frame time',
            resolver=lambda root, *_: datetime.utcfromtimestamp(int(root.get('unix_tstamp') or 0))
        ),
        'frameLength': GraphQLField(
            type=GraphQLInt,
            description='Frame length in bytes',
            resolver=lambda root, *_: root.get('frame_len')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='Client\'s IP address',
            resolver=lambda root, *_: root.get('ip_dst')
        ),
        'dnsQuery': GraphQLField(
            type=GraphQLString,
            description='DNS query name',
            resolver=lambda root, *_: root.get('dns_qry_name')
        ),
        'dnsQueryClass': GraphQLField(
            type=GraphQLInt,
            description='DNS query class',
            resolver=lambda root, *_: int(root.get('dns_qry_class') or '0x0', 16)
        ),
        'dnsQueryType': GraphQLField(
            type=GraphQLInt,
            description='DNS query type',
            resolver=lambda root, *_: root.get('dns_qry_type') or 0
        ),
        'dnsQueryRcode': GraphQLField(
            type=GraphQLInt,
            description='DNS query response code',
            resolver=lambda root, *_: root.get('dns_qry_rcode') or 0
        ),
        'score': GraphQLField(
            type=GraphQLFloat,
            description='Spot ML score value',
            resolver=lambda root, *_: root.get('ml_score') or 0
        ),
        'tld': GraphQLField(
            type=GraphQLString,
            description='Top Level Domain',
            resolver=lambda root, *_: root.get('tld')
        ),
        'dnsQueryRep': GraphQLField(
            type=GraphQLString,
            description='DNS query name reputation metadata',
            resolver=lambda root, *_: root.get('query_rep')
        ),
        'clientIpSev': GraphQLField(
            type=GraphQLInt,
            description='Client IP user risk score',
            resolver=lambda root, *_: root.get('ip_sev') or 0
        ),
        'dnsQuerySev': GraphQLField(
            type=GraphQLInt,
            description='@deprecated DNS query name user risk score',
            resolver=lambda root, *_: root.get('dns_sev') or 0
        ),
        'dnsQueryClassLabel': GraphQLField(
            type=GraphQLString,
            description='DNS query class name',
            resolver=lambda root, *_: root.get('dns_qry_class_name')
        ),
        'dnsQueryTypeLabel': GraphQLField(
            type=GraphQLString,
            description='DNS query type name',
            resolver=lambda root, *_: root.get('dns_qry_type_name')
        ),
        'dnsQueryRcodeLabel': GraphQLField(
            type=GraphQLString,
            description='DNS query response code name',
            resolver=lambda root, *_: root.get('dns_qry_rcode_name')
        ),
        'networkContext': GraphQLField(
            type=GraphQLString,
            description='@deprecated Network context for client ip',
            resolver=lambda root, *_: root.get('network_context')
        ),
        'unixTimestamp': GraphQLField(
            type=GraphQLInt,
            description='Frame unix timestamp',
            resolver=lambda root, *_: root.get('unix_tstamp') or 0
        )
    }
)

EdgeDetailsType = GraphQLObjectType(
    name='DnsEdgeDetailsType',
    fields={
        'frameTime': GraphQLField(
            type=SpotDatetimeType,
            description='Frame time',
            resolver=lambda root, *_: datetime.utcfromtimestamp(int(root.get('unix_tstamp') or 0))
        ),
        'frameLength': GraphQLField(
            type=GraphQLInt,
            description='Frame length in bytes',
            resolver=lambda root, *_: root.get('frame_len')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='Client\'s IP address',
            resolver=lambda root, *_: root.get('ip_dst')
        ),
        'serverIp': GraphQLField(
            type=SpotIpType,
            description='DNS server IP address',
            resolver=lambda root, *_: root.get('ip_src')
        ),
        'dnsQuery': GraphQLField(
            type=GraphQLString,
            description='DNS query name',
            resolver=lambda root, *_: root.get('dns_qry_name')
        ),
        'dnsQueryClass': GraphQLField(
            type=GraphQLInt,
            description='DNS query class',
            resolver=lambda root, *_: int(root.get('dns_qry_class') or '0x0', 16)
        ),
        'dnsQueryType': GraphQLField(
            type=GraphQLInt,
            description='DNS query type',
            resolver=lambda root, *_: root.get('dns_qry_type') or 0
        ),
        'dnsQueryRcode': GraphQLField(
            type=GraphQLInt,
            description='DNS query response code',
            resolver=lambda root, *_: root.get('dns_qry_rcode') or 0
        ),
        'dnsQueryClassLabel': GraphQLField(
            type=GraphQLString,
            description='DNS query class name',
            resolver=lambda root, *_: root.get('dns_qry_class_name')
        ),
        'dnsQueryTypeLabel': GraphQLField(
            type=GraphQLString,
            description='DNS query type name',
            resolver=lambda root, *_: root.get('dns_qry_type_name')
        ),
        'dnsQueryRcodeLabel': GraphQLField(
            type=GraphQLString,
            description='DNS query response code name',
            resolver=lambda root, *_: root.get('dns_qry_rcode_name')
        ),
        'dnsQueryAnswers': GraphQLField(
            type=GraphQLList(GraphQLString),
            description='DNS answers',
            resolver=lambda root, *_: root.get('dns_a', '').split('|')
        ),
        'unixTimestamp': GraphQLField(
            type=GraphQLInt,
            description='Frame unix timestamp',
            resolver=lambda root, *_: root.get('unix_tstamp') or 0
        )
    }
)

ThreatDetailsType = GraphQLObjectType(
    name='DnsThreatDetailsType',
    fields={
        'total': GraphQLField(
            type=GraphQLInt,
            description='The number of time an IP sent a dns query',
            resolver=lambda root, *_: root.get('total')
        ),
        'dnsQuery': GraphQLField(
            type=GraphQLString,
            description='DNS query name',
            resolver=lambda root, *_: root.get('dns_qry_name')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='Client IP address',
            resolver=lambda root, *_: root.get('ip_dst')
        )
    }
)

IpDetailsType = GraphQLObjectType(
    name='DnsIpDetailsType',
    fields={
        'dnsQuery': GraphQLField(
            type=GraphQLString,
            description='DNS query name',
            resolver=lambda root, *_: root.get('dns_qry_name')
        ),
        'dnsQueryAnswers': GraphQLField(
            type=GraphQLList(GraphQLString),
            description='DNS answers',
            resolver=lambda root, *_: root.get('dns_a', '').split('|')
        )
    }
)

ScoredThreatType = GraphQLObjectType(
    name='DnsScoredThreatType',
    fields={
        'datetime': GraphQLField(
            type=SpotDatetimeType,
            description='Date and time of user score',
            resolver=lambda root, *_: datetime.utcfromtimestamp(int(root.get('unix_tstamp') or 0))
        ),
        'dnsQuery': GraphQLField(
            type=GraphQLString,
            description='DNS query name',
            resolver=lambda root, *_: root.get('dns_qry_name')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='Client\'s IP address',
            resolver=lambda root, *_: root.get('ip_dst')
        ),
        'dnsScore': GraphQLField(
            type=GraphQLInt,
            description='DNS query name risk score value. 1->High, 2->Medium, 3->Low',
            resolver=lambda root, *_: root.get('dns_sev') or 0
        ),
        'clientIpScore': GraphQLField(
            type=GraphQLInt,
            description='Client IP address risk score value. 1->High, 2->Medium, 3->Low',
            resolver=lambda root, *_: root.get('ip_sev') or 0
        )
    }
)

CommentInterface = GraphQLInterfaceType(
    name='DnsCommentInterface',
    fields={
        'title': GraphQLField(GraphQLString),
        'text': GraphQLField(GraphQLString)
    },
    resolve_type=lambda root, *_: QueryCommentType if root.get('dns_threat') else ClientIpCommentType
)

QueryCommentType = GraphQLObjectType(
    name='DnsQueryCommentType',
    interfaces=[CommentInterface],
    fields={
        'dnsQuery': GraphQLField(
            type=GraphQLString,
            description='High risk DNS query name',
            resolver=lambda root, *_: root.get('dns_threat')
        ),
        'title': GraphQLField(
            type=GraphQLString,
            description='Threat title',
            resolver=lambda root, *_: root.get('title')
        ),
        'text': GraphQLField(
            type=GraphQLString,
            description='Threat description',
            resolver=lambda root, *_: root.get('text')
        )
    }
)

ClientIpCommentType = GraphQLObjectType(
    name='DnsClientIpCommentType',
    interfaces=[CommentInterface],
    fields={
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='High risk client IP address',
            resolver=lambda root, *_: root.get('ip_threat')
        ),
        'title': GraphQLField(
            type=GraphQLString,
            description='Threat title',
            resolver=lambda root, *_: root.get('title')
        ),
        'text': GraphQLField(
            type=GraphQLString,
            description='Threat description',
            resolver=lambda root, *_: root.get('text')
        )
    }
)

ThreatsInformationType = GraphQLObjectType(
    name='DnsThreats',
    fields={
        'list': GraphQLField(
            type=GraphQLList(ScoredThreatType),
            description='List of DNS query names or client IPs that have been scored',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference to retrieve the list of high risk threats. Defaults to today'
                )
            },
            resolver=lambda root, args, *_: Dns.get_scored_connections(date=args.get('date', date.today()))
        ),
        'comments': GraphQLField(
            type=GraphQLList(CommentInterface),
            description='A list of comments about threats',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference to retrieve the list of high risk comments. Defaults to today'
                )
            },
            resolver=lambda root, args, *_: Dns.comments(date=args.get('date', date.today()))
        )
    }
)

IncidentProgressionInterface = GraphQLInterfaceType(
    name='DnsIncidentProgressionInterface',
    fields={
        'total': GraphQLField(
            type=GraphQLInt
        )
    },
    resolve_type=lambda root, *_: IncidentProgressionQueryType if   root.get('dns_qry_name') else IncidentProgressionClientIpType
)

IncidentProgressionClientIpType = GraphQLObjectType(
    name='DnsIncidentProgressionClientIpType',
    interfaces=[IncidentProgressionInterface],
    fields={
        'total': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('total')
        ),
        'clientIp': GraphQLField(
            type=SpotIpType,
            description='Client\'s IP address',
            resolver=lambda root, *_: root.get('ip_dst')
        )
    }
)

IncidentProgressionQueryType = GraphQLObjectType(
    name='DnsIncidentProgressionQueryType',
    interfaces=[IncidentProgressionInterface],
    fields={
        'total': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('total')
        ),
        'dnsQuery': GraphQLField(
            type=GraphQLString,
            description='DNS query name',
            resolver=lambda root, *_: root.get('dns_qry_name')
        )
    }
)

ThreatInformationType = GraphQLObjectType(
    name='DnsThreatInformation',
    fields={
        'incidentProgression': GraphQLField(
            type=GraphQLList(IncidentProgressionInterface),
            description='Details the type of connections that conform the activity related to the threat',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for incident progression information. Defaults to today'
                ),
                'dnsQuery': GraphQLArgument(
                    type=GraphQLString,
                    description='Threat\'s dns query'
                ),
                'clientIp': GraphQLArgument(
                    type=SpotIpType,
                    description='Threat\'s client ip'
                )
            },
            resolver=lambda root, args, *_ : Dns.incident_progression(date=args.get('date', date.today()), query=args.get('dnsQuery'), ip=args.get('clientIp'))
        ),
        'details': GraphQLField(
            type=GraphQLList(ThreatDetailsType),
            description='Get details about threats',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as a reference for suspicous queries. Defaults to today'
                ),
                'dnsQuery': GraphQLArgument(
                    type=GraphQLString,
                    description='DNS query name of interest'
                ),
                'clientIp': GraphQLArgument(
                    type=SpotIpType,
                    description='Client IP address of interest'
                ),
                'first': GraphQLArgument(
                    type=GraphQLInt,
                    description='The number of records to return'
                )
            },
            resolver=lambda root, args, *_: Dns.expanded_search(
                                                date=args.get('date', date.today()),
                                                query=args.get('dnsQuery'),
                                                ip=args.get('clientIp'),
                                                limit=args.get('first'))
        )
    }
)

QueryType = GraphQLObjectType(
    name='DnsQueryType',
    fields={
        'suspicious': GraphQLField(
            type=GraphQLList(SuspiciousType),
            description='Suspicious DNS query names',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as a reference for suspicious connections. Defaults to today'
                ),
                'clientIp': GraphQLArgument(
                    type=SpotIpType,
                    description='Client IP of interest'
                ),
                'dnsQuery': GraphQLArgument(
                    type=GraphQLString,
                    description='Partial query name of interest'
                )
            },
            resolver=lambda root, args, *_: Dns.suspicious_queries(date=args.get('date', date.today()), ip=args.get('clientIp'), query=args.get('dnsQuery'))
        ),
        'edgeDetails': GraphQLField(
            type=GraphQLList(EdgeDetailsType),
            description='DNS queries between client and DNS server around a particular moment in time',
            args={
                'frameTime': GraphQLArgument(
                    type=GraphQLNonNull(SpotDatetimeType),
                    description='Time of interest'
                ),
                'dnsQuery': GraphQLArgument(
                    type=GraphQLNonNull(GraphQLString),
                    description='DNS query name of interest'
                )
            },
            resolver=lambda root, args, *_: Dns.details(frame_time=args.get('frameTime'), query=args.get('dnsQuery'))
        ),
        'ipDetails': GraphQLField(
            type=GraphQLList(IpDetailsType),
            description='Query names made by client',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as a reference for suspicous connections. Defaults to today'
                ),
                'clientIp': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Client\'s IP address'
                )
            },
            resolver=lambda root, args, *_: Dns.client_details(date=args.get('date', date.today()), ip=args.get('clientIp'))
        ),
        'threats': GraphQLField(
            type=ThreatsInformationType,
            description='Advanced inforamtion about threats',
            resolver=lambda *_ : {}
        ),
        'threat': GraphQLField(
            type=ThreatInformationType,
            description='Advanced inforamtion about a single threat',
            resolver=lambda *_: {}
        ),
        'ingestSummary': GraphQLField(
            type=GraphQLList(IngestSummaryType),
            description='Summary of ingested DNS records in range',
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
            resolver=lambda root, args, *_: Dns.ingest_summary(start_date=args.get('startDate'), end_date=args.get('endDate'))
         )
    }
)

TYPES = [QueryCommentType, ClientIpCommentType, IncidentProgressionQueryType, IncidentProgressionClientIpType]
