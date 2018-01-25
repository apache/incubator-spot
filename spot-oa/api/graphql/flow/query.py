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
from datetime import date
from graphql import (
    GraphQLObjectType,
    GraphQLField,
    GraphQLArgument,
    GraphQLList,
    GraphQLString,
    GraphQLInt,
    GraphQLFloat,
    GraphQLBoolean,
    GraphQLNonNull
)

from api.graphql.common import SpotDateType, SpotDatetimeType, SpotIpType, create_spot_node_type, IngestSummaryType
import api.resources.flow as Flow

SuspiciousType = GraphQLObjectType(
    name='NetflowSuspiciousType',
    fields={
        'tstart': GraphQLField(
            type=SpotDatetimeType,
            description='Time the flow was received by the flow collector',
            resolver=lambda root, *_: root.get('tstart')
        ),
        'srcIp': GraphQLField(
            type=GraphQLString,
            description='Source IP address',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=GraphQLString,
            description='Destination IP address',
            resolver=lambda root, *_: root.get('dstip')
        ),
        'srcPort': GraphQLField(
            type=GraphQLInt,
            description='Source port',
            resolver=lambda root, *_: root.get('sport') or 0
        ),
        'dstPort': GraphQLField(
            type=GraphQLInt,
            description='Destination port',
            resolver=lambda root, *_: root.get('dport') or 0
        ),
        'protocol': GraphQLField(
            type=GraphQLString,
            description='IP protocol',
            resolver=lambda root, *_: root.get('proto')
        ),
        'inPkts': GraphQLField(
            type=GraphQLInt,
            description='Input packets',
            resolver=lambda root, *_: root.get('ipkt') or 0
        ),
        'inBytes': GraphQLField(
            type=GraphQLInt,
            description='Input bytes',
            resolver=lambda root, *_: root.get('ibyt') or 0
        ),
        'outPkts': GraphQLField(
            type=GraphQLInt,
            description='Output packets',
            resolver=lambda root, *_: root.get('opkt') or 0
        ),
        'outBytes': GraphQLField(
            type=GraphQLInt,
            description='Output bytes',
            resolver=lambda root, *_: root.get('obyt') or 0
        ),
        'score': GraphQLField(
            type=GraphQLFloat,
            description='Spot ML score',
            resolver=lambda root, *_: root.get('ml_score') or 0
        ),
        'rank': GraphQLField(
            type=GraphQLInt,
            description='Spot ML rank',
            resolver=lambda root, *_: root.get('rank') or 0
        ),
        'srcIp_isInternal': GraphQLField(
            type=GraphQLInt,
            description='Internal source IP address context flag',
            resolver=lambda root, *_: root.get('srcip_internal')
        ),
        'dstIp_isInternal': GraphQLField(
            type=GraphQLInt,
            description='Internal destionation IP address context flag',
            resolver=lambda root, *_: root.get('dstip_internal')
        ),
        'srcIp_geoloc': GraphQLField(
            type=GraphQLString,
            description='Source IP geolocation',
            resolver=lambda root, *_: root.get('src_geoloc')
        ),
        'dstIp_geoloc': GraphQLField(
            type=GraphQLString,
            description='Destination IP geolocation',
            resolver=lambda root, *_: root.get('dst_geoloc')
        ),
        'srcIp_domain': GraphQLField(
            type=GraphQLString,
            description='Source IP domain',
            resolver=lambda root, *_: root.get('src_domain')
        ),
        'dstIp_domain': GraphQLField(
            type=GraphQLString,
            description='Destination IP domain',
            resolver=lambda root, *_: root.get('dst_domain')
        ),
        'srcIp_rep': GraphQLField(
            type=GraphQLString,
            description='Source IP reputation metadata',
            resolver=lambda root, *_: root.get('src_rep')
        ),
        'dstIp_rep': GraphQLField(
            type=GraphQLString,
            description='Destination IP reputation metadata',
            resolver=lambda root, *_: root.get('dst_rep')
        )
    }
)

EdgeDetailsType = GraphQLObjectType(
    name='NetflowEdgeDetailsType',
    fields={
        'tstart': GraphQLField(
            type=SpotDatetimeType,
            description='Time the flow was received by the flow collector',
            resolver=lambda root, *_: root.get('tstart')
        ),
        'srcIp': GraphQLField(
            type=GraphQLString,
            description='Source IP address',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=GraphQLString,
            description='Destination IP address',
            resolver=lambda root, *_: root.get('dstip')
        ),
        'srcPort': GraphQLField(
            type=GraphQLString,
            description='Source port',
            resolver=lambda root, *_: root.get('sport')
        ),
        'dstPort': GraphQLField(
            type=GraphQLString,
            description='Destination port',
            resolver=lambda root, *_: root.get('dport')
        ),
        'protocol': GraphQLField(
            type=GraphQLString,
            description='IP protocol',
            resolver=lambda root, *_: root.get('proto')
        ),
        'flags': GraphQLField(
            type=GraphQLString,
            description='TCP flags',
            resolver=lambda root, *_: root.get('flags')
        ),
        'tos': GraphQLField(
            type=GraphQLString,
            description='DSCP value',
            resolver=lambda root, *_: root.get('tos')
        ),
        'inBytes': GraphQLField(
            type=GraphQLInt,
            description='Input bytes',
            resolver=lambda root, *_: root.get('ibyt') or 0
        ),
        'inPkts': GraphQLField(
            type=GraphQLInt,
            description='Input packets',
            resolver=lambda root, *_: root.get('ipkt') or 0
        ),
        'inIface': GraphQLField(
            type=GraphQLString,
            description='SNMP input interface id index',
            resolver=lambda root, *_: root.get('input')
        ),
        'outIface': GraphQLField(
            type=GraphQLString,
            description='SNMP output interface id index',
            resolver=lambda root, *_: root.get('output')
        ),
        'routerIp': GraphQLField(
            type=GraphQLString,
            description='Reporting router IP address',
            resolver=lambda root, *_: root.get('rip')
        ),
        'outBytes': GraphQLField(
            type=GraphQLInt,
            description='Output bytes',
            resolver=lambda root, *_: root.get('obyt') or 0
        ),
        'outPkts': GraphQLField(
            type=GraphQLInt,
            description='Output packets',
            resolver=lambda root, *_: root.get('opkt') or 0
        )
    }
)

IpConnectionDetailsType = GraphQLObjectType(
    name='NetflowIpConnectionDetailsType',
    fields={
        'srcIp': GraphQLField(
            type=GraphQLString,
            description='Source IP address',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=GraphQLString,
            description='Destination IP address',
            resolver=lambda root, *_: root.get('dstip')
        ),
        'inBytes': GraphQLField(
            type=GraphQLInt,
            description='Input bytes',
            resolver=lambda root, *_: root.get('ibyt') or 0
        ),
        'inPkts': GraphQLField(
            type=GraphQLInt,
            description='Input packets',
            resolver=lambda root, *_: root.get('ipkt') or 0
        )
    }
)

ScoredConnectionType = GraphQLObjectType(
    name='NetflowScoredConnectionType',
    fields={
        'tstart': GraphQLField(
            type=SpotDatetimeType,
            description='Time the flow was received by the flow collector',
            resolver=lambda root, *_: root.get('tstart')
        ),
        'srcIp': GraphQLField(
            type=SpotIpType,
            description='Source IP address',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'srcPort': GraphQLField(
            type=GraphQLInt,
            description='Source port',
            resolver=lambda root, *_: root.get('srcport') or 0
        ),
        'dstIp': GraphQLField(
            type=SpotIpType,
            description='Destination IP address',
            resolver=lambda root, *_: root.get('dstip')
        ),
        'dstPort': GraphQLField(
            type=GraphQLInt,
            description='Destionation port',
            resolver=lambda root, *_: root.get('dstport') or 0
        ),
        'score': GraphQLField(
            type=GraphQLInt,
            description='Risk score value. 1->High, 2->Medium, 3->Low',
            resolver=lambda root, *_: root.get('score') or 0
        )
    }
)

ThreatDetailsType = GraphQLObjectType(
    name='NetflowThreatDetailsType',
    fields={
        'firstSeen': GraphQLField(
            type=SpotDatetimeType,
            description='First time two IPs were seen on a particular day of flow traffic data',
            resolver=lambda root, *_: root.get('firstseen')
        ),
        'lastSeen': GraphQLField(
            type=SpotDatetimeType,
            description='Last time two IPs were seen on a particular day of flow traffic data',
            resolver=lambda root, *_: root.get('lastseen')
        ),
        'srcIp': GraphQLField(
            type=SpotIpType,
            description='Source IP address',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=SpotIpType,
            description='Destination IP address',
            resolver=lambda root, *_: root.get('dstip')
        ),
        'srcPort': GraphQLField(
            type=GraphQLInt,
            description='Source port',
            resolver=lambda root, *_: root.get('sport')
        ),
        'dstPort': GraphQLField(
            type=GraphQLInt,
            description='Destination port',
            resolver=lambda root, *_: root.get('dport')
        ),
        'connections': GraphQLField(
            type=GraphQLInt,
            description='Number of connections on a particular day of flow traffic data',
            resolver=lambda root, *_: root.get('conns')
        ),
        'maxPkts': GraphQLField(
            type=GraphQLInt,
            description='Maximum number of packets tranferred on a single connection',
            resolver=lambda root, *_: root.get('maxpkts')
        ),
        'avgPkts': GraphQLField(
            type=GraphQLInt,
            description='Average number of packets transferred bwteen IPs',
            resolver=lambda root, *_: root.get('avgpkts')
        ),
        'maxBytes': GraphQLField(
            type=GraphQLInt,
            description='Maximum number of bytes tranferred on a single connection',
            resolver=lambda root, *_: root.get('maxbyts')
        ),
        'avgBytes': GraphQLField(
            type=GraphQLInt,
            description='Average number of bytes transferred bwteen IPs',
            resolver=lambda root, *_: root.get('avgbyts')
        )
    }
)

CommentType = GraphQLObjectType(
    name='NetflowCommentType',
    fields={
        'ip': GraphQLField(
            type=SpotIpType,
            description='High risk IP address',
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
    name='NetflowThreatsType',
    fields={
        'list': GraphQLField(
            type=GraphQLList(ScoredConnectionType),
            description='List of suspicious IPs that have been scored',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference to retrieve the list of scored IPs. Defaults to today'
                )
            },
            resolver=lambda root, args, *
            _: Flow.get_scored_connections(date=args.get('date', date.today()))
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
            resolver=lambda root, args, *
            _: Flow.story_board(date=args.get('date', date.today()))
        )
    }
)

IncidentProgressionNodeType = create_spot_node_type(
    'NetflowIncidentProgressionNodeType')

ImpactAnalysisNodeType = create_spot_node_type('NetflowImpactAnalysisNodeType', {
    'size': GraphQLField(
        type=GraphQLInt,
        description='Number of inbound, outbound and two-way connections',
        resolver=lambda root, *_: root.get('size') or 0
    )
})

MapViewGeometryType = GraphQLObjectType(
    name='NetflowMapViewGeometryType',
    fields={
        'coordinates': GraphQLField(
            type=GraphQLList(GraphQLFloat),
            description='Geo latitude and longitude',
            resolver=lambda root, *_: root.get('coordinates')
        )
    }
)

MapViewPropertiesType = GraphQLObjectType(
    name='NetflowMapViewPropertiesType',
    fields={
        'ip': GraphQLField(
            type=SpotIpType,
            description='IP',
            resolver=lambda root, *_: root.get('ip')
        ),
        'location': GraphQLField(
            type=GraphQLString,
            description='Name of the IP\'s location',
            resolver=lambda root, *_: root.get('location')
        ),
        'type': GraphQLField(
            type=GraphQLInt,
            description='Property type',
            resolver=lambda root, *_: root.get('type')
        )
    }
)

MapViewIpType = GraphQLObjectType(
    name='NetflowMapViewIpType',
    fields={
        'geometry': GraphQLField(
            type=MapViewGeometryType,
            description='Geolocalization information',
            resolver=lambda root, *_: root.get('geometry')
        ),
        'properties': GraphQLField(
            type=MapViewPropertiesType,
            description='Metadata',
            resolver=lambda root, *_: root.get('properties')
        )
    }
)

MapViewType = GraphQLObjectType(
    name='NetflowMapViewType',
    fields={
        'srcIps': GraphQLField(
            type=GraphQLList(MapViewIpType),
            description='A list of source IPs',
            resolver=lambda root, *_: root.get('sourceips', [])
        ),
        'dstIps': GraphQLField(
            type=GraphQLList(MapViewIpType),
            description='A list of destination IPs',
            resolver=lambda root, *_: root.get('destips', [])
        )
    }
)

TimelineType = GraphQLObjectType(
    name='NetflowTimelineType',
    fields={
        'tstart': GraphQLField(
            type=GraphQLNonNull(SpotDatetimeType),
            description='Connection\'s start time',
            resolver=lambda root, *_: root.get('tstart')
        ),
        'tend': GraphQLField(
            type=GraphQLNonNull(SpotDatetimeType),
            description='Connection\'s end time',
            resolver=lambda root, *_: root.get('tend')
        ),
        'srcIp': GraphQLField(
            type=GraphQLNonNull(SpotIpType),
            description='Source IP address',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=GraphQLNonNull(SpotIpType),
            description='Destination IP address',
            resolver=lambda root, *_: root.get('dstip')
        ),
        'protocol': GraphQLField(
            type=GraphQLNonNull(GraphQLString),
            description='Connection\'s protocol',
            resolver=lambda root, *_: root.get('proto')
        ),
        'srcPort': GraphQLField(
            type=GraphQLNonNull(GraphQLInt),
            description='Source port',
            resolver=lambda root, *_: root.get('sport')
        ),
        'dstPort': GraphQLField(
            type=GraphQLNonNull(GraphQLInt),
            description='Destination port',
            resolver=lambda root, *_: root.get('dport')
        ),
        'pkts': GraphQLField(
            type=GraphQLNonNull(GraphQLInt),
            description='Packets tranferred between IPs',
            resolver=lambda root, *_: root.get('ipkt')
        ),
        'bytes': GraphQLField(
            type=GraphQLNonNull(GraphQLInt),
            description='Bytes tranferred between IPs',
            resolver=lambda root, *_: root.get('ibyt')
        )
    }
)

ThreatInformationType = GraphQLObjectType(
    name='NetflowThreatInformation',
    fields={
        'details': GraphQLField(
            type=GraphQLList(ThreatDetailsType),
            description='Detailed information about a high risk IP',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for high rist IP information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Suspicious IP'
                )
            },
            resolver=lambda root, args, *
            _: Flow.expanded_search(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'incidentProgression': GraphQLField(
            type=IncidentProgressionNodeType,
            description='Details for the type of connections that conform the activity related to the threat',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for incident progression information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Suspicious IP'
                )
            },
            resolver=lambda root, args, *
            _: Flow.incident_progression(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'impactAnalysis': GraphQLField(
            type=ImpactAnalysisNodeType,
            description='Contains the number of inbound, outbound and two-way connections found related to the suspicious IP',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for impact analysis information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Suspicious IP'
                )
            },
            resolver=lambda root, args, *
            _: Flow.impact_analysis(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'geoLocalization': GraphQLField(
            type=MapViewType,
            description='Gelocalization info about the IPs related to this threat',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for geo localization information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Suspicious IP'
                )
            },
            resolver=lambda root, args, *
            _: Flow.sc_geo(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'timeline': GraphQLField(
            type=GraphQLList(TimelineType),
            description='Lists \'clusters\' of inbound connections to the IP, grouped by time; showing an overall idea of the times during the day with the most activity',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for time line information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Suspicious Ip'
                )
            },
            resolver=lambda root, args, *
            _: Flow.time_line(date=args.get('date', date.today()), ip=args.get('ip'))
        )
    }
)

QueryType = GraphQLObjectType(
    name='NetflowQueryType',
    fields={
        'suspicious': GraphQLField(
            type=GraphQLList(SuspiciousType),
            description='Flow suspicious connections',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as a reference for suspicous connections. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=SpotIpType,
                    description='IP of interest'
                )
            },
            resolver=lambda root, args, *
            _: Flow.suspicious_connections(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'edgeDetails': GraphQLField(
            type=GraphQLList(EdgeDetailsType),
            description='Flow activity between two IPs around a particular moment in time',
            args={
                'tstart': GraphQLArgument(
                    type=GraphQLNonNull(SpotDatetimeType),
                    description='Time of interest'
                ),
                'srcIp': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Source IP address'
                ),
                'dstIp': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Destination IP address'
                )
            },
            resolver=lambda root, args, *_: Flow.details(
                                                date=args.get('tstart'),
                                                src_ip=args.get('srcIp'),
                                                dst_ip=args.get('dstIp'))
        ),
        'ipDetails': GraphQLField(
            type=GraphQLList(IpConnectionDetailsType),
            description='Flow activity details in between IP of interest and other suspicious IPs',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for IP network activity details. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='IP address of interest'
                )
            },
            resolver=lambda root, args, *
            _: Flow.chord_details(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'threats': GraphQLField(
            type=ThreatsInformationType,
            description='Advanced information about threats',
            resolver=lambda *_: {}
        ),
        'threat': GraphQLField(
            type=ThreatInformationType,
            description='Advanced information about a single threat',
            resolver=lambda *_: {}
        ),
        'ingestSummary': GraphQLField(
            type=GraphQLList(IngestSummaryType),
            description='Summary of ingested flows in range',
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
            resolver=lambda root, args, *_: Flow.ingest_summary(start_date=args.get('startDate'), end_date=args.get('endDate'))
        )
    }
)

TYPES = []
