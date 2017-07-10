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

var React = require('react');

var MainMenuStore = require('../stores/MainMenuStore');

var MainMenu = React.createClass({
  getInitialState: function () {
    return {
      menu: MainMenuStore.getMenu()
    };
  },
  componentDidMount: function () {
    MainMenuStore.addChangeMenuListener(this.onChange);
  },
  componentWillUnmount: function () {
    MainMenuStore.removeChangeMenuListener(this.onChange);
  },
  render: function () {
    let createdMenu = this.state.menu.map((dropdwn) =>
      <li className="dropdown">
          {(dropdwn.sub.length === 0 && dropdwn.labelledby === 'pluginsMenu') ? '' : dropdwn.sub.length === 0 ?
            <a data-href={dropdwn.link} target={dropdwn.target}>{dropdwn.name}</a>
            :
            <a className="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                {dropdwn.glyphicon !== '' ? <span className={'glyphicon ' + dropdwn.glyphicon} aria-hidden="true"></span>
                :
                dropdwn.name}&nbsp;
                <span className="caret"></span>
            </a>
          }
          <ul className="dropdown-menu" aria-labelledby={dropdwn.labelledby}>
            {dropdwn.sub.map((li) =>
              <li>
                  <a data-href={li.link} target={li.target}>{li.name}</a>
              </li>
            )}
          </ul>
      </li>
    );

    return(
      <div>
        <ul className="nav navbar-nav navbar-right">
          {createdMenu}
        </ul>
      </div>
    );
  },
  onChange: function ()
  {
    this.setState({menu: MainMenuStore.getMenu()});
  }

});


module.exports = MainMenu;
