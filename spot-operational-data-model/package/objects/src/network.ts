export interface Network {
    // src_ip4/src_ip6	bigint	Source ip address of event	Integer representation of 10.1.1.1
    // src_host	string	Source FQDN of event	test.companyA.com
    // src_domain	string	Domain name of source address	companyA.com
    // src_port	int	Source port of event	1025
    // src_country_code	string	Source country code	cn
    // src_country_name	string	Source country name	China
    // src_region	string	Source region	string
    // src_city	string	Source city	Shenghai
    // src_lat	int	Source latitude	90
    // src_long	int	Source longitude	90
    // dst_ip4/dst_ip6	bigint	Destination ip address of event	Integer representation of 10.1.1.1
    // dst_host	string	Destination FQDN of event	test.companyA.com
    // dst_domain	string	Domain name of destination address	companyA.com
    // dst_port	int	Destination port of event	80
    // dst_country_code	string	Source country code	cn
    // dst_country_name	string	Source country name	China
    // dst_region	string	Source region	string
    // dst_city	string	Source city	Shenghai
    // dst_lat	int	Source latitude	90
    // dst_long	int	Source longitude	90
    // src_asn	int	Autonomous system number	33
    // dst_asn	int	Autonomous system number	33
    // net_direction	string	Direction	In, inbound, outbound, ingress, egress
    // net_flags	string	TCP flags	.AP.SF
}
