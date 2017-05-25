// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

let menu =
    {
       menu:
          [
             {name: 'Flows', link: '', glyphicon: '', labelledby: 'flowsMenu', sub:
                [
                   {name: 'Suspicious', link: '../flow/suspicious.html#date=${date}', target: '_self'},
                   {name: 'Threat Investigation', link: '../flow/threat-investigation.html#date=${date}', target: '_self'},
                   {name: 'Storyboard', link: '../flow/storyboard.html#date=${date}', target: '_self'},
                  //  {name: 'Advanced Mode', link: '../flow/ipython_notebook.html#date=${date}', target: '_blank'}
                ]
             },
             {name: 'DNS', link: '', glyphicon: '', labelledby: 'dnsMenu', sub:
                [
                  {name: 'Suspicious', link: '../dns/suspicious.html#date=${date}', target: '_self'},
                  {name: 'Threat Investigation', link: '../dns/threat-investigation.html#date=${date}', target: '_self'},
                  {name: 'Storyboard', link: '../dns/storyboard.html#date=${date}', target: '_self'},
                  // {name: 'Advanced Mode', link: '../dns/ipython_notebook.html#date=${date}', target: '_blank'}
                ]
              },
              {name: 'Proxy', link: '', glyphicon: '', labelledby: 'proxyMenu', sub:
                [
                  {name: 'Suspicious', link: '../proxy/suspicious.html#date=${date}', target: '_self'},
                  {name: 'Threat Investigation', link: '../proxy/threat-investigation.html#date=${date}', target: '_self'},
                  {name: 'Storyboard', link: '../proxy/storyboard.html#date=${date}', target: '_self'},
                  // {name: 'Advanced Mode', link: '../proxy/ipython_notebook.html#date=${date}', target: '_blank'}
                ]
              },
              {name: 'Ingest Summary', link: '../ingest-summary.html#end-date=${date}|pipeline=proxy', glyphicon: '', labelledby: '', sub: [], target: '_self'},
              {name: 'Plugins', link: '', glyphicon: '', labelledby: 'pluginsMenu', sub: []}

          ]
    };

module.exports = menu;
