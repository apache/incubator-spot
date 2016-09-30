var ProxyConstants = {
    // API URLS
    API_SUSPICIOUS: '../../data/proxy/${date}/proxy_scores.tsv',
    API_DETAILS: '../../data/proxy/${date}/edge-${clientip}-${hash}.tsv',
    API_COMMENTS: '../../data/proxy/${date}/threats.csv',
    API_INCIDENT_PROGRESSION: '../../data/proxy/${date}/incident-progression-${hash}.json',
    API_TIMELINE: '../../data/proxy/${date}/timeline-${hash}.tsv'
};

module.exports = ProxyConstants;
