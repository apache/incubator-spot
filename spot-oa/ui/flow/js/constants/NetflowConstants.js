var NetflowConstants = {
  // Data source URLS
  API_SUSPICIOUS: '../../data/flow/${date}/flow_scores.csv',
  API_DETAILS: '../../data/flow/${date}/edge-${src_ip}-${dst_ip}-${time}.tsv',
  API_VISUAL_DETAILS: '../../data/flow/${date}/chord-${ip}.tsv',
  API_COMMENTS: '../../data/flow/${date}/threats.csv',
  API_INCIDENT_PROGRESSION: '../../data/flow/${date}/threat-dendro-${ip}.json',
  API_IMPACT_ANALYSIS: '../../data/flow/${date}/stats-${ip}.json',
  API_GLOBE_VIEW: '../../data/flow/${date}/globe-${ip}.json',
  API_TIMELINE: '../../data/flow/${date}/sbdet-${ip}.tsv'
};

module.exports = NetflowConstants;
