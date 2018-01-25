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

let menu =
    {
       menu:
          [
             {name: 'Flows', link: '', glyphicon: '', labelledby: 'flowsMenu', sub:
                [
                   {name: 'Suspicious', link: '../flow/suspicious.html#date=${date}', target: '_self'},
                   {name: 'Threat Investigation', link: '../flow/threat-investigation.html#date=${date}', target: '_self'},
                   {name: 'Storyboard', link: '../flow/storyboard.html#date=${date}', target: '_self'},
                   {name: 'Advanced Mode', link: '../flow/ipython_notebook.html#date=${date}', target: '_blank'}
                ]
             },
             {name: 'DNS', link: '', glyphicon: '', labelledby: 'dnsMenu', sub:
                [
                  {name: 'Suspicious', link: '../dns/suspicious.html#date=${date}', target: '_self'},
                  {name: 'Threat Investigation', link: '../dns/threat-investigation.html#date=${date}', target: '_self'},
                  {name: 'Storyboard', link: '../dns/storyboard.html#date=${date}', target: '_self'},
                  {name: 'Advanced Mode', link: '../dns/ipython_notebook.html#date=${date}', target: '_blank'}
                ]
              },
              {name: 'Proxy', link: '', glyphicon: '', labelledby: 'proxyMenu', sub:
                [
                  {name: 'Suspicious', link: '../proxy/suspicious.html#date=${date}', target: '_self'},
                  {name: 'Threat Investigation', link: '../proxy/threat-investigation.html#date=${date}', target: '_self'},
                  {name: 'Storyboard', link: '../proxy/storyboard.html#date=${date}', target: '_self'},
                  {name: 'Advanced Mode', link: '../proxy/ipython_notebook.html#date=${date}', target: '_blank'}
                ]
              },
              {name: 'Ingest Summary', link: '../ingest/ingest-summary.html#end-date=${date}', glyphicon: '', labelledby: '', sub: [], target: '_self'},
              {name: 'Plugins', link: '', glyphicon: '', labelledby: 'pluginsMenu', sub: []}

          ]
    };

module.exports = menu;
