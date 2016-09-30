var React = require('react');

var GridPanelMixin = {
  selectItems: function (ids)
  {
    ids = ids instanceof Array ? ids : [ids];

    this.setState({selectedRows: ids});
  },
  render: function ()
  {
    var content, gridHeaders, gridBody, iterator, selectedRows, key, eventHandlers;

    if (this.state.error)
    {
      content = (
        <div className="text-center text-danger">
          {this.state.error}
        </div>
      );
    }
    else if (this.state.loading)
    {
      content = (
        <div className="oni_loader">
            Loading <span className="spinner"></span>
        </div>
      );
    }
    else if (this.state.data.length===0)
    {
        content = (
            <div className="text-center">
                {this.emptySetMessage || ''}
            </div>
        );
    }
    else
    {
      iterator = this.state.iterator || Object.keys(this.state.headers);

      selectedRows = this.state.selectedRows || [];

      gridHeaders = [];
      iterator.forEach(key => {
        // If a cell renderer is false, we should skip that cell
        if (this['_render_' + key + '_cell']===false) return;

        gridHeaders.push(
          <th key={'th_' + key} className={'text-center ' + key}>{this.state.headers[key]}</th>
        );
      });

      gridBody = [];
      this.state.data.forEach((item, index) => {
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
      <div className="oni-grid-panel col-md-12">
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
