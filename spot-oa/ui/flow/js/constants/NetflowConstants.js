var NetflowConstants = {
  // Netflow Actions
  RELOAD_INGEST_SUMMARY: 'RELOAD_INGEST_SUMMARY',
  // INGEST SUMMARY
  START_DATE: 'start-date',
  END_DATE: 'end-date',
  // Data source URLS
  API_SUSPICIOUS: '../../data/flow/${date}/flow_scores.csv',
  API_DETAILS: '../../data/flow/${date}/edge-${src_ip}-${dst_ip}-${time}.tsv',
  API_VISUAL_DETAILS: '../../data/flow/${date}/chord-${ip}.tsv',
  API_COMMENTS: '../../data/flow/${date}/threats.csv',
  API_INCIDENT_PROGRESSION: '../../data/flow/${date}/threat-dendro-${id}.json',
  API_INGEST_SUMMARY: '../../data/flow/ingest_summary/is_${year}${month}.csv',
  API_IMPACT_ANALYSIS: '../../data/flow/${date}/stats-${id}.json',
  API_GLOBE_VIEW: '../../data/flow/${date}/globe-${id}.json',
  API_TIMELINE: '../../data/flow/${date}/sbdet-${id}.tsv', 
};

module.exports = NetflowConstants;

