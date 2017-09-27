**Open Data Model (ODM)**
=========================

**Overview**
------------

This document describes a strategy for creating an open data model (ODM) for
Apache Spot (incubating) (formerly known as “Open Network Insight (ONI)”) in
support of cyber security analytic use cases. It also describes the use cases
for which Apache Spot (incubating) running on the Cloudera platform is uniquely
capable of addressing along with the data model.

**Apache Spot (incubating) Open Data Model Strategy**
-----------------------------------------------------

The Apache Spot (incubating) Open Data Model (ODM) strategy aims to extend
Apache Spot (incubating) capabilities to support a broader set of cyber security
use cases than initially supported. The primary use case initially supported by
Apache Spot (incubating) includes Network Traffic Analysis for network flows
(Netflow, sflow, etc.), DNS and Proxy; primarily the identification of threats
through anomalous event detection using both supervised and unsupervised machine
learning.

In order to support a broader set of use cases, Spot must be extended to collect
and analyze other common “event-oriented” data sources analyzed for cyber
threats, including but not limited to the following log types:

-   Proxy

-   Web server

-   Operating system

-   Firewall

-   Intrusion Prevention/Detection (IDS/ IPS)

-   Data Loss Prevention

-   Active Directory / Identity Management

-   User/Entity Behavior Analysis

-   Endpoint Protection/Asset Management

-   Network Metadata/Session and PCAP files

-   Network Access Control

-   Mail

-   VPN

-   etc..

One of the biggest challenges organizations face today in combating cyber
threats is collecting and normalizing data from the myriad of security event
data sources (hundreds) in order to build the needed analytics. This often
results in the analytics being dependent upon the specific technologies used by
an organization to detect threats and prevents the needed flexibility and
agility to keep up with these ever-increasing (and complex) threats. Technology
lock-in is sometimes a byproduct of today’s status quo, as it’s extremely costly
to add new technologies (or replace existing ones) because of the downstream
analytic dependencies.

To achieve the goal of extending Apache Spot (incubating) to support additional
use cases, it is necessary to create an open data model for the most relevant
security event and contextual data sources; Security event logs or alerts,
Network context, User details and information that comes from the endpoints or
any other console that are being use to manage the security / administration of
our endpoints. The presence of an open data model, which can be applied
“on-read” or “on-write”, in batch or stream, will allow for the separation of
security analytics from the specific data sources on which they are built. This
“separation of duties” will enable organizations to build analytics that are not
dependent upon specific technologies and provide the flexibility to change
underlying data sources and also provide segmentation of this information,
without impacting the analytics. This will also afford security vendors the
opportunity to build additional products on top of the Open Data Model to drive
new revenue streams and also to design new ways to detect threats and APT.

**Apache Spot (incubating) Enabled Use Cases**
----------------------------------------------

Spot on the Cloudera platform is uniquely positioned to help address the
following cyber security use cases, which are not effectively addressed by
legacy technologies:

**- Detection of known & unknown threats leveraging machine learning and
advanced analytic modeling**

Current technologies are limited in the analytics they can apply to detect
threats. These limitations stem from the inability to collect all the data
sources needed to effectively identify threats (structured, unstructured, etc.)
and inability to process the massive volumes of data needed to do so (billions
of events per day). Legacy technologies are typically focus and limited to
rules-based and signature detection. They are somewhat “effective” at detecting
known threats but struggle with new threats.

Spot addresses these gaps through its ability to collect any data type of any
volume. Coupled with the various analytic frameworks that are provided
(including machine learning), Spot enables a whole new class of analytics that
can scale to today’s demands. The topic model used by Spot to detect anomalous
network traffic is one example of where the Spot platform excels.

**- Reduction of mean time to incident detection & resolution (MTTR)**

One of the challenges organizations face today is detecting threats early enough
to minimize adverse impacts. This stems from the limitations previously
discussed with regards to limited analytics. It can also be attributed to the
fact that most of the investigative queries often take hours or days to return
results. Legacy technologies can’t offer or have a central data store for
facilitating such investigations due to their inability to store and serve the
massive amounts of data involved. This cripples incident investigations and
results in MTTRs of many weeks or months, meanwhile the adverse impacts of the
breach are magnified, thus making the threat harder to eradicate.

Apache Spot (incubating) addresses these gaps by providing the capability for a
central data store that houses ALL the data needed to facilitate an
investigation, returning investigative query results in seconds and minutes (vs.
hours and days). Spot can effectively reduce incident MTTR and reduce adverse
impacts of a breach.

**- Threat Hunting**

It’s become necessary for organizations to “hunt” for active threats as
traditional passive threat detection approaches are not sufficient. “Hunting”
involves performing ad-hoc searches and queries over vast amounts of data
representing many weeks and months’ worth of events, as well as applying ad-hoc
/ tune algorithms to detect the needle in the haystack. Traditional systems do
not perform well for these types of activities as the query results sometimes
take hours and days to be retrieved. These traditional systems also lack the
analytic flexibility to construct the necessary algorithms and logic needed.

Apache Spot (incubating) addresses these gaps in the same ways it addresses
others; by providing a central data store with the needed analytic frameworks
that scale to the needed workloads.

**Data Model**
--------------

In order to provide a framework for effectively analyzing data for cyber
threats, it is necessary to collect and analyze standard security event
logs/alerts and contextual data regarding the entities referenced in these
logs/alerts. The most common entities include network, user and endpoint, but
there are others such as file.

In the diagram below, the raw event tells us that user “jsmith” successfully
logged in to an Oracle database from the IP address 10:1.1.3. Based on the raw
event only, we don’t know if this event is a legitimate threat or not. After
injecting user and endpoint context, the enriched event tells us this event is a
potential threat that requires further investigation.

![](https://lh3.googleusercontent.com/-Q8TasmY-vRQ/WHVnoXAK44I/AAAAAAAAAtw/XBDy3PC98k800iaWpNIzAYoQ8S9zc5NBQCLcB/s0/ODMimage1.jpg)

Based on the need to collect and analyze both security events, logs or alerts
and contextual data, support for the following types of security information are
planned for inclusion in the Spot Open Data Model:

-   Security event logs/alerts This data type includes event logs from common
    data sources used to detect threats and includes network flows, operating
    system logs, IPS/IDS logs, firewall logs, proxy logs, web logs, DLP logs,
    etc.

-   Network context data This data type includes information about the network,
    which can be gleaned from Whois servers, asset databases and other similar
    data sources.

-   User context data This data type includes information from user and identity
    management systems including Active Directory, Centrify, and other identity
    and access management systems.

-   Endpoint context data This data includes information about endpoint systems
    (servers, workstations, routers, switches, etc.) and can be sourced from
    asset management systems, vulnerability scanners, and endpoint
    management/detection/response systems such as Webroot, Tanium, Sophos,
    Endgame, CarbonBlack, Intel Security ePO and others.

-   File context data **(ROADMAP ITEM)** This data includes contextual
    information about files and can be sourced from systems such as FireEye,
    Application Control , Intel Security McAfee Threat Intelligence Exchange
    (TIE).

-   Threat intelligence context data **(ROADMAP ITEM)** This data includes
    contextual information about URLs, domains, websites, files and others.

**Naming Convention**
---------------------

A naming convention is needed for the Open Data Model to represent common
attributes across vendor products and technologies. The naming convention is
described below.

**Prefixes**
------------

| Prefix   | Description                                                                                                                       |
|----------|-----------------------------------------------------------------------------------------------------------------------------------|
| src      | Corresponds to the “source” fields within a given event (i.e. source address)                                                     |
| dst      | Corresponds to the “destination” fields within a given event (i.e. destination address)                                           |
| dvc      | Corresponds to the “device” applicable fields within a given event (i.e. device address) and represent where the event originated |
| fwd      | Forwarded from device                                                                                                             |
| request  | Corresponds to requested values (vs. those returned, i.e. “requested URI”)                                                        |
| response | Corresponds to response value (vs. those requested)                                                                               |
| file     | Corresponds to the “file” fields within a given event (i.e. file type)                                                            |
| user     | Corresponds to user attributes (i.e. name, id, etc.)                                                                              |
| xlate    | Corresponds to translated values within a given event (i.e. src_xlate_ip for “translated source ip address”                       |
| in       | Ingress                                                                                                                           |
| out      | Egress                                                                                                                            |
| new      | New value                                                                                                                         |
| orig     | Original value                                                                                                                    |
| app      | Corresponds to values associated with application events                                                                          |

**Security Event Log/Alert Data Model**
---------------------------------------

The data model for security event logs/alerts is detailed in the below. The
attributes are categorized as follows:

-   Common -attributes that are common across many device types

-   Device -attributes that are applicable to the device that generated the
    event

-   File -attributes that are applicable to file objects referenced in the event

-   Endpoint -attributes that are applicable to the endpoints referenced in the
    event

-   User- attributes that are applicable to the user referenced in the event

-   Proxy - attributes that are applicable to proxy events

-   Protocol

-   DNS - attributes that are specific to DNS events

-   HTTP - attributes that are specific to HTTP events

-   SMTP, SSH, TLS, DHCP, IRC, SNMP and FTP

Note: The model will evolve to include reserved attributes for additional device
types that are not currently represented. The model can currently be extended to
support ANY attribute for ANY device type by following the guidance outlined in
the section titled [“Extensibility of Data Model”.](#extensibility)

Note: Attributes denoted in **Bold**, represent those that are listed in the
model multiple times for the purpose of demonstrating attribute coverage for a
particular entity (endpoint, user, network, etc.) or log type (Proxy, DNS,
etc.).

| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
|--------------|---------------------------|-------------------|-----------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| **Common**   | eventtime                 | long              | timestamp of event (UTC)                                              | 1472653952                                                                          |
|              | duration                  | int               | Time duration (milliseconds)                                          | 2345                                                                                |
|              | eventid                   | string            | Unique identifier for event                                           | x:2388                                                                              |
|              | org                       | string            | Organization                                                          | “HR” or “Finance” or “CustomerA”                                                    |
|              | type                      | string            | Type information                                                      | “Informational”, “image/gif”                                                        |
|              | nproto                    | string            | Network protocol of event                                             | TCP, UDP, ICMP                                                                      |
|              | aproto                    | string            | Application protocol of event                                         | HTTP, NFS, FTP                                                                      |
|              | msg                       | string            | Message (details of action taken on object)                           | Some long string                                                                    |
|              | mac                       | string            | MAC address                                                           | 94:94:26:3:86:16                                                                    |
|              | severity                  | string            | Severity of event                                                     | High, 10, 1                                                                         |
|              | raw                       | string            | Raw text message of entire event                                      | Complete copy of log entry                                                          |
|              | risk                      | Floating point    | Risk score                                                            | 95.67                                                                               |
|              | code                      | string            | Response or error code                                                | 404                                                                                 |
|              | category                  | string            | Event category                                                        | /Application/Start                                                                  |
|              | qry                       | string            | Query (DNS query, URI query, SQL query, etc.)                         | Select \* from "table"                                                              |
|              | service                   | string            | (i.e. service name, type of service)                                  | sshd                                                                                |
|              | state                     | string            | State of object                                                       | Running, Paused, stopped                                                            |
|              | in_bytes                  | int               | Bytes in                                                              | 1025                                                                                |
|              | out_bytes                 | int               | Bytes out                                                             | 9344                                                                                |
|              | additional_attrs          | String (JSON Map) | Custom event attributes                                               | "building":"729","cube":"401"                                                       |
|              | dvc_time                  | long              | UTC timestamp from device where event/alert originates or is received | 1472653952                                                                          |
|              | dvc_ip4/dvc_ip6           | long              | IP address of device                                                  | Integer representaion of 10.1.1.1                                                   |
|              | dvc_host                  | string            | Hostname of device                                                    | Integer representaion of 10.1.1.1                                                   |
|              | dvc_type                  | string            | Device type that generated the log                                    | Unix, Windows, Sonicwall                                                            |
|              | dvc_vendor                | string            | Vendor                                                                | Microsoft, Fireeye, Intel Security                                                  |
|              | dvc_version               | string            | Version                                                               | 5.4                                                                                 |
|              | fwd_ip4/fwd_ip6           | long              | Forwarded from device                                                 | Integer representation of 10.1.1.1                                                  |
|              | version                   | string            | Version                                                               | “3.2.2”                                                                             |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **Network**  | src_ip4/src_ip6           | bigint            | Source ip address of event                                            | Integer representation of 10.1.1.1                                                  |
|              | src_host                  | string            | Source FQDN of event                                                  | test.companyA.com                                                                   |
|              | src_domain                | string            | Domain name of source address                                         | companyA.com                                                                        |
|              | src_port                  | int               | Source port of event                                                  | 1025                                                                                |
|              | src_country_code          | string            | Source country code                                                   | cn                                                                                  |
|              | src_country_name          | string            | Source country name                                                   | China                                                                               |
|              | src_region                | string            | Source region                                                         | string                                                                              |
|              | src_city                  | string            | Source city                                                           | Shenghai                                                                            |
|              | src_lat                   | int               | Source latitude                                                       |                                                                                     |
|              | src_long                  | int               | Source longitude                                                      |                                                                                     |
|              | dst_ip4/dst_ip6           | bigint            | Destination ip address of event                                       | Integer representaion of 10.1.1.1                                                   |
|              | dst_host                  | string            | Destination FQDN of event                                             | test.companyA.com                                                                   |
|              | dst_domain                | string            | Domain name of destination address                                    | companyA.com                                                                        |
|              | dst_port                  | int               | Destination port of event                                             | 80                                                                                  |
|              | dst_country_code          | string            | Source country code                                                   | cn                                                                                  |
|              | dst_country_name          | string            | Source country name                                                   | China                                                                               |
|              | dst_region                | string            | Source region                                                         | string                                                                              |
|              | dst_city                  | string            | Source city                                                           | Shenghai                                                                            |
|              | dst_lat                   | int               | Source latitude                                                       |                                                                                     |
|              | dst_long                  | int               | Source longitude                                                      |                                                                                     |
|              | asn                       | int               | Autonomous system number                                              | 33                                                                                  |
|              | **in_bytes**              | int               | Bytes in                                                              | 987                                                                                 |
|              | **out_bytes**             | int               | Bytes out                                                             | 1222                                                                                |
|              | direction                 | string            | Direction                                                             | In, inbound, outbound, ingress, egress                                              |
|              | flags                     | string            | TCP flags                                                             | .AP.SF                                                                              |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **File**     | file_name                 | string            | Filename from event                                                   | output.csv                                                                          |
|              | file_path                 | string            | File path                                                             | /root/output.csv                                                                    |
|              | file_atime                | bigint            | Timestamp (UTC) of file access                                        | 1472653952                                                                          |
|              | file_acls                 | string            | File permissions                                                      | rwx-rwx-rwx                                                                         |
|              | file_type                 | string            | Type of file                                                          | “.doc”                                                                              |
|              | file_size                 | int               | Size of file in bytes                                                 | 1244                                                                                |
|              | file_desc                 | string            | Description of file                                                   | Project Plan for Project xyz                                                        |
|              | file_hash                 | string            | Hash of file                                                          |                                                                                     |
|              | file_hash_type            | string            | Type of hash                                                          | MD5, SHA1,SHA256                                                                    |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **Endpoint** | object                    | string            | File/Process/Registry                                                 | File, Registry, Process                                                             |
|              | action                    | string            | Action taken on object (open/delete/edit)                             | Open, Edit                                                                          |
|              | **msg**                   | string            | Message (details of action taken on object)                           | Some long string                                                                    |
|              | app                       | string            | Application                                                           | Microsoft Powerpoint                                                                |
|              | location                  | string            | Location                                                              | Atlanta, GA                                                                         |
|              | proc                      | string            | Process                                                               | SSHD                                                                                |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **User**     | user_name                 | string            | username from event                                                   | mhicks                                                                              |
|              | email                     | string            | Email address                                                         | test\@companyA.com                                                                  |
|              | user_id                   | string            | userid                                                                | 234456                                                                              |
|              | user_loc                  | string            | location                                                              | Herndon, VA                                                                         |
|              | user_desc                 | string            | Description of user                                                   |                                                                                     |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **DNS**      | dns_class                 | string            | DNS class                                                             | 1                                                                                   |
|              | dns_length                | int               | DNS frame length                                                      | 188                                                                                 |
|              | **dns_qry**               | string            | Requested DNS query                                                   | test.test.com                                                                       |
|              | **dns_code**              | string            | Response code                                                         | 0x00000001                                                                          |
|              | dns_response_qry          | string            | Response to DNS Query                                                 | 178.2.1.99                                                                          |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **Proxy**    | **category**              | string            | Event category                                                        | SG-HTTP-SERVICE                                                                     |
|              | browser                   | string            | Web browser                                                           | Internet Explorer                                                                   |
|              | **code**                  | string            | Error or response code                                                | 404                                                                                 |
|              | **in_bytes**              | int               | Bytes in                                                              | 1025                                                                                |
|              | **out_bytes**             | int               | Bytes out                                                             | 1288                                                                                |
|              | referrer                  | string            | Referrer                                                              | www.usatoday.com                                                                    |
|              | **request_uri**           | string            | Requested URI                                                         | /wcm/assets/images/imagefileicon.gif                                                |
|              | filter_rule               | string            | Applied filter or rule                                                | Internet, Rule 6                                                                    |
|              | filter_result             | string            | Result of applied filter or rule                                      | Proxied, Blocked                                                                    |
|              | **qry**                   | string            | URI query                                                             | ?func=S_senseHTML&Page=a26815a313504697a126279                                      |
|              | **action**                | string            | Action taken on object                                                | TCP_HIT, TCP_MISS, TCP_TUNNELED                                                     |
|              | method                    | string            | HTTP method                                                           | GET, CONNECT, POST                                                                  |
|              | **type**                  | string            | Type of request                                                       | image/gif                                                                           |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **HTTP**     | request_method            | string            | HTTP method                                                           | GET, CONNECT, POST                                                                  |
|              | **request_uri**           | string            | Requested URI                                                         | /wcm/assets/images/imagefileicon.gif                                                |
|              | request_body_len          | int               | Length of request body                                                | 98                                                                                  |
|              | request_user_name         | string            | username from event                                                   | mhicks                                                                              |
|              | request_password          | string            | Password from event                                                   | abc123                                                                              |
|              | request_proxied           | string            |                                                                       |                                                                                     |
|              | request_headers           | MAP               | HTTP request headers                                                  | request_headers[‘HOST’] request_headers[‘USER-AGENT’] request_headers[‘ACCEPT’]     |
|              | response_status_code      | int               | HTTP response status code                                             | 404                                                                                 |
|              | response_status_msg       | string            | HTTP response status message                                          | “Not found”                                                                         |
|              | response_body_len         | int               | Length of response body                                               | 98                                                                                  |
|              | response_info_code        | int               | HTTP response info code                                               | 100                                                                                 |
|              | response_info_msg         | string            | HTTP response info message                                            | “Some string”                                                                       |
|              | response_resp_fuids       | string            | Response FUIDS                                                        |                                                                                     |
|              | response_mime_types       | string            | Mime types                                                            | “cgi,bat,exe”                                                                       |
|              | response_headers          | MAP               | Response headers                                                      | response_headers[‘SERVER’] response_headers[‘SET-COOKIE’’] response_headers[‘DATE’] |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **SMTP**     | trans_depth               | int               | Depth of email into SMTP exchange                                     | Coming soon                                                                         |
|              | headers_helo              | string            | Helo header                                                           | Coming soon                                                                         |
|              | headers_mailfrom          | string            | Mailfrom header                                                       | Coming soon                                                                         |
|              | headers_rcptto            | string            | Rcptto header                                                         | Coming soon                                                                         |
|              | headers_date              | string            | Header date                                                           | Coming soon                                                                         |
|              | headers_from              | string            | From header                                                           | Coming soon                                                                         |
|              | headers_to                | string            | To header                                                             | Coming soon                                                                         |
|              | headers_reply_to          | string            | Reply to header                                                       | Coming soon                                                                         |
|              | headers_msg_id            | string            | Message ID                                                            | Coming soon                                                                         |
|              | headers_in_reply_to       | string            | In reply to header                                                    | Coming soon                                                                         |
|              | headers_subject           | string            | Subject                                                               | Coming soon                                                                         |
|              | headers_x_originating_ip4 | bigint            | Originating IP address                                                | Coming soon                                                                         |
|              | headers_first_received    | string            | First to receive message                                              | Coming soon                                                                         |
|              | headers_second_received   | string            | Second to receive message                                             | Coming soon                                                                         |
|              | last_reply                | string            | Last reply in message chain                                           | Coming soon                                                                         |
|              | path                      | string            | Path of message                                                       | Coming soon                                                                         |
|              | user_agent                | string            | User agent                                                            | Coming soon                                                                         |
|              | tls                       | boolean           | Indication of TLS use                                                 | Coming soon                                                                         |
|              | is_webmail                | boolean           | Indication of webmail                                                 | Coming soon                                                                         |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **FTP**      | **user_name**             | string            | Username                                                              | Coming soon                                                                         |
|              | password                  | string            | Password                                                              | Coming soon                                                                         |
|              | command                   | string            | FTP command                                                           | Coming soon                                                                         |
|              | arg                       | string            | Argument                                                              | Coming soon                                                                         |
|              | mime_type                 | string            | Mime type                                                             | Coming soon                                                                         |
|              | file_size                 | int               | File size                                                             | Coming soon                                                                         |
|              | reply_code                | int               | Reply code                                                            | Coming soon                                                                         |
|              | reply_msg                 | string            | Reply message                                                         | Coming soon                                                                         |
|              | data_channel_passive      | boolean           | Passive data channel?                                                 | Coming soon                                                                         |
|              | data_channel_rsp_p        | string            |                                                                       | Coming soon                                                                         |
|              | cwd                       | string            | Current working directory                                             | Coming soon                                                                         |
|              | cmdarg_ts                 | float             |                                                                       | Coming soon                                                                         |
|              | cmdarg_cmd                | string            | Command                                                               | Coming soon                                                                         |
|              | cmdarg_arg                | string            | Command argument                                                      | Coming soon                                                                         |
|              | cmdarg_seq                | int               | Sequence                                                              | Coming soon                                                                         |
|              | pending_commands          | string            | Pending commands                                                      | Coming soon                                                                         |
|              | is_passive                | boolean           | Passive mode enabled                                                  | Coming soon                                                                         |
|              | fuid                      | string            | Coming soon                                                           | Coming soon                                                                         |
|              | last_auth_requested       | string            | Coming soon                                                           | Coming soon                                                                         |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **SNMP**     | **version**               | string            | Coming soon                                                           | Coming soon                                                                         |
|              | community                 | string            | Coming soon                                                           | Coming soon                                                                         |
|              | get_requests              | int               | Coming soon                                                           | Coming soon                                                                         |
|              | get_bulk_requests         | int               | Coming soon                                                           | Coming soon                                                                         |
|              | get_responses             | int               | Coming soon                                                           | Coming soon                                                                         |
|              | set_requests              | int               | Coming soon                                                           | Coming soon                                                                         |
|              | display_string            | string            | Coming soon                                                           | Coming soon                                                                         |
|              | up_since                  | float             | Coming soon                                                           | Coming soon                                                                         |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **TLS**      | **version**               | string            | Coming soon                                                           | Coming soon                                                                         |
|              | cipher                    | string            | Coming soon                                                           | Coming soon                                                                         |
|              | curve                     | string            | Coming soon                                                           | Coming soon                                                                         |
|              | server_name               | string            | Coming soon                                                           | Coming soon                                                                         |
|              | resumed                   | boolean           | Coming soon                                                           | Coming soon                                                                         |
|              | next_protocol             | string            | Coming soon                                                           | Coming soon                                                                         |
|              | established               | boolean           | Coming soon                                                           | Coming soon                                                                         |
|              | cert_chain_fuids          | string            | Coming soon                                                           | Coming soon                                                                         |
|              | client_cert_chain_fuids   | string            | Coming soon                                                           | Coming soon                                                                         |
|              | subject                   | string            | Coming soon                                                           | Coming soon                                                                         |
|              | issuer                    | string            | Coming soon                                                           | Coming soon                                                                         |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **SSH**      | **version**               | string            | Coming soon                                                           | Coming soon                                                                         |
|              | auth_success              | boolean           | Coming soon                                                           | Coming soon                                                                         |
|              | client                    | string            | Coming soon                                                           | Coming soon                                                                         |
|              | server                    | string            | Coming soon                                                           | Coming soon                                                                         |
|              | cipher_algorithm          | string            | Coming soon                                                           | Coming soon                                                                         |
|              | mac_algorithm             | string            | Coming soon                                                           | Coming soon                                                                         |
|              | compression_algorithm     | string            | Coming soon                                                           | Coming soon                                                                         |
|              | key_exchange_algorithm    | string            | Coming soon                                                           | Coming soon                                                                         |
|              | host_key_algorithm        | string            | Coming soon                                                           | Coming soon                                                                         |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **DHCP**     | assigned_ip4              | bigint            | Coming soon                                                           | Coming soon                                                                         |
|              | mac                       | string            | Coming soon                                                           | Coming soon                                                                         |
|              | lease_time                | double            | Coming soon                                                           | Coming soon                                                                         |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **IRC**      | user                      | string            | Coming soon                                                           | Coming soon                                                                         |
|              | nickname                  | string            | Coming soon                                                           | Coming soon                                                                         |
|              | command                   | string            | Coming soon                                                           | Coming soon                                                                         |
|              | value                     | string            | Coming soon                                                           | Coming soon                                                                         |
|              | additional_data           | string            | Coming soon                                                           | Coming soon                                                                         |
| **Category** | **Attribute**             | **Data Type**     | **Description**                                                       | **Sample Values**                                                                   |
| **Flow**     | in_packets                | int               | Coming soon                                                           | Coming soon                                                                         |
|              | out_packets               | int               | Coming soon                                                           | Coming soon                                                                         |
|              | **in_bytes**              | int               | Coming soon                                                           | Coming soon                                                                         |
|              | **out_bytes**             | int               | Coming soon                                                           | Coming soon                                                                         |
|              | conn_state                | string            | Coming soon                                                           | Coming soon                                                                         |
|              | history                   | string            | Coming soon                                                           | Coming soon                                                                         |
|              | duration                  | float             | Coming soon                                                           | Coming soon                                                                         |
|              | src_os                    | string            | Coming soon                                                           | Coming soon                                                                         |
|              | dst_os                    | string            | Coming soon                                                           | Coming soon                                                                         |

Note: It is not necessary to populate all of the attributes within the model.
For attributes not populated in a single security event log/alert, contextual
data may not be available. For example, the sample event below can be enriched
with contextual data about the referenced endpoints (10.1.1.1 and
192.168.10.10), but not a user, because username is not populated.

>   **date,time,source_ip,source_port,protocol,destination_ip,destination_port,bytes
>   12/12/2015,23:14:56,10.1.1.1,1025,tcp,192.168.10.10,443,1183**

**Context Models**
==================

The recommended approach for populating the context models (user, endpoint,
network, etc.) involves consuming information from the systems most capable or
providing the needed context. Populating the user context model is best
accomplished by leveraging user/identity management systems such as Active
Directory or Centrify and populating the model with details such as the user’s
full name, job title, phone number, manager’s name, physical address,
entitlements, etc. Similarly, an endpoint model can be populated by consuming
information from endpoint/asset management systems (Tanium, Webroot, etc.),
which provide information such as the services running on the system, system
owner, business context, etc.

**User Context Model**
----------------------

The data model for user context information is as follows:

| **Attribute**    | **Data Type**                                        | **Description**                                              | **Sample Values**                   |
|------------------|------------------------------------------------------|--------------------------------------------------------------|-------------------------------------|
| dvc_time         | bigint                                               | Timestamp from when the user context information is obtained | 1472653952                          |
| created          | bigint                                               | Timestamp from when user was created                         | 1472653952                          |
| Changed––––      | bigint                                               | Timestamp from when user was updated                         | 1472653952                          |
| lastlogon        | bigint                                               | Timestamp from when user last logged on                      | 1472653952                          |
| logoncount       | int                                                  | Number of times account has logged on                        | 232                                 |
| lastreset        | bigint                                               | Timestamp from when user last reset passwod                  | 1472653952                          |
| expiration       | bigint                                               | Date/time when user expires                                  | 1472653952                          |
| userid           | string                                               | Unique user id                                               | 1234                                |
| username         | string                                               | Username in event log/alert                                  | jsmith                              |
| name_first       | string                                               | First name                                                   | John                                |
| name_middle      | string                                               | Middle name                                                  | Henry                               |
| name_last        | string                                               | Last name                                                    | Smith                               |
| name_mgr         | string                                               | Manager’s name                                               | Ronald Reagan                       |
| phone            | string                                               | Phone number                                                 | 703-555-1212                        |
| email            | string                                               | Email address                                                | jsmith\@company.com                 |
| code             | string                                               | Job code                                                     | 3455                                |
| loc              | string                                               | Location                                                     | US                                  |
| departm          | string                                               | Department                                                   | IT                                  |
| dn               |                                                      | Distinguished name                                           | "CN=scm-admin-mej-test2-adk,OU=app- |
| ou               | string                                               | Organizational unit                                          | EAST                                |
| empid            | string                                               | Employee ID                                                  | 12345                               |
| title            | string                                               | Job Title                                                    | Director of IT                      |
| groups           | string (comma separated list, no spaces after comma) | Groups to which the user belongs                             | “Domain Admins”, “Domain Users”     |
| dvc_type         | string                                               | Device type that generated the user context data             | Active Directory                    |
| dvc_vendor       | string                                               | Vendor                                                       | Microsoft                           |
| dvc_version      | string                                               | Version                                                      | 8.1.2                               |
| additional_attrs | string                                               | Additional attributes of user                                | Key value pairs                     |

**Endpoint Context Model**
--------------------------

The data model for endpoint context information is as follows:

| **Abbreviation** | **Data Type**                                    | **Description**                                                  | **Sample Values**                                    |
|------------------|--------------------------------------------------|------------------------------------------------------------------|------------------------------------------------------|
| dvc_time         | bigint                                           | Timestamp from when the endpoint context information is obtained | 1472653952                                           |
| ip4              | bigint                                           | IP address of endpoint                                           | Integer representaion of 10.1.1.1                    |
| ip6              | bigint                                           | IP address of endpoint                                           | Integer representaion of 10.1.1.1                    |
| os               | string                                           | Operating system                                                 | Redhat Linux 6.5.1                                   |
| os_version       | string                                           | Version of OS                                                    | 5.4                                                  |
| os_sp            | string                                           | Service pack                                                     | SP 2.3.4.55                                          |
| tz               | string                                           | timezone                                                         | EST                                                  |
| hotfixes         | string                                           | Applied hotfixes                                                 | 993.2                                                |
| disks            | string                                           | Available disks                                                  | \\Device\\HarddiskVolume1, \\Device\\HarddiskVolume2 |
| removables       | string                                           | Removable media devices                                          | USB Key                                              |
| nics             | string                                           | Network interfaces                                               | fe10::28f4:1a47:658b:d6e8, fe82::28f4:1a47:658b:d6e8 |
| drivers          | string                                           | Installed kernel drivers                                         | ntoskrnl.exe, hal.dll                                |
| users            | string                                           | Local user accounts                                              | administrator, jsmith                                |
| host             | string                                           | Hostname of endpoint                                             | tes1.companya.com                                    |
| mac              | string                                           | MAC address of endpoint                                          | fe10::28f4:1a47:658b:d6e8                            |
| owner            | string                                           | Endpoint owner (name)                                            | John Smith                                           |
| vulns            | string (comma separated, no spaces after commas) | Vulnerability identifiers (CVE identifier)                       | CVE-123, CVE-456                                     |
| loc              | string                                           | Location                                                         | US                                                   |
| departm          | string                                           | Department name                                                  | IT                                                   |
| company          | string                                           | Company name                                                     | CompanyA                                             |
| regs             | string (comma-separated)                         | Applicable regulations                                           | HIPAA, SOX                                           |
| svcs             | string (comma-separated)                         | Services running on system                                       | Cisco Systems, Inc. VPN Service, Adobe LM Service    |
| procs            | string                                           | Processes                                                        | svchost.exe, sppsvc.exe                              |
| criticality      | string                                           | Criticality of device                                            | Very High                                            |
| apps             | string (comma-separated)                         | Applications running on system                                   | Microsoft Word, Chrome                               |
| desc             | string                                           | Endpoint descriptor                                              | Some string                                          |
| dvc_type         | string                                           | Device type that generated the log                               | Microsoft Windows 7                                  |
| dvc_vendor       | string                                           | Vendor                                                           | Endgame                                              |
| dvc_version      | string                                           | Version                                                          | 2.1                                                  |
| architecture     | string                                           | CPU architecture                                                 | x86                                                  |
| uuid             | string                                           | Universally unique identifier                                    | a59ba71e-18b0-f762-2f02-0deaf95076c6                 |
| memtotal         | int                                              | Total memory (bytes)                                             | 844564433                                            |
| additional_attrs | string                                           | Additional attributes                                            | Key value pairs                                      |

**VPN Context Model**
---------------------

The data model for VPN context information is based on the VPN logs as follows:

| **Abbreviation** | **Data Type**           | **Description**                                                            | **Sample Values**                                    |
|------------------|-------------------------|----------------------------------------------------------------------------|------------------------------------------------------|
| dvc_time         | bigint                  | Timestamp from when the endpoint context information is obtained           | 1472653952                                           |
| ip4              | bigint                  | IP address of VPN box                                                      | Integer representaion of 10.1.1.1                    |
| ip6              | bigint                  | IP address of VPN box                                                      | Integer representaion of 10.1.1.1                    |
| vpn_vendor       | string                  | Vendor VPN                                                                 | Cisco                                                |
| vpn_version      | string                  | Version VPN                                                                | 3.0                                                  |
| vpn_sp           | string                  | VPN Service pack                                                           | 5                                                    |
| tz               | string                  | VPN timezone                                                               | EST                                                  |
| vpn_hotfixes     | string                  | VPN Applied hotfixes                                                       | 1134                                                 |
| vpn_nics         | string                  | Network interfaces                                                         | fe10::28f4:1a47:658b:d6e8, fe82::28f4:1a47:658b:d6e8 |
| vpn_host         | VPN Country Code        | string                                                                     | MX                                                   |
| vpn_country_name | VPN Country Name        | string                                                                     | Mexico                                               |
| vpn_ip           |                         | string                                                                     | Integer representation of 10.1.1.2                   |
| vpn_encrypt      | VPN encryption protocol | string                                                                     | IPSEC                                                |
| vpn_username     | string                  | VPN user account                                                           | jsmith                                               |
| vpn_user_ip      | string                  | VPN User IP address                                                        | Integer representation of 10.1.1.2                   |
| vpn_user_cc      | string                  | VPN Country Code                                                           | US                                                   |
| vpn_user_cn      | string                  | VPN Country Name                                                           | United States                                        |
| vpn_user_auth    | string                  | VPN user authorization / role                                              | Admin, normal user, etc                              |
| vpn_account_vip  | string                  | Criticality of the VPN account                                             | Medium, High                                         |
| vpn_uuid         | string                  | Universally unique identifier                                              | a59ba71e-18b0-f762-2f02-0deaf95076c6                 |
| uuids            | string                  | Universally unique identifier(s) comes from thee endpoint context if match | a59ba71e-18b0-f762-2f02-0deaf95xmexzA                |
| additional_attrs | string                  | Additional attributes                                                      | Key value pairs                                      |

**Network Context Model**
-------------------------

The data model for network context information is based on “whois” information
as follows:

| **Attribute**                          | **Data Type** | **Description**                        | **Sample Values** |
|----------------------------------------|---------------|----------------------------------------|-------------------|
| domain_name                            | string        | Domain name                            |                   |
| registry_domain_id                     | string        | Registry Domain ID                     |                   |
| registrar_whois_server                 | string        | Registrar WHOIS Server                 |                   |
| registrar_url                          | string        | Registrar URL                          |                   |
| update_date                            | bigint        | UTC timestamp                          |                   |
| creation_date                          | bigint        | Creation Date                          |                   |
| registrar_registration_expiration_date | bigint        | Registrar Registration Expiration Date |                   |
| registrar                              | string        | Registrar                              |                   |
| registrar_iana_id                      | string        | Registrar IANA ID                      |                   |
| registrar_abuse_contact_email          | string        | Registrar Abuse Contact Email          |                   |
| registrar_abuse_contact_phone          | string        | Registrar Abuse Contact Phone          |                   |
| domain_status                          | string        | Domain Status                          |                   |
| registry_registrant_id                 | string        | Registry Registrant ID                 |                   |
| registrant_name                        | string        | Registrant Name                        |                   |
| registrant_organization                | string        | Registrant Organization                |                   |
| registrant_street                      | string        | Registrant Street                      |                   |
| registrant_city                        | string        | Registrant City                        |                   |
| registrant_state_province              | string        | Registrant State/Province              |                   |
| registrant_postal_code                 | string        | Registrant Postal Code                 |                   |
| registrant_country                     | string        | Registrant Country                     |                   |
| registrant_phone                       | string        | Registrant Phone                       |                   |
| registrant_email                       | string        | Registrant Email                       |                   |
| registry_admin_id                      | string        | Registry Admin ID                      |                   |
| name_server                            | string        | Name Server                            |                   |
| dnssec                                 | string        | DNSSEC                                 |                   |

### **Extensibility of Data Model**

The aforementioned data model can be extended to accommodate custom attributes
by embedding key-value pairs within the log/alert/context entries. Each model
will support an additional attribute by the name of additional_attrs whose value
would be a JSON string. This JSON string will contain a Map (and only a Map) of
additional attributes that can’t be expressed in the specified model
description. Regardless of the type of these additional attributes, they will
always be interpreted as String. It’s up to the user, to translate them to
appropriate types, if necessary, in the analytics layer. It is also the user’s
responsibility to populate the aforementioned attribute as a Map, by presumably
parsing out these attributes from the original message. For example, if a user
wanted to extend the user context model to include a string attribute for “Desk
Location” and “City”, the following string would be set for additional_attrs:

| **Attribute Key** | **Attribute Value**                             |
|-------------------|-------------------------------------------------|
| additional_attrs  | {"dsk_location":"B3-F2-W3", "city":"Palo Alto"} |

Something similar can be done for endpoint context model, security event
log/alert model and other entities.

**Note:** This [UDF library](https://github.com/klout/brickhouse) can be used
for converting to/from JSON.

**Model Relationships**
-----------------------

The relationships between the data model entities are illustrated below.

![enter image description here](https://lh3.googleusercontent.com/-SxEubiTPzFE/WHVo0uxgJtI/AAAAAAAAAt8/3su9v3h0MsovJ0Mhy08EbuFTvRvKEoIwQCLcB/s0/ODMimage2.jpg)

**Data Ingestion Framework**
----------------------------

One of the challenges in populating the data model is the large number of
products and technologies that organizations are currently using to manage
security event logs/alerts, user and endpoint information. There are literally
dozens of vendors in each category that offer technologies that could be used to
populate the model. The labor required to transform the data and map the
attributes to the data model is extensive when you consider how many
technologies are in the mix at each organization (and across organizations). One
way to address this challenge is with a Data Ingestion Framework that provides a
configuration-based mechanism to perform the transformations and mappings. A
configuration-based capability will allow the ingest pipelines to become
portable and reusable across the community. For example, if I create an ingest
pipeline for Centrify to populate the user context model, it can be shared with
other users of Centrify who can immediately realize the benefit. Such a
framework could allow the community to quickly build the necessary pipelines for
the dozens (and hundreds) of technologies being used in the market. Without a
standard ingest framework, each pipeline is built independently, requiring more
labor, providing no standardization and little portability. It’s also important
that the data ingestion framework support the ability to both capture the “raw”
event and create a meta event that represents the normalized event and maps the
attributes to the defined data model. This will ensure both stream and batch
processing use cases are supported.

Streamsets is an ingest framework that provides the needed functionality
outlined above. Sample Streamsets ingest pipelines for populating the ODM with
common data sources will be published to the Spot Github repo.

**Data Formats**
----------------

**Avro**
--------

Avro is the recommended data format due to its schema representation,
compatibility checks, and interoperability with Hadoop. Avro supports a pure
JSON representation for readability and ease of use but also a binary
representation of the data for efficient storage. Avro is the optimal format for
streaming-based analytic use cases. A sample event and corresponding schema
representation are detailed below.

**Event**

{

"eventtime":1469562994,

"src_ip4":”192.168.1.1”,

“src_host”:”test1.clouera.com”,

“src_port”:1029, “dst_ip4”:”192.168.21.22”,

“dst_host”:”test3.companyA.com”,

“dst_port”:443,

“dvc_type”:”sshd”,

“category”:”auth”,

“aproto”:”sshd”,

“msg”:”user:mhicks successfully logged in to test3.companyA.com from
192.168.1.1”,

“username”:”mhicks”,

“Severity”:3,

}

 

**Schema**

{

"type": "record",

"doc":"This event records SSHD activity",

"name": "auth",

"fields":{

{"name":"eventtime", "type":"long", "doc":"Stop time of event""},

{"name":"src_ip4", "type":"long", "doc":”Source IP Address"},

{"name":"src_host", "type":"string",”doc”:”Source hostname},

{"name":"src_port", "type":"int",”doc”:”Source port”},

{"name":"dst_ip4", "type":"long", "doc"::”Destination IP Address"},

{"name":"dst_host", "type":"string", "doc":”Destination IP Address"},

{"name":"dst_port", "type":"int", ”doc”:”Destination port”},

{"name":"dvc_type", "type":"string", “doc”:”Source device type”},

{"name":"category", "type":"string",”doc”:”category/type of event message”},

{"name":"aproto", "type":"string",”doc”:”Application or network protocol”},

{"name":"msg", "type":"string",”doc”:”event message”},

{"name":"username", "type":"string",”doc”:”username”},

{"name":"severity", "type":"int",”doc”:”severity of event on scale of 1-10”},

}

 

**JSON**
--------

JSON is commonly used as a data-interchange format due to it’s ease of use and
familiarity within the development community. The corresponding JSON object for
the sample event described previously is noted below.

{

“eventtime”:1469562994,

“src_ip4”:”192.168.1.1”,

“src_host”:”test1.clouera.com”,

“src_port”:1029,

“dst_ip4”:”192.168.21.22”,

“dst_host”:”test3.companyA.com”,

“dst_port”:443,

“aproto”:”sshd”,

“msg”:”user:mhicks successfully logged in to test3.companyA.com from
192.168.1.1”,

“username”:”mhicks”,

}

**Parquet**
-----------

Parquet is a columnar storage format that offers the benefits of compression and
efficient columnar data representation and is optimal for batch analytic use
cases. More information on parquet can be found here:
https://parquet.apache.org/documentation/latest/ It should be noted that
conversion from Avro to Parquet is supported. This allows for data collected and
analyzed for stream-based use cases to be easily converted to Parquet for
longer-term batch analytics.

**Example - Advanced Threat Modeling**
--------------------------------------

In this example, the ODM is leveraged to build an “event” table for a threat
model that uses attributes native to the ODM and derived attributes, which are
calculations based on the aggregate data stored in the model. In this context,
an “event” table is defined by the attributes to be evaluated for predictive
power in identifying threats and the actual attribute values (i.e rows in the
table). In the example below, the event table is composed of the following
attributes, which are then leveraged to identify threats via a Risk Score
analytic model:

-   “src_ipv4” - This attribute is native to the security event log component of
    the ODM and represents the source IP address of the corresponding table row

-   “os” - This attribute is native to the endpoint context component of the ODM
    and represents the operating system of the endpoint system in the table row

-   SUM (in_bytes + out_bytes) for the last 7 days - “in_bytes” and “out_bytes”
    are native to the security event log component of the ODM. This derived
    attribute represents a summation of bytes between the source address and
    destination domain over the last 7 days

-   “dst_domain” - This attribute is native to the security event log component
    of the ODM and represents the destination domain

-   Days since “creation_date” - “creation_date” is native to the network
    context component of the ODM and represents the date the referenced domain
    was registered. This derived attribute calculates the days since the domain
    was created/registered.

| **src_ipv4** | **OS​**    | **dst domain** | **Days since “creation_date”** | **SUM (in_bytes + out_bytes)** | **Risk Score (1-100)** |
|--------------|-----------|----------------|--------------------------------|--------------------------------|------------------------|
| 10.1.1.10    | Microsoft | dajdkwk.com    | 39                             | 3021 MB                        | 99                     |
| 192.168.8.9  | Redhat    | usatoday.com   | 3027                           | 2 MB                           | 2                      |
| 172.16.32.3  | Apple     | box.com        | 1532                           | 76 MB                          | 10                     |
| 192.168.4.4  | Microsoft | kzjkeljr.ru    | 3                              | 0.9 MB                         | 92                     |

 

The **“Risk Score”** attribute represents potential output from a threat
detection model based on the attributes and values represented in the “event”
table and is provided as an example of what is enabled by the ODM. Can you tell
which attributes and values hold predictive power for threat detection?

 

**Example - Singular Data View for Complete Context**
-----------------------------------------------------

The table below demonstrates a logical, “denormalized” view of what is offered
by the ODM. In this example, the raw DNS event is mapped to the ODM, which is
enriching the DNS event with Endpoint and Network context needed to make a
proper threat determination. For large data sets, this type of view is not
performant or reasonable to provide with databases upon which legacy security
analytic technologies are built. However, this singular/denormalized data
representation is feasible with Apache Spot (incubating).

 

**RAW DNS EVENT**

1463702961,169,10.0.0.101,172.16.36.157,www.kzjkeljr.ru,1,0x00000001,0,49.52.46.49

 

**DNS EVENT + Open Data Model**

| **ODM Attribute** | **Value**       | **Description**                      | **ODM Context Attributes**                                                                                                                                                                                |
|-------------------|-----------------|--------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| eventtime         | 1463702961      | UTC timestamp of DNS query           |                                                                                                                                                                                                           |
| length            | 169             | DNS Frame length                     |                                                                                                                                                                                                           |
| dst_ip4           | 10.1.0.11       | Destination address (DNS server)     | Endpoint Context OS=”Redhat 6.3” host=”dns.companyA.com” mac=”94:94:26:3:86:16” departm=”IT” regs=”PCI” vulns=”CVE-123, CVE-456,...” ….                                                                   |
| src_ip4           | 172.16.32.17    | Source address (DNS query initiator) | Endpoint Context OS=”Microsoft Windows 7” host=”jsmith.companyA.com” mac=”94:94:26:3:86:17” departm=”FCE” regs=”Corporate” apps=”Office 365, Visio 12.2, Chrome 52.0.3….” vulns=”CVE-123, CVE-456,...” …. |
| qry               | www.kzjkeljr.ru | DNS query                            | Network Context domain_name=”kzjkeljr.ru” Creation_date”2016-08-30” registrar_registration_expiration_date=”2016-09-30” registration_country=”Russia” ….                                                  |
| class             | 1               | DNS query class                      |                                                                                                                                                                                                           |
| code              | 0x00000001      | DNS response code                    |                                                                                                                                                                                                           |
| response_qry      | 49.52.46.49     | A record, DNS query response         |                                                                                                                                                                                                           |

 