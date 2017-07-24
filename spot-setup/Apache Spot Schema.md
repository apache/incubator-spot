# Apache Spot Schema 

This document is to centralize a place where users can read information about Proxy, DNS and Netflow schema.

## Proxy

| Spot Field Name | Type   | Description                                 | Original Field Name | Format     | Spot-ingest | Spot-ml | Spot-oa | Spot-ui | 
|-----------------|--------|---------------------------------------------|---------------------|------------|-------------|---------|---------|---------|
| p_date          | string | Date for the connection                     |        date         | yyyy-mm-dd |   required  |    x    |    x    |    x    |
| p_time	      | string | Time for the connection	                 |        time	       |  hh:MM:SS  |	required  |    x    |    x    |    x    |