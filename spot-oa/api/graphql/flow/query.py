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
            resolver=lambda root, *_: root.get('tstart')
        ),
        'srcIp': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('dstip')
        ),
        'srcPort': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('sport') or 0
        ),
        'dstPort': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('dport') or 0
        ),
        'protocol': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('proto')
        ),
        'inPkts': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('ipkt') or 0
        ),
        'inBytes': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('ibyt') or 0
        ),
        'outPkts': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('opkt') or 0
        ),
        'outBytes': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('obyt') or 0
        ),
        'score': GraphQLField(
            type=GraphQLFloat,
            resolver=lambda root, *_: root.get('ml_score') or 0
        ),
        'rank': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('rank') or 0
        ),
        'srcIp_isInternal': GraphQLField(
            type=GraphQLBoolean,
            resolver=lambda root, *_: root.get('srcip_internal') == '1'
        ),
        'dstIp_isInternal': GraphQLField(
            type=GraphQLBoolean,
            resolver=lambda root, *_: root.get('dstip_internal') == '1'
        ),
        'srcIp_geoloc': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('src_geoloc')
        ),
        'dstIp_geoloc': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('dst_geoloc')
        ),
        'srcIp_domain': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('src_domain')
        ),
        'dstIp_domain': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('dst_domain')
        ),
        'srcIp_rep': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('src_rep')
        ),
        'dstIp_rep': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('dst_rep')
        )
    }
)

EdgeDetailsType = GraphQLObjectType(
    name='NetflowEdgeDetailsType',
    fields={
        'tstart': GraphQLField(
            type=SpotDatetimeType,
            resolver=lambda root, *_: root.get('tstart')
        ),
        'srcIp': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('dstip')
        ),
        'srcPort': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('sport')
        ),
        'dstPort': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('dport')
        ),
        'protocol': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('proto')
        ),
        'flags': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('flags')
        ),
        'tos': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('tos')
        ),
        'inBytes': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('ibyt') or 0
        ),
        'inPkts': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('ipkt') or 0
        ),
        'inIface': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('input')
        ),
        'outIface': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('output')
        ),
        'routerIp': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('rip')
        ),
        'outBytes': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('obyt') or 0
        ),
        'outPkts': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('opkt') or 0
        )
    }
)

IpConnectionDetailsType = GraphQLObjectType(
    name='NetflowIpConnectionDetailsType',
    fields={
        'srcIp': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=GraphQLString,
            resolver=lambda root, *_: root.get('dstip')
        ),
        'inBytes': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('ibyt') or 0
        ),
        'inPkts': GraphQLField(
            type=GraphQLInt,
            resolver=lambda root, *_: root.get('ipkt') or 0
        )
    }
)

ScoredConnectionType = GraphQLObjectType(
    name='NetflowScoredConnectionType',
    fields={
        'tstart': GraphQLField(
            type=SpotDatetimeType,
            resolver=lambda root, *_: root.get('tstart')
        ),
        'srcIp': GraphQLField(
            type=SpotIpType,
            description='Source Ip',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'srcPort': GraphQLField(
            type=GraphQLInt,
            description='Source port',
            resolver=lambda root, *_: root.get('srcport') or 0
        ),
        'dstIp': GraphQLField(
            type=SpotIpType,
            description='Destination Ip',
            resolver=lambda root, *_: root.get('dstip')
        ),
        'dstPort': GraphQLField(
            type=GraphQLInt,
            description='Destionation port',
            resolver=lambda root, *_: root.get('dstport') or 0
        ),
        'score': GraphQLField(
            type=GraphQLInt,
            description='Score value. 1->High, 2->Medium, 3->Low',
            resolver=lambda root, *_: root.get('score') or 0
        )
    }
)

ThreatDetailsType = GraphQLObjectType(
    name='NetflowThreatDetailsType',
    fields={
        'firstSeen': GraphQLField(
            type=SpotDatetimeType,
            description='First time two ips were seen on one day data of network traffic',
            resolver=lambda root, *_: root.get('firstseen')
        ),
        'lastSeen': GraphQLField(
            type=SpotDatetimeType,
            description='Last time two ips were seen on one day data of network trafic',
            resolver=lambda root, *_: root.get('lastseen')
        ),
        'srcIp': GraphQLField(
            type=SpotIpType,
            description='Source ip',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=SpotIpType,
            description='Destination ip',
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
            description='Number of connections on one day of network traffic',
            resolver=lambda root, *_: root.get('conns')
        ),
        'maxPkts': GraphQLField(
            type=GraphQLInt,
            description='Maximum number of packets tranferred on a single connection',
            resolver=lambda root, *_: root.get('maxpkts')
        ),
        'avgPkts': GraphQLField(
            type=GraphQLInt,
            description='Average number of packets transferred bwteen ips',
            resolver=lambda root, *_: root.get('avgpkts')
        ),
        'maxBytes': GraphQLField(
            type=GraphQLInt,
            description='Maximum number of bytes tranferred on a single connection',
            resolver=lambda root, *_: root.get('maxbyts')
        ),
        'avgBytes': GraphQLField(
            type=GraphQLInt,
            description='Average number of bytes transferred bwteen ips',
            resolver=lambda root, *_: root.get('avgbyts')
        )
    }
)

CommentType = GraphQLObjectType(
    name='NetflowCommentType',
    fields={
        'ip': GraphQLField(
            type=SpotIpType,
            resolver=lambda root, *_: root.get('ip_threat')
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
    name='NetflowThreatsType',
    fields={
        'list': GraphQLField(
            type=GraphQLList(ScoredConnectionType),
            description='List of connections that have been scored',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference to retrieve the list of scored connections. Defaults to today'
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
        description='Node size',
        resolver=lambda root, *_: root.get('size') or 0
    )
})

MapViewGeometryType = GraphQLObjectType(
    name='NetflowMapViewGeometryType',
    fields={
        'coordinates': GraphQLField(
            type=GraphQLList(GraphQLFloat),
            description='Geo Latitude and longitude',
            resolver=lambda root, *_: root.get('coordinates')
        )
    }
)

MapViewPropertiesType = GraphQLObjectType(
    name='NetflowMapViewPropertiesType',
    fields={
        'ip': GraphQLField(
            type=SpotIpType,
            description='Ip',
            resolver=lambda root, *_: root.get('ip')
        ),
        'location': GraphQLField(
            type=GraphQLString,
            description='Name of the ip\'s location',
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
            description='A list of source ips',
            resolver=lambda root, *_: root.get('sourceips', [])
        ),
        'dstIps': GraphQLField(
            type=GraphQLList(MapViewIpType),
            description='A list of destination ips',
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
            description='Source ip',
            resolver=lambda root, *_: root.get('srcip')
        ),
        'dstIp': GraphQLField(
            type=GraphQLNonNull(SpotIpType),
            description='Destination ip',
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
            description='Destionation port',
            resolver=lambda root, *_: root.get('dport')
        ),
        'pkts': GraphQLField(
            type=GraphQLNonNull(GraphQLInt),
            description='Packets tranferred between ips',
            resolver=lambda root, *_: root.get('ipkt')
        ),
        'bytes': GraphQLField(
            type=GraphQLNonNull(GraphQLInt),
            description='Bytes tranferred between ips',
            resolver=lambda root, *_: root.get('ibyt')
        )
    }
)

ThreatInformationType = GraphQLObjectType(
    name='NetflowThreatInformation',
    fields={
        'details': GraphQLField(
            type=GraphQLList(ThreatDetailsType),
            description='Detailed information about a high risk threat',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for incident progression information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Threat\'s Ip'
                )
            },
            resolver=lambda root, args, *
            _: Flow.expanded_search(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'incidentProgression': GraphQLField(
            type=IncidentProgressionNodeType,
            description='Incident progression information',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for incident progression information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Threat\'s Ip'
                )
            },
            resolver=lambda root, args, *
            _: Flow.incident_progression(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'impactAnalysis': GraphQLField(
            type=ImpactAnalysisNodeType,
            description='Impact analysis information',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for impact analysis information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Threat\'s Ip'
                )
            },
            resolver=lambda root, args, *
            _: Flow.impact_analysis(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'geoLocalization': GraphQLField(
            type=MapViewType,
            description='Gelocalization info about the ips related to this threat',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for geo localization information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Threat\'s Ip'
                )
            },
            resolver=lambda root, args, *
            _: Flow.sc_geo(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'timeline': GraphQLField(
            type=GraphQLList(TimelineType),
            description='Time based information about this threat',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for time line information. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Threat\'s Ip'
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
            description='Netflow Suspicious connections',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as a reference for suspicous connections. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=SpotIpType,
                    description='Ip of interest'
                )
            },
            resolver=lambda root, args, *
            _: Flow.suspicious_connections(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'edgeDetails': GraphQLField(
            type=GraphQLList(EdgeDetailsType),
            description='Network acitvity between two ips around a particular moment in time',
            args={
                'tstart': GraphQLArgument(
                    type=GraphQLNonNull(SpotDatetimeType),
                    description='Time of interest'
                ),
                'srcIp': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Source ip'
                ),
                'dstIp': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Destination ip'
                )
            },
            resolver=lambda root, args, *_: Flow.details(
                                                date=args.get('tstart'),
                                                src_ip=args.get('srcIp'),
                                                dst_ip=args.get('dstIp'))
        ),
        'ipDetails': GraphQLField(
            type=GraphQLList(IpConnectionDetailsType),
            description='Ip network activity details',
            args={
                'date': GraphQLArgument(
                    type=SpotDateType,
                    description='A date to use as reference for ip network activity details. Defaults to today'
                ),
                'ip': GraphQLArgument(
                    type=GraphQLNonNull(SpotIpType),
                    description='Ip of interest'
                )
            },
            resolver=lambda root, args, *
            _: Flow.chord_details(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'threats': GraphQLField(
            type=ThreatsInformationType,
            description='Advanced inforamtion about threats',
            resolver=lambda *_: {}
        ),
        'threat': GraphQLField(
            type=ThreatInformationType,
            description='Advanced inforamtion about a single threat',
            resolver=lambda *_: {}
        ),
        'ingestSummary': GraphQLField(
            type=GraphQLList(IngestSummaryType),
            description='Total of ingested netflows',
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
