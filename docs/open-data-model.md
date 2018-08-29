Apache Spot (Incubating) Open Data Model (ODM)

(v1.1)

# Overview

This document describes the Apache Spot open data model (ODM) and corresponding strategy.  It also describes the use cases for which Spot running on the Cloudera platform is uniquely capable of addressing along with the open data model.

# Apache Spot Open Data Model Strategy

The Spot open data model strategy extends Spot capabilities to support a broader set of cybersecurity use cases than initially supported.  The primary use case initially supported by Spot includes Network Traffic Analysis for network flows (Netflow, sflow, etc.), DNS and Proxy; primarily the identification of threats through anomalous event detection using both supervised and unsupervised machine learning.

 

In order to support a broader set of use cases, Spot has been extended to collect and analyze other common "event-oriented" data sources analyzed for cyber threats, including but not limited to the following log types:

 

●      Proxy

●      Web server

●      Operating system

●      Firewall

●      Intrusion Prevention/Detection

●      Data Loss Prevention

●      Active Directory / Identity Management

●      User/Entity Behavior Analysis

●      Endpoint/Asset Management

●      Network Meta/Session and PCAP files

●      Etc.

 

One of the biggest challenges organizations face today in combating cyber threats is collecting and normalizing data from the myriad of security event data sources (hundreds) in order to build the needed analytics.  This often results in the analytics being dependent upon the specific technologies used by an organization to detect threats and prevents the needed flexibility and agility to keep up with these ever-increasing (and complex) threats.  Technology lock-in is sometimes a byproduct of today’s status quo, as it’s extremely costly to add new technologies (or replace existing ones) because of the downstream analytic dependencies.

 

To achieve the goal of extending Spot to support additional use cases, an open data model for the most relevant security event and contextual data sources has been implemented; Security event logs/alerts, Network, User and Endpoint.  The open data model, which can be applied "on-read" or “on-write”, in batch or stream, allows for the separation of security analytics from the specific data sources on which they are built. This “separation of duties” enables organizations to build analytics that are not dependant upon specific technologies and provides the flexibility to change underlying data sources, without impacting the analytics.  This also affords security vendors the opportunity to build additional products on top of the ODM to drive new revenue streams.  

# Apache Spot Enabled Use Cases

Spot (which runs on the Cloudera platform) is uniquely positioned to help address the following cybersecurity use cases, which are not effectively addressed by legacy cybersecurity platforms:

* Detection of known & unknown threats leveraging machine learning and advanced analytic modeling

    *  Current technologies are limited in the analytics they can apply to detect threats. These limitations stem from the inability to collect all the data sources needed to effectively identify threats (structured, unstructured, etc.) AND inability to process the massive volumes of data needed to do so.  Legacy platforms are typically limited to rules-based and signature detection.  They are somewhat effective at detecting known threats but struggle with new threats.

    * Spot addresses these gaps through its ability to collect any data type of any volume. Coupled with the various analytic frameworks that are provided (including machine learning), Spot enables a whole new class of analytics that can scale to today’s demands.  The topic model used by Spot to detect anomalous network traffic is one example of where the Spot platform excels.

* Reduction of mean time to incident detection & resolution (MTTR)

    * One of the challenges organizations face today is detecting threats early enough to minimize adverse impacts.  This stems from the limitations previously discussed with regards to limited analytics.  It can also be attributed to the fact that investigative queries often take hours or days to return results.  Legacy platforms can’t offer a central data store for facilitating such investigations due to their inability to store and serve the massive amounts of data involved.  This cripples incident investigations and results in MTTRs of many weeks and months, meanwhile the adverse impacts of the breach are magnified, thus making the threat harder to eradicate.

    * Spot addresses these gaps by providing the capability for a central data store that houses ALL the data needed to facilitate an investigation, returning investigative query results in seconds and minutes (vs. hours and days).  Spot can effectively reduce incident MTTR and reduce adverse impacts of a breach.

* Threat Hunting

    * It’s become necessary for organizations to "hunt" for active threats as traditional passive threat detection approaches are not sufficient. “Hunting” involves performing ad-hoc searches and queries over vast amounts of data representing many weeks and months worth of events, as well as applying ad-hoc algorithms to detect the needle in the haystack. Traditional platforms do not perform well for these types of activities as the query results sometimes take hours and days to be retrieved.  These traditional platforms also lack the analytic flexibility to construct the necessary algorithms and logic needed.

    * Spot addresses these gaps in the same ways it addresses others; by providing a central data store with the needed analytic frameworks that scale to the needed workloads.

# Data Model

In order to provide a framework for effectively analyzing data for cyber threats, it is necessary to collect and analyze standard security event logs/alerts and contextual data regarding the entities referenced in these logs/alerts.  The most common entities include network, user and endpoint, but there are others such as file and certificate.  

In the diagram below, the raw event tells us that user "jsmith" successfully logged in to an Oracle database from the IP address 10:1.1.3.  Based on the raw event only, we don’t know if this event is a legitimate threat or not.  After injecting user and endpoint context, the enriched event tells us this event is a potential threat that requires further investigation.

![image alt text](https://lh3.googleusercontent.com/-Q8TasmY-vRQ/WHVnoXAK44I/AAAAAAAAAtw/XBDy3PC98k800iaWpNIzAYoQ8S9zc5NBQCLcB/s0/ODMimage1.jpg)

Based on the need to collect and analyze both security event logs/alerts and contextual data, support for the following types of security information are included in the Spot open data model:

* Security event logs/alerts

    * This data type includes event logs from common data sources used to detect threats and includes network flows, operating system logs, IPS/IDS logs, firewall logs, proxy logs, web logs, DLP logs, etc. 

* Network context data

    * This data type includes information about the network, which can be gleaned from Whois servers, asset databases and other similar data sources.

* User context data

    * This data type includes information from user and identity management systems including Active Directory, Centrify, and other similar systems.

* Endpoint context data

    * This data includes information about endpoint systems (servers, workstations, routers, switches, etc.) and can be sourced from asset management systems, vulnerability scanners, and endpoint management/detection/response systems such as Webroot, Tanium, Sophos, Endgame, CarbonBlack and others.

* Threat intelligence context data

    * This data includes contextual information about URLs, domains, websites, files and others.

* Vulnerability context data

    * This data includes contextual information about vulnerabilities and is typically sources from vulnerability management systems (i.e. Qualys, Tenable, etc.).

* **Roadmap Items**

    * File context data

    * Certificate context data

# Naming Convention

A naming convention is needed for the open data model to represent attributes across vendor products and technologies.  The naming convention is composed of prefixes (net, http, src, dst, etc.) and common attribute names (ip4, user_name, etc.).  It is common to use multiple prefixes in combination with an attribute.  The following examples are provided to illustrate the naming convention.

* src_ip4 

    * "src" - this prefix indicates the attribute pertains to details about the “source” entity referenced in the event (src_ip4, src_user_name, src_host, etc.)

    * "ip4" - this attribute name corresponds to an IP address (version 4)

    * Summary: This attribute represents the source ip address (version 4) within the referenced event

* prx_browser

    * "prx" - this prefix indicates the attribute pertains to a “Proxy” event

    * "browser" -this attribute name corresponds to the “browser” referenced within the event

    * Summary: This attribute represents the browser (i.e. "Mozilla", “Internet Explorer”, etc.) referenced in the Proxy event

* dvc_host

    * "dvc" - This prefix indicates the attribute pertains to the “Device” that is the source of the event

    * "host" - This attribute name corresponds to the “hostname” 

    * Summary: This attribute represents the hostname of the device where the event was generated

## Prefixes

<table>
  <tr>
    <td>Prefix</td>
    <td>Description</td>
  </tr>
  <tr>
    <td>src</td>
    <td>Corresponds to the "source" fields within a given event (i.e. source address)</td>
  </tr>
  <tr>
    <td>dst</td>
    <td>Corresponds to the “destination” fields within a given event (i.e. destination address)</td>
  </tr>
  <tr>
    <td>dvc</td>
    <td>Corresponds to the “device” applicable fields within a given event (i.e. device address) and represent where the event originated</td>
  </tr>
  <tr>
    <td>fwd</td>
    <td>Forwarded from device</td>
  </tr>
  <tr>
    <td>request</td>
    <td>Corresponds to requested values (vs. those returned, i.e. “requested URI”)</td>
  </tr>
  <tr>
    <td>response</td>
    <td>Corresponds to response value (vs. those requested)</td>
  </tr>
  <tr>
    <td>file</td>
    <td>Corresponds to the “file” fields within a given event (i.e. file type)</td>
  </tr>
  <tr>
    <td>user</td>
    <td>Corresponds to user attributes (i.e. name, id, etc.)</td>
  </tr>
  <tr>
    <td>xlate</td>
    <td>Corresponds to translated values within a given event (i.e. src_xlate_ip for “translated source ip address”</td>
  </tr>
  <tr>
    <td>in</td>
    <td>Ingress</td>
  </tr>
  <tr>
    <td>out</td>
    <td>Egress</td>
  </tr>
  <tr>
    <td>new</td>
    <td>New value</td>
  </tr>
  <tr>
    <td>orig</td>
    <td>Original value</td>
  </tr>
  <tr>
    <td>app</td>
    <td>Corresponds to values associated with application events</td>
  </tr>
  <tr>
    <td>net</td>
    <td>Corresponds to values associated with network attributes (direction, flags)</td>
  </tr>
  <tr>
    <td>end</td>
    <td>Corresponds to values associated with endpoint attributes</td>
  </tr>
  <tr>
    <td>dns</td>
    <td>Corresponds to attributes within the DNS protocol</td>
  </tr>
  <tr>
    <td>prx</td>
    <td>Corresponds to attributes within Proxy events</td>
  </tr>
  <tr>
    <td>av</td>
    <td>Corresponds to attributes within Antivirus events</td>
  </tr>
  <tr>
    <td>http</td>
    <td>Corresponds to attributes within the HTTP protocol</td>
  </tr>
  <tr>
    <td>smtp</td>
    <td>Corresponds to attributes within the SMTP protocol</td>
  </tr>
  <tr>
    <td>ftp</td>
    <td>Corresponds to attributes within the FTP protocol</td>
  </tr>
  <tr>
    <td>snmp</td>
    <td>Corresponds to attributes within the SNMP protocol</td>
  </tr>
  <tr>
    <td>tls</td>
    <td>Corresponds to attributes within the TLS protocol</td>
  </tr>
  <tr>
    <td>ssh</td>
    <td>Corresponds to attributes within the SSH protocol</td>
  </tr>
  <tr>
    <td>dhcp</td>
    <td>Corresponds to attributes within the DHCP protocol</td>
  </tr>
  <tr>
    <td>irc</td>
    <td>Corresponds to attributes within the IRC protocol</td>
  </tr>
  <tr>
    <td>flow</td>
    <td>Corresponds to attributes within FLOW events</td>
  </tr>
  <tr>
    <td>ti</td>
    <td>Corresponds to attributes within Threat Intelligence context data</td>
  </tr>
  <tr>
    <td>vuln</td>
    <td>Corresponds to attributes within vulnerability management data</td>
  </tr>
</table>


# Security Event Log/Alert Data Model

The data model for security event logs/alerts is detailed in the below. The attributes are categorized as follows:

* Common - attributes that are common across many device types

* Device - attributes that are applicable to the device that generated the event

* Network - attributes that are applicable to the network components of the event

* File - attributes that are applicable to file objects referenced in the event

* Endpoint - attributes that are applicable to the endpoints referenced in the event

* User- attributes that are applicable to the user referenced in the event

* Proxy - attributes that are applicable to proxy events

* Antivirus - attributes that are applicable to antivirus events

* Vulnerability - attributes that are applicable to vulnerability management events

* Protocol 

    * DNS - attributes that are specific to the DNS protocol

    * HTTP - attributes that are specific to the HTTP protocol

    * ….SMTP, SSH, TLS, DHCP, IRC, SNMP and FTP

Note: The model will evolve to include reserved attributes for additional device types that are not currently represented.  The model can currently be extended to support ANY attribute for ANY device type by following the guidance outlined in the section titled "[Extensibility of Data Model](#heading=h.h3vtn2etca3j)".

<table>
  <tr>
    <td>Category</td>
    <td>Attribute</td>
    <td>Data type</td>
    <td>Description</td>
    <td>Sample Values</td>
  </tr>
  <tr>
    <td>Common</td>
    <td>event_time</td>
    <td>long</td>
    <td>timestamp of event (UTC)</td>
    <td>1472653952
</td>
  </tr>
  <tr>
    <td></td>
    <td>begintime</td>
    <td>long</td>
    <td>timestamp</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td></td>
    <td>endtime</td>
    <td>long</td>
    <td>timestamp</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td></td>
    <td>event_insertime</td>
    <td>long</td>
    <td>timestamp</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td></td>
    <td>lastupdatetime</td>
    <td>long</td>
    <td>timestamp</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td></td>
    <td>duration</td>
    <td>float</td>
    <td>Time duration (milliseconds)</td>
    <td>2345</td>
  </tr>
  <tr>
    <td></td>
    <td>event_id</td>
    <td>string</td>
    <td>Unique identifier for event</td>
    <td>x:2388</td>
  </tr>
  <tr>
    <td></td>
    <td>name</td>
    <td>string</td>
    <td>Name of event</td>
    <td>"Successful login …"</td>
  </tr>
  <tr>
    <td></td>
    <td>org</td>
    <td>string</td>
    <td>Organization</td>
    <td>“HR” or “Finance” or “CustomerA”</td>
  </tr>
  <tr>
    <td></td>
    <td>type</td>
    <td>string</td>
    <td>Type information </td>
    <td>“Informational”, “image/gif”</td>
  </tr>
  <tr>
    <td></td>
    <td>n_proto</td>
    <td>string</td>
    <td>Network protocol of event </td>
    <td>TCP, UDP, ICMP</td>
  </tr>
  <tr>
    <td></td>
    <td>a_proto</td>
    <td>string</td>
    <td>Application protocol of event </td>
    <td>HTTP, NFS, FTP</td>
  </tr>
  <tr>
    <td></td>
    <td>msg</td>
    <td>string</td>
    <td>Message (details of action taken on object)</td>
    <td>Some long string</td>
  </tr>
  <tr>
    <td></td>
    <td>mac</td>
    <td>string</td>
    <td>MAC address</td>
    <td>94:94:26:3:86:16</td>
  </tr>
  <tr>
    <td></td>
    <td>severity</td>
    <td>string</td>
    <td>Severity of event</td>
    <td>High, 10, 1</td>
  </tr>
  <tr>
    <td></td>
    <td>raw</td>
    <td>string</td>
    <td>Raw text message of entire event</td>
    <td>Complete copy of log entry</td>
  </tr>
  <tr>
    <td></td>
    <td>risk</td>
    <td>Floating point</td>
    <td>Risk score</td>
    <td>95.67</td>
  </tr>
  <tr>
    <td></td>
    <td>code</td>
    <td>string</td>
    <td>Response or error code</td>
    <td>404</td>
  </tr>
  <tr>
    <td></td>
    <td>category</td>
    <td>string</td>
    <td>Event category</td>
    <td>/Application/Start</td>
  </tr>
  <tr>
    <td></td>
    <td>query</td>
    <td>string</td>
    <td>Query (DNS query, URI query,  SQL query, etc.)</td>
    <td>Select * from table</td>
  </tr>
  <tr>
    <td></td>
    <td>service</td>
    <td>string</td>
    <td>(i.e. service name, type of service)</td>
    <td>sshd</td>
  </tr>
  <tr>
    <td></td>
    <td>state</td>
    <td>string</td>
    <td>State of object</td>
    <td>Running, Paused, stopped</td>
  </tr>
  <tr>
    <td></td>
    <td>in_bytes</td>
    <td>int</td>
    <td>Bytes in</td>
    <td>1025</td>
  </tr>
  <tr>
    <td></td>
    <td>out_bytes</td>
    <td>int</td>
    <td>Bytes out</td>
    <td>9344</td>
  </tr>
  <tr>
    <td></td>
    <td>xref</td>
    <td>string</td>
    <td>External reference to public description</td>
    <td>http://www.oracle.com/technetwork/java/javase/2col/6u85-bugfixes-2298235.html
</td>
  </tr>
  <tr>
    <td></td>
    <td>version</td>
    <td>string</td>
    <td>Version </td>
    <td>5.4</td>
  </tr>
  <tr>
    <td></td>
    <td>api</td>
    <td>string</td>
    <td>API label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>parameter</td>
    <td>string</td>
    <td>Parameter label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>action</td>
    <td>string</td>
    <td>Action label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>proc</td>
    <td>string</td>
    <td>Process label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>app</td>
    <td>string</td>
    <td>Application label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>disposition</td>
    <td>string</td>
    <td>Disposition label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>prevalence</td>
    <td>string</td>
    <td>Prevalence label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>confidence</td>
    <td>string</td>
    <td>Confidence label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>sensitivity</td>
    <td>string</td>
    <td>Sensitivity label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>count</td>
    <td>int</td>
    <td>Generic count</td>
    <td>20</td>
  </tr>
  <tr>
    <td></td>
    <td>company</td>
    <td>string</td>
    <td>Company label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>additional_attrs</td>
    <td>String (JSON Map)</td>
    <td>Custom event attributes</td>
    <td>"building":"729","cube":"401"</td>
  </tr>
  <tr>
    <td></td>
    <td>totrust</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>fromtrust</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>rule</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>threat</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>pcap_id</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td>Device</td>
    <td>dvc_time</td>
    <td>long</td>
    <td>UTC timestamp from device where event/alert originates or is received</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_ip4/dvc_ip6</td>
    <td>int/int128</td>
    <td>IP address of device</td>
    <td>Integer representation of 10.1.1.1</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_group</td>
    <td>string</td>
    <td>Device group label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_server</td>
    <td>string</td>
    <td>Server label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_host</td>
    <td>string</td>
    <td>Hostname of device</td>
    <td>Integer representation of 10.1.1.1</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_domain</td>
    <td>string</td>
    <td>Domain of dvc</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_type</td>
    <td>string</td>
    <td>Device type that generated the log</td>
    <td>Unix, Windows, Sonicwall</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_vendor</td>
    <td>string</td>
    <td>Vendor</td>
    <td>Microsoft, Fireeye</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_fwd_ip4/fwd_ip6</td>
    <td>int/int128</td>
    <td>Forwarded from device</td>
    <td>Integer representation of 10.1.1.1</td>
  </tr>
  <tr>
    <td></td>
    <td>dvc_version</td>
    <td>string</td>
    <td>Version</td>
    <td>“3.2.2”</td>
  </tr>
  <tr>
    <td>Network</td>
    <td>src_ip4/src_ip6</td>
    <td>int/int128</td>
    <td>Source ip address of event</td>
    <td>Integer representation of 10.1.1.1</td>
  </tr>
  <tr>
    <td></td>
    <td>src_host</td>
    <td>string</td>
    <td>Source FQDN of event</td>
    <td>test.companyA.com</td>
  </tr>
  <tr>
    <td></td>
    <td>src_domain</td>
    <td>string</td>
    <td>Domain name of source address</td>
    <td>companyA.com</td>
  </tr>
  <tr>
    <td></td>
    <td>src_port</td>
    <td>int</td>
    <td>Source port of event</td>
    <td>1025</td>
  </tr>
  <tr>
    <td></td>
    <td>src_country_code</td>
    <td>string</td>
    <td>Source country code</td>
    <td>cn</td>
  </tr>
  <tr>
    <td></td>
    <td>src_country_name</td>
    <td>string</td>
    <td>Source country name</td>
    <td>China</td>
  </tr>
  <tr>
    <td></td>
    <td>src_region</td>
    <td>string</td>
    <td>Source region</td>
    <td>string</td>
  </tr>
  <tr>
    <td></td>
    <td>src_city</td>
    <td>string</td>
    <td>Source city</td>
    <td>Shenghai</td>
  </tr>
  <tr>
    <td></td>
    <td>src_lat</td>
    <td>int</td>
    <td>Source latitude</td>
    <td>90</td>
  </tr>
  <tr>
    <td></td>
    <td>src_long</td>
    <td>int</td>
    <td>Source longitude</td>
    <td>90</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_ip4/dst_ip6</td>
    <td>int/int128</td>
    <td>Destination ip address of event</td>
    <td>Integer representation of 10.1.1.1</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_host</td>
    <td>string</td>
    <td>Destination FQDN of event</td>
    <td>test.companyA.com</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_domain</td>
    <td>string</td>
    <td>Domain name of destination address</td>
    <td>companyA.com</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_port</td>
    <td>int</td>
    <td>Destination port of event</td>
    <td>80</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_country_code</td>
    <td>string</td>
    <td>Source country code</td>
    <td>cn</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_country_name</td>
    <td>string</td>
    <td>Source country name</td>
    <td>China</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_region</td>
    <td>string</td>
    <td>Source region</td>
    <td>string</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_city</td>
    <td>string</td>
    <td>Source city</td>
    <td>Shenghai</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_lat</td>
    <td>int</td>
    <td>Source latitude</td>
    <td>90</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_long</td>
    <td>int</td>
    <td>Source longitude</td>
    <td>90</td>
  </tr>
  <tr>
    <td></td>
    <td>src_asn</td>
    <td>int</td>
    <td>Autonomous system number</td>
    <td>33</td>
  </tr>
  <tr>
    <td></td>
    <td>dst_asn</td>
    <td>int</td>
    <td>Autonomous system number</td>
    <td>33</td>
  </tr>
  <tr>
    <td></td>
    <td>net_direction</td>
    <td>string</td>
    <td>Direction</td>
    <td>In, inbound, outbound, ingress, egress</td>
  </tr>
  <tr>
    <td></td>
    <td>net_flags</td>
    <td>string</td>
    <td>TCP flags</td>
    <td>.AP.SF</td>
  </tr>
  <tr>
    <td>File</td>
    <td>file_name</td>
    <td>string</td>
    <td>Filename from event</td>
    <td>output.csv</td>
  </tr>
  <tr>
    <td></td>
    <td>file_path</td>
    <td>string</td>
    <td>File path</td>
    <td>/root/output.csv</td>
  </tr>
  <tr>
    <td></td>
    <td>file_atime</td>
    <td>long</td>
    <td>Timestamp (UTC) of file access</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td></td>
    <td>file_acls</td>
    <td>string</td>
    <td>File permissions</td>
    <td>rwx-rwx-rwx</td>
  </tr>
  <tr>
    <td></td>
    <td>file_type</td>
    <td>string</td>
    <td>Type of file</td>
    <td>“.doc”</td>
  </tr>
  <tr>
    <td></td>
    <td>file_size</td>
    <td>int</td>
    <td>Size of file in bytes</td>
    <td>1244</td>
  </tr>
  <tr>
    <td></td>
    <td>file_desc</td>
    <td>string</td>
    <td>Description of file</td>
    <td>Project Plan for Project xyz</td>
  </tr>
  <tr>
    <td></td>
    <td>file_hash</td>
    <td>string</td>
    <td>Hash of file</td>
    <td></td>
  </tr>
  <tr>
    <td></td>
    <td>file_hash_type</td>
    <td>string</td>
    <td>Type of hash</td>
    <td>MD5, SHA1,SHA256</td>
  </tr>
  <tr>
    <td>Endpoint</td>
    <td>end_object</td>
    <td>string</td>
    <td>File/Process/Registry</td>
    <td>File, Registry, Process</td>
  </tr>
  <tr>
    <td></td>
    <td>end_action</td>
    <td>string</td>
    <td>Action taken on object (open/delete/edit)</td>
    <td>Open, Edit</td>
  </tr>
  <tr>
    <td></td>
    <td>end_msg</td>
    <td>string</td>
    <td>Message (details of action taken on object)</td>
    <td>Some long string</td>
  </tr>
  <tr>
    <td></td>
    <td>end_app</td>
    <td>string</td>
    <td>Application</td>
    <td>Microsoft Powerpoint</td>
  </tr>
  <tr>
    <td></td>
    <td>end_location</td>
    <td>string</td>
    <td>Location</td>
    <td>Atlanta, GA</td>
  </tr>
  <tr>
    <td></td>
    <td>end_proc</td>
    <td>string</td>
    <td>Process</td>
    <td>SSHD</td>
  </tr>
  <tr>
    <td>User</td>
    <td>user_name
(Src_user_name, dst_user_name)</td>
    <td>string</td>
    <td>username from event
</td>
    <td>mhicks</td>
  </tr>
  <tr>
    <td></td>
    <td>user_email</td>
    <td>string</td>
    <td>Email address</td>
    <td>test@companyA.com</td>
  </tr>
  <tr>
    <td></td>
    <td>user_id</td>
    <td>string</td>
    <td>userid</td>
    <td>234456</td>
  </tr>
  <tr>
    <td></td>
    <td>user_loc</td>
    <td>string</td>
    <td>location</td>
    <td>Herndon, VA</td>
  </tr>
  <tr>
    <td></td>
    <td>user_desc</td>
    <td>string</td>
    <td>Description of user</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td>DNS</td>
    <td>dns_class</td>
    <td>string</td>
    <td>DNS class</td>
    <td>1</td>
  </tr>
  <tr>
    <td></td>
    <td>dns_len</td>
    <td>int</td>
    <td>DNS frame length</td>
    <td>188</td>
  </tr>
  <tr>
    <td></td>
    <td>dns_query</td>
    <td>string</td>
    <td>Requested DNS query</td>
    <td>test.test.com</td>
  </tr>
  <tr>
    <td></td>
    <td>dns_response_code</td>
    <td>string</td>
    <td>Response code</td>
    <td>0x00000001</td>
  </tr>
  <tr>
    <td></td>
    <td>dns_answers</td>
    <td>string</td>
    <td>Response to DNS Query</td>
    <td>178.2.1.99</td>
  </tr>
  <tr>
    <td></td>
    <td>dns_type</td>
    <td>int</td>
    <td>DNS query type </td>
    <td>1</td>
  </tr>
  <tr>
    <td>Proxy</td>
    <td>prx_category</td>
    <td>string</td>
    <td>Event category</td>
    <td>SG-HTTP-SERVICE</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_browser</td>
    <td>string</td>
    <td>Web browser</td>
    <td>Internet Explorer</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_code</td>
    <td>string</td>
    <td>Error or response code</td>
    <td>404</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_referrer</td>
    <td>string</td>
    <td>Referrer</td>
    <td>www.usatoday.com</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_host</td>
    <td>string</td>
    <td>Requested URI</td>
    <td>/wcm/assets/images/imagefileicon.gif</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_filter_rule</td>
    <td>string</td>
    <td>Applied filter or rule</td>
    <td>Internet, Rule 6 </td>
  </tr>
  <tr>
    <td></td>
    <td>prx_filter_result</td>
    <td>string</td>
    <td>Result of applied filter or rule</td>
    <td>Proxied, Blocked</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_query</td>
    <td>string</td>
    <td>URI query</td>
    <td>?func=S_senseHTML&Page=a26815a313504697a126279</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_action</td>
    <td>string</td>
    <td>Action taken on object </td>
    <td>TCP_HIT, TCP_MISS, TCP_TUNNELED</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_method</td>
    <td>string</td>
    <td>HTTP method</td>
    <td>GET, CONNECT, POST</td>
  </tr>
  <tr>
    <td></td>
    <td>prx_type</td>
    <td>string</td>
    <td>Type of request</td>
    <td>image/gif</td>
  </tr>
  <tr>
    <td>HTTP</td>
    <td>http_request_method</td>
    <td>string</td>
    <td>HTTP method</td>
    <td>GET, CONNECT, POST</td>
  </tr>
  <tr>
    <td></td>
    <td>http_request_uri </td>
    <td>string</td>
    <td>Requested URI</td>
    <td>/wcm/assets/images/imagefileicon.gif</td>
  </tr>
  <tr>
    <td></td>
    <td>http_request_body_len</td>
    <td>int</td>
    <td>Length of request body</td>
    <td>98</td>
  </tr>
  <tr>
    <td></td>
    <td>http_request_user_name </td>
    <td>string</td>
    <td>username from event</td>
    <td>mhicks</td>
  </tr>
  <tr>
    <td></td>
    <td>http_request_password</td>
    <td>string</td>
    <td>Password from event</td>
    <td>abc123</td>
  </tr>
  <tr>
    <td></td>
    <td>http_request_proxied</td>
    <td>string</td>
    <td>Proxy request label</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>http_request_headers</td>
    <td>MAP</td>
    <td>HTTP request headers</td>
    <td>request_headers[‘HOST’]
request_headers[‘USER-AGENT’]
request_headers[‘ACCEPT’]</td>
  </tr>
  <tr>
    <td></td>
    <td>http_response_status_code</td>
    <td>int</td>
    <td>HTTP response status code</td>
    <td>404</td>
  </tr>
  <tr>
    <td></td>
    <td>http_response_status_msg</td>
    <td>string</td>
    <td>HTTP response status message</td>
    <td>“Not found”</td>
  </tr>
  <tr>
    <td></td>
    <td>http_response_body_len</td>
    <td>int</td>
    <td>Length of response body</td>
    <td>98</td>
  </tr>
  <tr>
    <td></td>
    <td>http_response_info_code </td>
    <td>int</td>
    <td>HTTP response info code</td>
    <td>100</td>
  </tr>
  <tr>
    <td></td>
    <td>http_response_info_msg</td>
    <td>string</td>
    <td>HTTP response info message</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>http_response_resp_fuids</td>
    <td>string</td>
    <td>Response FUIDS</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>http_response_mime_types</td>
    <td>string</td>
    <td>Mime types</td>
    <td>“cgi,bat,exe”</td>
  </tr>
  <tr>
    <td></td>
    <td>http_response_headers</td>
    <td>MAP</td>
    <td>Response headers</td>
    <td>response_headers[‘SERVER’]
response_headers[‘SET-COOKIE’’]
response_headers[‘DATE’]</td>
  </tr>
  <tr>
    <td>SMTP</td>
    <td>smtp_trans_depth</td>
    <td>int</td>
    <td>Depth of email into SMTP exchange</td>
    <td>2</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_helo</td>
    <td>string</td>
    <td>Helo header</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_mailfrom</td>
    <td>string</td>
    <td>Mailfrom header</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_rcptto</td>
    <td>string</td>
    <td>Rcptto header</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_date</td>
    <td>string</td>
    <td>Header date</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_from</td>
    <td>string</td>
    <td>From header</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_to</td>
    <td>string</td>
    <td>To header</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_reply_to</td>
    <td>string</td>
    <td>Reply to header</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_msg_id</td>
    <td>string</td>
    <td>Message ID </td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_in_reply_to</td>
    <td>string</td>
    <td>In reply to header</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_subject</td>
    <td>string</td>
    <td>Subject</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_x_originating_ip4</td>
    <td>int</td>
    <td>Originating IP address</td>
    <td>1203743731</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_first_received</td>
    <td>string</td>
    <td>First to receive message</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_headers_second_received</td>
    <td>string</td>
    <td>Second to receive message</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_last_reply</td>
    <td>string</td>
    <td>Last reply in message chain</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_path</td>
    <td>string</td>
    <td>Path of message</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_user_agent</td>
    <td>string</td>
    <td>User agent</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_tls</td>
    <td>boolean</td>
    <td>Indication of TLS use</td>
    <td>1</td>
  </tr>
  <tr>
    <td></td>
    <td>smtp_is_webmail</td>
    <td>boolean</td>
    <td>Indication of webmail</td>
    <td>0</td>
  </tr>
  <tr>
    <td>FTP</td>
    <td>ftp_user_name</td>
    <td>string</td>
    <td>Username</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_password</td>
    <td>string</td>
    <td>Password</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_command</td>
    <td>string</td>
    <td>FTP command</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_arg</td>
    <td>string</td>
    <td>Argument</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_mime_type</td>
    <td>string</td>
    <td>Mime type</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_file_size</td>
    <td>int</td>
    <td>File size</td>
    <td>1024</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_reply_code</td>
    <td>int</td>
    <td>Reply code</td>
    <td>3</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_reply_msg</td>
    <td>string</td>
    <td>Reply message</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_data_channel_passive</td>
    <td>boolean</td>
    <td>Passive data channel?</td>
    <td>1</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_data_channel_rsp_p</td>
    <td>string</td>
    <td></td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_cwd</td>
    <td>string</td>
    <td>Current working directory</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_cmdarg_ts</td>
    <td>float</td>
    <td></td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_cmdarg_cmd</td>
    <td>string</td>
    <td>Command</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_cmdarg_arg</td>
    <td>string</td>
    <td>Command argument</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_cmdarg_seq</td>
    <td>int</td>
    <td>Sequence</td>
    <td>2</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_pending_commands</td>
    <td>string</td>
    <td>Pending commands</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_is_passive</td>
    <td>boolean</td>
    <td>Passive mode enabled</td>
    <td>0</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_fuid</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>ftp_last_auth_requested</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td>SNMP</td>
    <td>snmp_version</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>snmp_community</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>“somestring”</td>
  </tr>
  <tr>
    <td></td>
    <td>snmp_get_requests</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>snmp_get_bulk_requests</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>snmp_get_responses</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>snmp_set_requests</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>snmp_display_string</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>snmp_up_since</td>
    <td>float</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td>TLS</td>
    <td>tls_version</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_cipher</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_curve</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_server_name</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_resumed</td>
    <td>boolean</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_next_protocol</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_established</td>
    <td>boolean</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_cert_chain_fuids</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_client_cert_chain_fuids</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_subject</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>tls_issuer</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td>SSH</td>
    <td>ssh_version</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ssh_auth_success</td>
    <td>boolean</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ssh_client</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ssh_server</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ssh_cipher_algorithm</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ssh_mac_algorithm</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ssh_compression_algorithm</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ssh_key_exchange_algorithm</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>ssh_host_key_algorithm</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td>DHCP</td>
    <td>dhcp_assigned_ip4</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>dhcp_mac</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>dhcp_lease_time</td>
    <td>int</td>
    <td>mechanism through which a DHCP server knows when a host will stop using an IP address</td>
    <td>2592000</td>
  </tr>
  <tr>
    <td>IRC</td>
    <td>irc_user</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>irc_nickname</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>irc_command</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>irc_value</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>irc_additional_data</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td>Flow</td>
    <td>flow_in_packets</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>flow_out_packets</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>flow_conn_state</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>flow_history</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>flow_src_dscp</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>flow_dst_dscp</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>flow_input</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>flow_output</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td>Vulnerability</td>
    <td>vuln_id</td>
    <td>string</td>
    <td>Unique vulnerability identifier</td>
    <td>10748</td>
  </tr>
  <tr>
    <td></td>
    <td>vuln_type</td>
    <td>string</td>
    <td>Vulnerability title (i.e. Wireshark Multiple Vulnerabilities)</td>
    <td></td>
  </tr>
  <tr>
    <td></td>
    <td>vuln_status</td>
    <td>string</td>
    <td>Vulnerability type (Potential, Confirmed, etc.)</td>
    <td></td>
  </tr>
  <tr>
    <td></td>
    <td>vuln_severity</td>
    <td>string</td>
    <td>Vulnerability severity (Critical, High, etc.)</td>
    <td></td>
  </tr>
  <tr>
    <td></td>
    <td>created</td>
    <td>long</td>
    <td>Timestamp of vulnerability identification</td>
    <td></td>
  </tr>
  <tr>
    <td>Antivirus</td>
    <td>av_riskname</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_actualaction</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_requestedaction
</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_secondaryaction
</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_downloadsite</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_downloadedby</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_tracking_status</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_firstseen</td>
    <td>bigint</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>application_hash</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>application_hash_type</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>application_name</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>application_version</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>application_type</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_categoryset</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_categorytype</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_threat_count</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_infected_count</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_omitted_count</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_scanid</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_startmessage</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_stopmessage</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_totalfiles</td>
    <td>int</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_signatureid</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_signaturestring</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_signaturesubid</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_intrusionurl</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>av_intrusionpayloadurl</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
  <tr>
    <td></td>
    <td>objectname</td>
    <td>string</td>
    <td>Coming soon</td>
    <td>Coming soon</td>
  </tr>
</table>


Note, it is not necessary to populate all of the attributes within the model.  For attributes not populated in a single security event log/alert, contextual data may not be available. For example, the sample event below can be enriched with contextual data about the referenced endpoints (10.1.1.1 and 192.168.10.10), but not a user, because username is not populated.

<table>
  <tr>
    <td>date,time,source_ip,source_port,protocol,destination_ip,destination_port,bytes
12/12/2015,23:14:56,10.1.1.1,1025,tcp,192.168.10.10,443,1183</td>
  </tr>
</table>


# Context Models

The recommended approach for populating the context models (user, endpoint, network, threat intelligence, etc.) involves consuming information from the systems most capable or providing the needed context.  Populating the user context model is best accomplished by leveraging user/identity management systems such as Active Directory or Centrify and populating the model with details such as the user’s full name, job title, phone number, manager’s name, physical address, entitlements, etc.  Similarly, an endpoint model can be populated by consuming information from endpoint/asset management systems (Tanium, Webroot, etc.), which provide information such as the services running on the system, system owner, business context, etc.

## User Context Model

The data model for user context information is as follows:

<table>
  <tr>
    <td>Attribute</td>
    <td>Data Type</td>
    <td>Description</td>
    <td>Sample Values</td>
  </tr>
  <tr>
    <td>dvc_time</td>
    <td>long</td>
    <td>Timestamp from when the user context information is obtained</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td>user_created</td>
    <td>long</td>
    <td>Timestamp from when user was created</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td>user_changed</td>
    <td>long</td>
    <td>Timestamp from when user was updated</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td>user_last_logon</td>
    <td>long</td>
    <td>Timestamp from when user last logged on</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td>user_logon_count</td>
    <td>int</td>
    <td>Number of times account has logged on</td>
    <td>232</td>
  </tr>
  <tr>
    <td>user_last_reset</td>
    <td>long</td>
    <td>Timestamp from when user last reset password</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td>user_expiration</td>
    <td>long</td>
    <td>Date/time when user expires</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td>user_id</td>
    <td>string</td>
    <td>Unique user id</td>
    <td>1234</td>
  </tr>
  <tr>
    <td>user_image</td>
    <td>binary</td>
    <td>Image/picture of user</td>
    <td></td>
  </tr>
  <tr>
    <td>user_name</td>
    <td>string</td>
    <td>Username in event log/alert</td>
    <td>jsmith</td>
  </tr>
  <tr>
    <td>user_name_first</td>
    <td>string</td>
    <td>First name</td>
    <td>John</td>
  </tr>
  <tr>
    <td>user_name_middle</td>
    <td>string</td>
    <td>Middle name</td>
    <td>Henry</td>
  </tr>
  <tr>
    <td>user_name_last</td>
    <td>string</td>
    <td>Last name</td>
    <td>Smith</td>
  </tr>
  <tr>
    <td>user_name_mgr</td>
    <td>string</td>
    <td>Manager’s name</td>
    <td>Ronald Reagan</td>
  </tr>
  <tr>
    <td>user_phone</td>
    <td>string</td>
    <td>Phone number</td>
    <td>703-555-1212</td>
  </tr>
  <tr>
    <td>user_email</td>
    <td>string</td>
    <td>Email address</td>
    <td>jsmith@company.com</td>
  </tr>
  <tr>
    <td>user_code</td>
    <td>string</td>
    <td>Job code</td>
    <td>3455</td>
  </tr>
  <tr>
    <td>user_loc</td>
    <td>string</td>
    <td>Location</td>
    <td>US</td>
  </tr>
  <tr>
    <td>user_departm</td>
    <td>string</td>
    <td>Department</td>
    <td>IT</td>
  </tr>
  <tr>
    <td>user_dn</td>
    <td></td>
    <td>Distinguished name</td>
    <td>"CN=scm-admin-mej-test2-adk,OU=app-admins,DC=ad,DC=halxg,DC=companya,DC=com"</td>
  </tr>
  <tr>
    <td>user_ou</td>
    <td>string</td>
    <td>Organizational unit</td>
    <td>EAST</td>
  </tr>
  <tr>
    <td>user_empid</td>
    <td>string</td>
    <td>Employee ID</td>
    <td>12345</td>
  </tr>
  <tr>
    <td>user_title</td>
    <td>string</td>
    <td>Job Title</td>
    <td>Director of IT</td>
  </tr>
  <tr>
    <td>user_groups</td>
    <td>array<String>  (Comma separated)</td>
    <td>Groups to which the user belongs</td>
    <td>"Domain Admins", “Domain Users”</td>
  </tr>
  <tr>
    <td>dvc_type</td>
    <td>string</td>
    <td>Device type that generated the user context data</td>
    <td>Active Directory</td>
  </tr>
  <tr>
    <td>dvc_vendor</td>
    <td>string</td>
    <td>Vendor</td>
    <td>Microsoft</td>
  </tr>
  <tr>
    <td>user_risk</td>
    <td>Floating point</td>
    <td>Risk score</td>
    <td>95.67</td>
  </tr>
  <tr>
    <td>dvc_version</td>
    <td>string</td>
    <td>Version </td>
    <td>8.1.2</td>
  </tr>
  <tr>
    <td>additional_attrs</td>
    <td>map</td>
    <td>Additional attributes of user</td>
    <td>Key value pairs</td>
  </tr>
</table>


## Endpoint Context Model

The data model for endpoint context information is as follows:

<table>
  <tr>
    <td>Abbreviation</td>
    <td>Data Type</td>
    <td>Description</td>
    <td>Sample Values</td>
  </tr>
  <tr>
    <td>dvc_time</td>
    <td>long</td>
    <td>Timestamp from when the endpoint context information is obtained</td>
    <td>1472653952</td>
  </tr>
  <tr>
    <td>end_ip4</td>
    <td>int</td>
    <td>IP address of endpoint </td>
    <td>Integer representation of 10.1.1.1</td>
  </tr>
  <tr>
    <td>end_ip6</td>
    <td>int128</td>
    <td>IP address of endpoint </td>
    <td>Integer representation of 10.1.1.1</td>
  </tr>
  <tr>
    <td>end_os</td>
    <td>string</td>
    <td>Operating system</td>
    <td>Redhat Linux 6.5.1</td>
  </tr>
  <tr>
    <td>end_os_version</td>
    <td>string</td>
    <td>Version of OS</td>
    <td>5.4</td>
  </tr>
  <tr>
    <td>end_os_sp</td>
    <td>string</td>
    <td>Service pack</td>
    <td>SP 2.3.4.55</td>
  </tr>
  <tr>
    <td>end_tz</td>
    <td>string</td>
    <td>timezone</td>
    <td>EST</td>
  </tr>
  <tr>
    <td>end_hotfixes</td>
    <td>array<String>  (Comma separated)</td>
    <td>Applied hotfixes</td>
    <td>993.2</td>
  </tr>
  <tr>
    <td>end_disks</td>
    <td>array<String>  (Comma separated)</td>
    <td>Available disks</td>
    <td>\\Device\\HarddiskVolume1, \\Device\\HarddiskVolume2</td>
  </tr>
  <tr>
    <td>end_removables</td>
    <td>array<String>  (Comma separated)</td>
    <td>Removable media devices</td>
    <td>USB Key</td>
  </tr>
  <tr>
    <td>end_nics</td>
    <td>array<String>  (Comma separated)</td>
    <td>Network interfaces</td>
    <td>fe10::28f4:1a47:658b:d6e8, fe82::28f4:1a47:658b:d6e8 </td>
  </tr>
  <tr>
    <td>end_drivers</td>
    <td>array<String>  (Comma separated)</td>
    <td>Installed kernel drivers</td>
    <td>ntoskrnl.exe, hal.dll</td>
  </tr>
  <tr>
    <td>end_users</td>
    <td>array<String>  (Comma separated)</td>
    <td>Local user accounts</td>
    <td>administrator, jsmith</td>
  </tr>
  <tr>
    <td>end_host</td>
    <td>string</td>
    <td>Hostname of endpoint</td>
    <td>tes1.companya.com</td>
  </tr>
  <tr>
    <td>end_mac</td>
    <td>string</td>
    <td>MAC address of endpoint</td>
    <td>fe10::28f4:1a47:658b:d6e8</td>
  </tr>
  <tr>
    <td>end_owner</td>
    <td>string</td>
    <td>Endpoint owner (name)</td>
    <td>John Smith</td>
  </tr>
  <tr>
    <td>end_vulns</td>
    <td>array<String>  (Comma separated)</td>
    <td>Vulnerability identifiers (CVE identifier)</td>
    <td>CVE-123, CVE-456</td>
  </tr>
  <tr>
    <td>end_loc</td>
    <td>string</td>
    <td>Location</td>
    <td>US</td>
  </tr>
  <tr>
    <td>end_departm</td>
    <td>string</td>
    <td>Department name</td>
    <td>IT</td>
  </tr>
  <tr>
    <td>end_company</td>
    <td>string</td>
    <td>Company name</td>
    <td>CompanyA</td>
  </tr>
  <tr>
    <td>end_regs</td>
    <td>array<String>  (Comma separated)</td>
    <td>Applicable regulations</td>
    <td>HIPAA, SOX</td>
  </tr>
  <tr>
    <td>end_svcs</td>
    <td>array<String>  (Comma separated)</td>
    <td>Services running on system</td>
    <td>Cisco Systems, Inc. VPN Service, Adobe LM Service</td>
  </tr>
  <tr>
    <td>end_procs</td>
    <td>array<String>  (Comma separated)</td>
    <td>Processes</td>
    <td>svchost.exe, sppsvc.exe</td>
  </tr>
  <tr>
    <td>end_criticality</td>
    <td>string</td>
    <td>Criticality of device</td>
    <td>Very High</td>
  </tr>
  <tr>
    <td>end_apps</td>
    <td>array<String>  (Comma separated)</td>
    <td>Applications running on system</td>
    <td>Microsoft Word, Chrome</td>
  </tr>
  <tr>
    <td>end_desc</td>
    <td>string</td>
    <td>Endpoint descriptor</td>
    <td>Some string</td>
  </tr>
  <tr>
    <td>dvc_type</td>
    <td>string</td>
    <td>Device type that generated the log</td>
    <td>Microsoft Windows 7</td>
  </tr>
  <tr>
    <td>dvc_vendor</td>
    <td>string</td>
    <td>Vendor</td>
    <td>Endgame</td>
  </tr>
  <tr>
    <td>dvc_version</td>
    <td>string</td>
    <td>Version </td>
    <td>2.1</td>
  </tr>
  <tr>
    <td>end_architecture</td>
    <td>string</td>
    <td>CPU architecture</td>
    <td>x86</td>
  </tr>
  <tr>
    <td>end_uuid</td>
    <td>string</td>
    <td>Universally unique identifier</td>
    <td>a59ba71e-18b0-f762-2f02-0deaf95076c6</td>
  </tr>
  <tr>
    <td>end_risk</td>
    <td>Floating point</td>
    <td>Risk score</td>
    <td>95.67</td>
  </tr>
  <tr>
    <td>end_memtotal</td>
    <td>int</td>
    <td>Total memory (bytes)</td>
    <td>844564433</td>
  </tr>
  <tr>
    <td>additional_attrs</td>
    <td>map</td>
    <td>Additional attributes</td>
    <td>Key value pairs</td>
  </tr>
</table>


## Vulnerability Context Model

The data model for vulnerability context information is as follows:

<table>
  <tr>
    <td>Attribute</td>
    <td>Data Type</td>
    <td>Description</td>
    <td></td>
  </tr>
  <tr>
    <td>vuln_id</td>
    <td>string</td>
    <td>Unique vulnerability identifier</td>
    <td>10748</td>
  </tr>
  <tr>
    <td>vuln_title</td>
    <td>string</td>
    <td>Vulnerability title</td>
    <td>"Wireshark Multiple Vulnerabilities"</td>
  </tr>
  <tr>
    <td>vuln_description</td>
    <td>string</td>
    <td>Vulnerability description</td>
    <td></td>
  </tr>
  <tr>
    <td>vuln_solution</td>
    <td>string</td>
    <td>Vulnerability remediation description</td>
    <td>“Patch:  The following URLs provide patch procedures ..”</td>
  </tr>
  <tr>
    <td>vuln_type</td>
    <td>string</td>
    <td>Vulnerability type</td>
    <td>Potential, Confirmed, etc.</td>
  </tr>
  <tr>
    <td>vuln_category</td>
    <td>string</td>
    <td>Vulnerability category</td>
    <td>Ubuntu, Windows, etc.</td>
  </tr>
  <tr>
    <td>vuln_status</td>
    <td>string</td>
    <td>Vulnerability status</td>
    <td>Active, Fixed, etc.</td>
  </tr>
  <tr>
    <td>vuln_severity</td>
    <td>string</td>
    <td>Vulnerability severity</td>
    <td>Critical, High, Medium, etc.</td>
  </tr>
  <tr>
    <td>vuln_created</td>
    <td>long</td>
    <td>Vulnerability creation timestamp</td>
    <td>timestamp</td>
  </tr>
  <tr>
    <td>vuln_updated</td>
    <td>long</td>
    <td>Vulnerability updated timestamp</td>
    <td>timestamp</td>
  </tr>
  <tr>
    <td>additional_attrs</td>
    <td>map</td>
    <td>Additional attributes</td>
    <td>Key value pairs</td>
  </tr>
</table>


## Network Context Model

The data model for network context information is based on "whois" information as follows:

<table>
  <tr>
    <td>Attribute</td>
    <td>Data Type</td>
    <td>Description</td>
    <td></td>
  </tr>
  <tr>
    <td>net_domain_name</td>
    <td>string</td>
    <td>Domain name</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registry_domain_id</td>
    <td>string</td>
    <td>Registry Domain ID</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrar_whois_server</td>
    <td>string</td>
    <td>Registrar WHOIS Server</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrar_url</td>
    <td>string</td>
    <td>Registrar URL</td>
    <td></td>
  </tr>
  <tr>
    <td>net_update_date</td>
    <td>long</td>
    <td>UTC timestamp</td>
    <td></td>
  </tr>
  <tr>
    <td>net_creation_date</td>
    <td>bigint</td>
    <td>Creation Date</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrar_registration_expiration_date</td>
    <td>bigint</td>
    <td>Registrar Registration Expiration Date</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrar</td>
    <td>string</td>
    <td>Registrar</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrar_iana_id</td>
    <td>string</td>
    <td>Registrar IANA ID</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrar_abuse_contact_email</td>
    <td>string</td>
    <td>Registrar Abuse Contact Email</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrar_abuse_contact_phone</td>
    <td>string</td>
    <td>Registrar Abuse Contact Phone</td>
    <td></td>
  </tr>
  <tr>
    <td>net_domain_status</td>
    <td>string</td>
    <td>Domain Status</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registry_registrant_id</td>
    <td>string</td>
    <td>Registry Registrant ID</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_name</td>
    <td>string</td>
    <td>Registrant Name</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_organization</td>
    <td>string</td>
    <td>Registrant Organization</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_street</td>
    <td>string</td>
    <td>Registrant Street</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_city</td>
    <td>string</td>
    <td>Registrant City</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_state_province</td>
    <td>string</td>
    <td>Registrant State/Province</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_postal_code</td>
    <td>string</td>
    <td>Registrant Postal Code</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_country</td>
    <td>string</td>
    <td>Registrant Country</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_phone</td>
    <td>string</td>
    <td>Registrant Phone</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registrant_email</td>
    <td>string</td>
    <td>Registrant Email</td>
    <td></td>
  </tr>
  <tr>
    <td>net_registry_admin_id</td>
    <td>string</td>
    <td>Registry Admin ID</td>
    <td></td>
  </tr>
  <tr>
    <td>net_name_servers</td>
    <td>string</td>
    <td>Name Server</td>
    <td></td>
  </tr>
  <tr>
    <td>net_dnssec</td>
    <td>string</td>
    <td>DNSSEC</td>
    <td></td>
  </tr>
  <tr>
    <td>net_risk</td>
    <td>Floating point</td>
    <td>Risk score</td>
    <td>95.67</td>
  </tr>
</table>


## Threat Intelligence Context Model

The data model for threat intelligence context information is as follows:

<table>
  <tr>
    <td>Attribute</td>
    <td>Data Type</td>
    <td>Description</td>
  </tr>
  <tr>
    <td>ti_source</td>
    <td>String</td>
    <td>TI Provider, Open Source List, Internally Developed, LE Tip, Other</td>
  </tr>
  <tr>
    <td>ti_provider_id</td>
    <td>String</td>
    <td>Anomali, CrowdStrike, Mandiant, Alienvault OTX, USCERT, etc</td>
  </tr>
  <tr>
    <td>ti_indicator_id</td>
    <td>String</td>
    <td>Unique IQ from the provider</td>
  </tr>
  <tr>
    <td>ti_indicator_desc</td>
    <td>String</td>
    <td>Full Text descriptor and links of the Indicator and associated information</td>
  </tr>
  <tr>
    <td>ti_date_added</td>
    <td>UTC Timestamp</td>
    <td>Date first added by the provider</td>
  </tr>
  <tr>
    <td>ti_date_modified</td>
    <td>UTC Timestamp</td>
    <td>Date last updated by the provider.</td>
  </tr>
  <tr>
    <td>ti_risk_impact</td>
    <td>String</td>
    <td>Likely Targets what function within the organization?</td>
  </tr>
  <tr>
    <td>ti_severity</td>
    <td>String</td>
    <td>Nation State, Targeted, Advanced, Commodity, Other</td>
  </tr>
  <tr>
    <td>ti_category</td>
    <td>String
</td>
    <td>Ecrime, Hacktivism, Geo Pollitical, Foreign Intelligence Service</td>
  </tr>
  <tr>
    <td>ti_campaign_name</td>
    <td>String</td>
    <td>Internal Campaign designation</td>
  </tr>
  <tr>
    <td>ti_deployed_location</td>
    <td>array<String> (Comma separated)</td>
    <td>Where this indicator should be matched for applicability (Core, Perimeter, Network, Endpoint, Logs, ALL, etc)</td>
  </tr>
  <tr>
    <td>ti_associated_incidents</td>
    <td>String</td>
    <td>Known Associated Incident ID's</td>
  </tr>
  <tr>
    <td>ti_adversarial_identification_group</td>
    <td>String</td>
    <td>Adversary Group designation usually provided by the provider.</td>
  </tr>
  <tr>
    <td>ti_adversarial_identification_tactics</td>
    <td>String</td>
    <td>Known Adversary Tactics as indicated by the source provider.</td>
  </tr>
  <tr>
    <td>ti_adversarial_identification_reports</td>
    <td>String</td>
    <td>Linked Adversary reports.</td>
  </tr>
  <tr>
    <td>ti_phase</td>
    <td>String</td>
    <td>Discovery, Weaponization, Delivery, C2, Exploitation, Actions on Objectives, etc</td>
  </tr>
  <tr>
    <td>ti_indicator_cve</td>
    <td>String</td>
    <td>MITRE CVE Link(s)</td>
  </tr>
  <tr>
    <td>ti_indicator_ip4</td>
    <td>array<bigINT></td>
    <td>CIDR noted IPv4 Address Indicated by Threat Intelligence</td>
  </tr>
  <tr>
    <td>ti_indicator_ip6</td>
    <td>array<int128></td>
    <td>IPv6 Address Indicated by Threat Intelligence</td>
  </tr>
  <tr>
    <td>ti_indicator_domain</td>
    <td>String</td>
    <td>Domain Name(s)</td>
  </tr>
  <tr>
    <td>ti_indicator_hostname</td>
    <td>String</td>
    <td>Host or Subdomain Name(es)</td>
  </tr>
  <tr>
    <td>ti_indicator_email</td>
    <td>array<String>  (Comma separated)</td>
    <td>Email addresses associated with Indicator</td>
  </tr>
  <tr>
    <td>ti_indicator_url</td>
    <td>array<String>  (Comma separated)</td>
    <td>URL(s) associated with indicator</td>
  </tr>
  <tr>
    <td>ti_indicator_uri</td>
    <td>array<String>  (Comma separated)</td>
    <td>URI(s) associated with indicator</td>
  </tr>
  <tr>
    <td>ti_indicator_file_hash</td>
    <td>String</td>
    <td>File Hash Value associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_path</td>
    <td>String</td>
    <td>File Path Value associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_mutex</td>
    <td>String</td>
    <td>MUTEX Value associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_md5</td>
    <td>String</td>
    <td>MD5 Hash Sum Value</td>
  </tr>
  <tr>
    <td>ti_indicator_sha1</td>
    <td>String</td>
    <td>SHA1 Hash Sum Value</td>
  </tr>
  <tr>
    <td>ti_indicator_sha256</td>
    <td>String</td>
    <td>SHA256 Hash Sum Value</td>
  </tr>
  <tr>
    <td>ti_indicator_device_path</td>
    <td>String</td>
    <td>Device Path Value associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_drive</td>
    <td>String</td>
    <td>Drive Value associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_name</td>
    <td>String</td>
    <td>File Name Value associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_extension</td>
    <td>String</td>
    <td>File Extension Value associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_size</td>
    <td>String</td>
    <td>File Size Value associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_created</td>
    <td>bigint</td>
    <td>Date File value associated with the indicator was created.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_accessed</td>
    <td>bigint</td>
    <td>Date File value associated with the indicator was last accessed.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_changed</td>
    <td>bigint</td>
    <td>Date File value associated with the indicator was last changed.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_entropy</td>
    <td>String</td>
    <td>Calculated entropy value associated with the file indicated.</td>
  </tr>
  <tr>
    <td>ti_indicator_file_attributes</td>
    <td>array<String>  (Comma separated)</td>
    <td>Read Only, System, Hidden, Directory, Archive, Device, Temporary, SparseFile, Compressed, Encrypted, Index, Deleted, etc</td>
  </tr>
  <tr>
    <td>ti_indicator_user_name</td>
    <td>String</td>
    <td>username associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_security_id</td>
    <td>String</td>
    <td>if known securityID associated with the indicator.</td>
  </tr>
  <tr>
    <td>ti_indicator_pe_info</td>
    <td>array<String>  (Comma separated)</td>
    <td>Subsystem, BaseAddress, PETImeStamp, Expert, JumpCodes, DetectedAnomalies, DigitalSignatures,VersionInfo, ResourceInfo,Imported Modules</td>
  </tr>
  <tr>
    <td>ti_indicator_pe_type</td>
    <td>array<String>  (Comma separated)</td>
    <td>Executable, DLL, Invalid, Unknown, Native, Windows_GUI, OS2, POSIX, EFI, etc</td>
  </tr>
  <tr>
    <td>ti_indicator_strings</td>
    <td>array<String>  (Comma separated)</td>
    <td>Any strings associated with the file indicated that might be useful in identification or further indicator development or adversary identification.</td>
  </tr>
  <tr>
    <td>ti_indicator_org</td>
    <td>String</td>
    <td>Name of the business that owns the IP address associated with the indicator</td>
  </tr>
  <tr>
    <td>ti_indicator_reg_name</td>
    <td>String</td>
    <td>Name of the person who registered the domain</td>
  </tr>
  <tr>
    <td>ti_indicator_reg_email</td>
    <td>String</td>
    <td>Email address of the person who registered the domain</td>
  </tr>
  <tr>
    <td>ti_indicator_reg_org</td>
    <td>String</td>
    <td>Name of the organisation that registered the domain</td>
  </tr>
  <tr>
    <td>ti_indicator_reg_phone</td>
    <td>String</td>
    <td>Phone number associated with the domain registered</td>
  </tr>
  <tr>
    <td>ti_tags</td>
    <td>String</td>
    <td>Additional comments/associations from the feed</td>
  </tr>
  <tr>
    <td>ti_threat_type</td>
    <td>String</td>
    <td>malware, compromised, apt, c2, etc...</td>
  </tr>
</table>


# Extensibility of Data Model

The aforementioned data model can be extended to accommodate custom attributes by embedding key-value pairs within the log/alert/context entries. 

Each model will support an additional attribute by the name of additional_attrs whose value would be a JSON string. This JSON string will contain a Map (and only a Map) of additional attributes that can’t be expressed in the specified model description. Regardless of the type of these additional attributes, they will always be interpreted as String. It’s up to the user, to translate them to appropriate types, if necessary, in the analytics layer. It is also the user’s responsibility to populate the aforementioned attribute as a Map, by presumably parsing out these attributes from the original message.

For example, if a user wanted to extend the user context model to include a string attribute for "Desk Location" and “City”, the following string would be set for additional_attrs:

<table>
  <tr>
    <td>Attribute key</td>
    <td>Attribute value</td>
  </tr>
  <tr>
    <td>additional_attrs</td>
    <td>{"dsk_location":"B3-F2-W3", "city":"Palo Alto"}</td>
  </tr>
</table>


Something similar can be done for endpoint context model, security event log/alert model and other entities.

**Note: **[This UDF library](https://github.com/klout/brickhouse) can be used for converting to/from JSON.

# Model Relationships

The relationships between the data model entities are illustrated below.

# ![image alt text](https://lh3.googleusercontent.com/-SxEubiTPzFE/WHVo0uxgJtI/AAAAAAAAAt8/3su9v3h0MsovJ0Mhy08EbuFTvRvKEoIwQCLcB/s0/ODMimage2.jpg)

# Data Ingestion Framework

One of the challenges in populating the data model is the large number of products and technologies that organizations are currently using to manage security event logs/alerts, user and endpoint information. There are literally dozens of vendors in each category that offer technologies that could be used to populate the model.  The labor required to transform the data and map the attributes to the data model is extensive when you consider how many technologies are in the mix at each organization (and across organizations). One way to address this challenge is with a Data Ingestion Framework that provides a configuration-based mechanism to perform the transformations and mappings.  A configuration-based capability will allow the ingest pipelines to become portable and reusable across the community. For example, if I create an ingest pipeline for Centrify to populate the user context model, it can be shared with other users of Centrify who can immediately realize the benefit.  Such a framework could allow the community to quickly build the necessary pipelines for the dozens (and hundreds) of technologies being used in the market.  Without a standard ingest framework, each pipeline is built independently, requiring more labor, providing no standardization and little portability.  It’s also important that the data ingestion framework support the ability to both capture the "raw" event and create a metaevent that represents the normalized event and maps the attributes to the defined data model.  This will ensure both stream and batch processing use cases are supported.

 

Streamsets is an ingest framework that provides the needed functionality outlined above.  Sample Streamsets ingest pipelines for populating the ODM with common data sources will be published to the Spot Github repo.

# Data Formats

The following data formats are recommended for use with the Spot open data model.

## Avro

Avro is the recommended data format due to its schema representation, compatibility checks, and interoperability with Hadoop.  Avro supports a pure JSON representation for readability and ease of use but also a binary representation of the data for efficient storage.   Avro is the optimal format for streaming-based analytic use cases.

A sample event and corresponding schema representation are detailed below.

Event

{

<table>
  <tr>
    <td>"event_time":1469562994,</td>
  </tr>
  <tr>
    <td>“net_src_ip4”:”192.168.1.1”,</td>
  </tr>
  <tr>
    <td>“net_src_host”:”test1.clouera.com”,</td>
  </tr>
  <tr>
    <td>“net_src_port”:1029,</td>
  </tr>
  <tr>
    <td>“net_dst_ip4”:”192.168.21.22”,</td>
  </tr>
  <tr>
    <td>“net_dst_host”:”test3.companyA.com”</td>
  </tr>
  <tr>
    <td>“net_dst_port”:443,</td>
  </tr>
  <tr>
    <td>“dvc_type”:”sshd”,</td>
  </tr>
  <tr>
    <td>“category”:”auth”</td>
  </tr>
  <tr>
    <td>“a_proto”:”sshd”</td>
  </tr>
  <tr>
    <td>“msg”:”user:mhicks successfully logged in  to test3.companyA.com from 192.168.1.1”,</td>
  </tr>
  <tr>
    <td>“user_name”:”mhicks”,</td>
  </tr>
  <tr>
    <td>“Severity”:3,</td>
  </tr>
</table>


}

Schema

{

  "type": "record",

  "doc":"This event records SSHD activity",

  "name": "auth",

  "fields" : [

{"name":"event_time", "type":"long", "doc":"Stop time of event""},

{"name":"net_src_ip4", "type":"int", "doc":"Source IP Address"},

{"name":"net_src_host", "type":"string","doc”:”Source hostname},

{"name":"net_src_port", "type":"int","doc”:”Source port”},

{"name":"net_dst_ip4", "type":"int", "doc"::"Destination IP Address"},

{"name":"net_dst_host", "type":"string", "doc":"Destination IP Address"},

{"name":"net_dst_port", "type":"int", "doc”:”Destination port”},

{"name":"dvc_type", "type":"string", "doc":”Source device type”},

 {"name":"category", "type":"string","doc”:”category/type of event message”},

 {"name":"a_proto", "type":"string","doc”:”Application or network protocol”},

    {"name":"msg", "type":"string","doc”:”event message”},

    {"name":"user_name", "type":"string","doc”:”username”},

    {"name":"severity", "type":"int","doc”:”severity of event on scale of 1-10”},

}

## JSON

JSON is commonly used as a data-interchange format due to it’s ease of use and familiarity within the development community.  The corresponding JSON object for the sample event described previously is noted below.

{

<table>
  <tr>
    <td>"event_time":1469562994,</td>
  </tr>
  <tr>
    <td>“net_src_ip4”:”192.168.1.1”,</td>
  </tr>
  <tr>
    <td>“net_src_host”:”test1.clouera.com”,</td>
  </tr>
  <tr>
    <td>“net_src_port”:1029,</td>
  </tr>
  <tr>
    <td>“net_dst_ip4”:”192.168.21.22”,</td>
  </tr>
  <tr>
    <td>“net_dst_host”:”test3.companyA.com”,</td>
  </tr>
  <tr>
    <td>“net_dst_port”:443,</td>
  </tr>
  <tr>
    <td>“a_proto”:”sshd”</td>
  </tr>
  <tr>
    <td>“msg”:”user:mhicks successfully logged in  to test3.companyA.com from 192.168.1.1”,</td>
  </tr>
  <tr>
    <td>“user_name”:”mhicks”,</td>
  </tr>
</table>


}

## Parquet

Parquet is a columnar storage format that offers the benefits of compression and efficient columnar data representation and is optimal for batch analytic use cases.  More information on parquet can be found here:  [https://parquet.apache.org/documentation/latest/](https://parquet.apache.org/documentation/latest/) 

It should be noted that conversion from Avro to Parquet is supported. This allows for data collected and analyzed for stream-based use cases to be easily converted to Parquet for longer-term batch analytics.

# ODM Resultant Capability - A Singular View

The resultant capability provided by the Spot ODM is the ability to bring together all the security relevant data from the entities referenced (event, user, network, endpoint, etc.) into a singular view that can be used to detect threats more effectively than ever before.  The singular view can be leveraged to create new analytic models that were not previously possible and to provide needed context at the event level to effectively determine whether or not there is a threat.  

## **Example - Advanced Threat Modeling** 

In this example, the ODM is leveraged to build an "event" table for a threat model that uses attributes native to the ODM and derived attributes, which are calculations based on the aggregate data stored in the model.  In this context, an “event” table is defined by the attributes to be evaluated for predictive power in identifying threats and the actual attribute values (i.e rows in the table).  In the example below, the event table is composed of the following attributes, which are then leveraged to identify threats via a Risk Score analytic model:

* **"net_src_ipv4"** - This attribute is native to the security event log component of the ODM and represents the source IP address of the corresponding table row

* **"os" **- This attribute is native to the endpoint context component of the ODM and represents the operating system of the endpoint system in the table row

* **SUM (in_bytes + out_bytes)** for the last 7 days - "in_bytes" and “out_bytes” are native to the security event log component of the ODM.  This derived attribute represents a summation of bytes between the source address and destination domain over the last 7 days

* **"net_dst_domain"** - This attribute is native to the security event log component of the ODM and represents the destination domain 

* **Days since "creation_date"** - “creation_date” is native to the network context component of the ODM and represents the date the referenced domain was registered. This derived attribute calculates the days since the domain was created/registered.

<table>
  <tr>
    <td>net_src_ipv4</td>
    <td>os</td>
    <td>net_dst_domain</td>
    <td>Days since "creation_date"</td>
    <td>SUM (in_bytes + out_bytes)</td>
    <td>Risk Score (1-100)</td>
  </tr>
  <tr>
    <td>10.1.1.10</td>
    <td>Microsoft</td>
    <td>dajdkwk.com</td>
    <td>39</td>
    <td>3021 MB</td>
    <td>99</td>
  </tr>
  <tr>
    <td>192.168.8.9</td>
    <td>Redhat</td>
    <td>usatoday.com</td>
    <td>3027</td>
    <td>2 MB</td>
    <td>2</td>
  </tr>
  <tr>
    <td>172.16.32.3</td>
    <td>Apple</td>
    <td>box.com</td>
    <td>1532</td>
    <td>76 MB</td>
    <td>10</td>
  </tr>
  <tr>
    <td>192.168.4.4</td>
    <td>Microsoft</td>
    <td>kzjkeljr.ru</td>
    <td>3</td>
    <td>0.9 MB</td>
    <td>92</td>
  </tr>
</table>


The "Risk Score"  attribute represents potential output from a threat detection model based on the attributes and values represented in the “event” table and is provided as an example of what is enabled by the ODM.  **Can you tell which attributes and values hold predictive power for threat detection?**

## **Example - Singular Data View for Complete Context**

The table below demonstrates a logical, "denormalized" view of what is offered by the ODM.  In this example, the raw DNS event is mapped to the ODM, which is enriching the DNS event with Endpoint and Network context needed to make a proper threat determination.  For large datasets, this type of view is not performant or reasonable to provide with databases upon which legacy security analytic technologies are built.  However, this singular/denormalized data representation is feasible with Spot.

**RAW DNS EVENT**

1463702961,169,10.0.0.101,172.16.36.157,www.kzjkeljr.ru,1,0x00000001,0,49.52.46.49

**DNS EVENT + ODM**

<table>
  <tr>
    <td>ODM Attribute</td>
    <td>Value</td>
    <td>Description</td>
    <td>ODM Context Attributes</td>
  </tr>
  <tr>
    <td>event_time</td>
    <td>1463702961</td>
    <td>UTC timestamp of DNS query</td>
    <td></td>
  </tr>
  <tr>
    <td>length</td>
    <td>169</td>
    <td>DNS Frame length</td>
    <td></td>
  </tr>
  <tr>
    <td>net_dst_ip4</td>
    <td>10.1.0.11</td>
    <td>Destination address (DNS server)</td>
    <td>Endpoint Context
os="Redhat 6.3”
host=”dns.companyA.com”
mac=”94:94:26:3:86:16”
departm=”IT”
regs=”PCI”
vulns=”CVE-123, CVE-456,...”
….</td>
  </tr>
  <tr>
    <td>net_src_ip4</td>
    <td>172.16.32.17</td>
    <td>Source address (DNS query initiator)</td>
    <td>Endpoint Context
os=”Microsoft Windows 7”
host=”jsmith.companyA.com”
mac=”94:94:26:3:86:17”
departm=”FCE”
regs=”Corporate”
apps=”Office 365, Visio 12.2, Chrome 52.0.3….”
vulns=”CVE-123, CVE-456,...”
….</td>
  </tr>
  <tr>
    <td>dns_query</td>
    <td>www.kzjkeljr.ru</td>
    <td>DNS query</td>
    <td>Network Context
domain_name=”kzjkeljr.ru”
Creation_date”2016-08-30”
registrar_registration_expiration_date=”2016-09-30”
registration_country=”Russia”
….</td>
  </tr>
  <tr>
    <td>dns_class</td>
    <td>1</td>
    <td>DNS query class</td>
    <td></td>
  </tr>
  <tr>
    <td>dns_code</td>
    <td>0x00000001</td>
    <td>DNS response code</td>
    <td></td>
  </tr>
  <tr>
    <td>dns_answer</td>
    <td>49.52.46.49</td>
    <td>A record, DNS query response</td>
    <td></td>
  </tr>
</table>


