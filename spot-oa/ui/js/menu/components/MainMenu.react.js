// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

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
