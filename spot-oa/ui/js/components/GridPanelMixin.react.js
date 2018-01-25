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

var GridPanelMixin = {
  selectItems: function (ids)
  {
    ids = ids instanceof Array ? ids : [ids];

    this.setState({selectedRows: ids});
  },
  render: function ()
  {
    var state, content, gridHeaders, gridBody, iterator, selectedRows, key, eventHandlers;

    state = this.state || {};

    if (state.error)
    {
      content = (
        <div className="text-center text-danger">
          {state.error}
        </div>
      );
    }
    else if (state.loading)
    {
      content = (
        <div className="spot-loader">
            Loading <span className="spinner"></span>
        </div>
      );
    }
    else if (!state.data || state.data.length===0)
    {
        content = (
            <div className="text-center">
                {this.emptySetMessage || ''}
            </div>
        );
    }
    else
    {
      iterator = this.props.iterator || Object.keys(state.headers);

      selectedRows = state.selectedRows || [];

      gridHeaders = [];
      iterator.forEach(key => {
        // If a cell renderer is false, we should skip that cell
        if (this['_render_' + key + '_cell']===false) return;

        gridHeaders.push(
          <th key={'th_' + key} className={'text-center ' + key}>{state.headers[key]}</th>
        );
      });

      gridBody = [];
      state.data.forEach((item, index) => {
        var key, cells, className, cellRenderer, cellContent;

        cells = [];
        iterator.forEach(key => {
          cellRenderer = '_render_' + key + '_cell';

          if (!this[cellRenderer])
          {
            // If a cell renderer is false, we should skip that cell
            if (this[cellRenderer]===false) return;

            cellRenderer = '_renderCell';
          }

          cellContent = this[cellRenderer](item[key], item, index);

          cells.push(
            <td key={'td_' + index + '_' + key} className={'text-center ' + key}>
              {cellContent}
            </td>
          );
        });

        className = selectedRows.indexOf(item) >= 0 ? 'bg-warning' : null;

        eventHandlers = {};

        // Bind event handlers if present
        this._onClickRow && (eventHandlers.onClick = function (){ this._onClickRow(item); }.bind(this));
        this._onMouseEnterRow && (eventHandlers.onMouseEnter = function () { this._onMouseEnterRow(item); }.bind(this));
        this._onMouseLeaveRow && (eventHandlers.onMouseLeave = function () { this._onMouseLeaveRow(item); }.bind(this));

        gridBody.push(
          <tr key={'tr_' + index} className={className} {...eventHandlers}>{cells}</tr>
        );
      });

      content = (
        <table className="table table-intel table-intel-striped table-hover" style={{fontSize: 'small'}}>
          <thead>
            <tr>
              {gridHeaders}
            </tr>
          </thead>
          <tbody>
            {gridBody}
          </tbody>
        </table>
      );
    }

    return (
      <div className="spot-grid-panel col-md-12">
        {content}
      </div>
    );
  },
  _renderCell: function (value)
  {
    return value;
  }
};

module.exports = GridPanelMixin;
