This folder must include the following files:

### **ipranges.csv**
This is a comma separated file, defines a range of IP that should be considered as part of your internal network.  
This file will be requested by the following modules:

        - oa/flow
        - oa/dns
        - oa/proxy

Schema with zero-indexed columns:

        0.start_IP : string
        1.limit_IP : string
        2.name : string

### **iploc.csv**
This is a comma separated geolocation database file containing all (or most) known public IP ranges and the details of its location.
This file is required by the following modules:

        - oa/components/geoloc (Only for flow)
        - oa/flow/ipynb_templates/threat_investigation_master.ipynb (Notebook)  
   
Schema with zero-indexed columns:

        0. IP Range start (string)
        1. IP Range end (string)
        2. Country code (string)
        3. Country (string)
        4. State (string)
        5. City (string)
        6. Latitude (string)
        7. Longitude (string)
        8. Owner or Register (string)
        9. Domain name (string)        

The schema is based on the GeoIP database by [MAXMIND](https://www.maxmind.com). Either users acquire MAXMIND database or any other database or even create their own, it should always comply the schema above. This includes enclosing every field in double quotes.

Ip ranges are defined by Ips converted into integers. To calculate the integer value from an IP first you need to split the string by "." into four values or octets. Then apply the following formula: (1st octet * 256^3) + (2nd octet * 256^2) + (3th octet * 256) + (4th octet).

Example: 10.180.88.23
1st octet = 10
2nd octet = 180
3th octet = 88
4th octet = 23

Ip to integer = (10 * 16777216) + (180 * 65536) + (88 * 256) + (23) = 179591191.

### **networkcontext_1.csv**
This is a comma separated file necessary to add more specific context about your network.
This file will be requested by the following modules:

        - oa/components/nc (only for flow)
        - oa/flow/threat_investigation_master.ipynb (Notebook)  

Schema with zero-indexed columns:

        0.srcIP : string
        1.name : string

    Where _srcIP_ is the IP or IPs that can be grouped into the same context in the network. This column can have one of the following formats:

        - IP range (192.0.54.0-192.0.54.254)  
        - Subnets 192.0.54.0/24 (192.0.54.0-192.0.54.254)    
        - Unique IP  



