//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

const SpotConstants = {
  PIPELINE_NETFLOW: 'flow',
  PIPELINE_DNS: 'dns',
  PIPELINE_PROXY: 'proxy',
  UPDATE_PIPELINE: 'UPDATE_PIPELINE',
  // Search Actions
  UPDATE_FILTER: 'UPDATE_FILTER',
  UPDATE_DATE: 'UPDATE_DATE',
  // Panel Actions
  EXPAND_PANEL: 'EXPAND_PANEL',
  RESTORE_PANEL: 'RESTORE_PANEL',
  TOGGLE_MODE_PANEL: 'TOGGLE_MODE_PANEL',
  DETAILS_MODE: 'DETAILS_MODE',
  VISUAL_DETAILS_MODE: 'VISUAL_DETAILS_MODE',
  // Panel
  SUSPICIOUS_PANEL:'Suspicious',
  NETVIEW_PANEL: 'Network View',
  NOTEBOOK_PANEL: 'Notebook',
  NO_IPYTHON_NOTEBOOK: 'NoIpythonNotebooks',
  SCORING_PANEL: 'Scoring',
  SAVE_SCORED_ELEMENTS: 'SAVE_SCORED_ELEMENTS',
  RESET_SCORED_ELEMENTS: 'RESET_SCORED_ELEMENTS',
  CHANGE_CSS_CLS: 'CHANGE_CSS_CLS',
  DETAILS_PANEL: 'Details',
  COMMENTS_PANEL:'Executive Threat Briefing',
  INCIDENT_PANEL:'Incident Progression',
  IMPACT_ANALYSIS_PANEL:'Impact Analysis',
  GLOBE_VIEW_PANEL:'Map View | Globe',
  TIMELINE_PANEL:'Timeline',
  INGEST_SUMMARY_PANEL:'Ingest Summary',
  // Edge Investigation
  MAX_SUSPICIOUS_ROWS: 250,
  RELOAD_SUSPICIOUS: 'RELOAD_SUSPICIOUS',
  RELOAD_DETAILS: 'RELOAD_DETAILS',
  RELOAD_VISUAL_DETAILS: 'RELOAD_VISUAL_DETAILS',
  HIGHLIGHT_THREAT: 'HIGHLIGHT_THREAT',
  UNHIGHLIGHT_THREAT: 'UNHIGHLIGHT_THREAT',
  SELECT_THREAT: 'SELECT_THREAT',
  SELECT_IP: 'SELECT_IP',
  // Storyboard
  RELOAD_COMMENTS: 'RELOAD_COMMENTS',
  SELECT_COMMENT: 'SELECT_COMMENT',
  // INGEST SUMMARY
  API_INGEST_SUMMARY: '../data/${pipeline}/ingest_summary/is_${year}${month}.csv',
  RELOAD_INGEST_SUMMARY: 'RELOAD_INGEST_SUMMARY',
  START_DATE: 'start-date',
  END_DATE: 'end-date',
  // Server Paths
  NOTEBOOKS_PATH: '/notebooks/ipynb',
  GRAPHQL_ENDPOINT: '/graphql'
};

module.exports = SpotConstants;
