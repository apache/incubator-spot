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

from api.graphql.common import SpotDateType, SpotDatetimeType, SpotIpType
from api.resources.flow import Flow

SuspiciousType = GraphQLObjectType(
    name='NetflowSuspiciousType',
    fields={
		'sev': GraphQLField(
			type=GraphQLInt,
			resolver=lambda root, *_: root.get('sev') or 0
		),
		'tstart': GraphQLField(
			type=GraphQLString,
			resolver=lambda root, *_: root.get('tstart')
		),
        'srcIp': GraphQLField(
            type= GraphQLString,
            resolver=lambda root, *_: root.get('srcIP')
        ),
        'dstIp': GraphQLField(
            type= GraphQLString,
            resolver=lambda root, *_: root.get('dstIP')
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
			resolver=lambda root, *_: root.get('score') or 0
		),
		'rank': GraphQLField(
			type=GraphQLInt,
			resolver=lambda root, *_: root.get('rank') or 0
		),
		'srcIp_isInternal': GraphQLField(
			type=GraphQLBoolean,
			resolver=lambda root, *_: root.get('srcip_internal')=='1'
		),
		'dstIp_isInternal': GraphQLField(
			type=GraphQLBoolean,
			resolver=lambda root, *_: root.get('dstip_internal')=='1'
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
			type=GraphQLString,
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
			resolver=lambda root, *_: root.get('ibytes') or 0
		),
		'inPkts': GraphQLField(
			type=GraphQLInt,
			resolver=lambda root, *_: root.get('ipkts') or 0
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
			resolver=lambda root, *_: root.get('obytes') or 0
        ),
		'outPkts': GraphQLField(
			type=GraphQLInt,
			resolver=lambda root, *_: root.get('obytes') or 0
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
			resolver=lambda root, *_: root.get('ibytes') or 0
		),
		'inPkts': GraphQLField(
			type=GraphQLInt,
			resolver=lambda root, *_: root.get('ipkts') or 0
		)
    }
)

QueryType = GraphQLObjectType(
    name='NetflowQueryType',
    fields={
        'suspicious': GraphQLField(
            type= GraphQLList(SuspiciousType),
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
            resolver=lambda root, args, *_: Flow.suspicious_connections(date=args.get('date', date.today()), ip=args.get('ip'))
        ),
        'edgeDetails': GraphQLField(
            type= GraphQLList(EdgeDetailsType),
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
            resolver=lambda root, args, *_: Flow.details(tstart=args.get('tstart'), src_ip=args.get('srcIp'), dst_ip=args.get('dstIp'))
        ),
        'ipDetails': GraphQLField(
            type= GraphQLList(IpConnectionDetailsType),
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
            resolver=lambda root, args, *_: Flow.chord_details(date=args.get('date', date.today()), ip=args.get('ip'))
        )
    }
)
