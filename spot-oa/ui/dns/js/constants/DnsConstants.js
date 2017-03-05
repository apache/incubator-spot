// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var DnsConstants = {
  // API URLS
  API_SUSPICIOUS: '../../data/dns/${date}/dns_scores.csv',
  API_DETAILS: '../../data/dns/${date}/edge-${dns_qry_name}_${time}.csv',
  API_VISUAL_DETAILS: '../../data/dns/${date}/dendro-${ip_dst}.csv',
  API_COMMENTS: '../../data/dns/${date}/threats.csv',
  API_INCIDENT_PROGRESSION: '../../data/dns/${date}/threat-dendro-${id}.csv'
};

module.exports = DnsConstants;
