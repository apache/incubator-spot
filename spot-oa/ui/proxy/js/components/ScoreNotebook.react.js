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

var SuspiciousStore = require('../stores/SuspiciousStore');
var NotebookStore = require('../stores/NotebookStore');
var EdInActions = require('../../../js/actions/EdInActions');
var SelectInput = require('../../../js/components/scoring/SelectInput.react');
var FilterSelectInput = require('../../../js/components/scoring/FilterSelectInput.react');
var ButtonsScoring = require('../../../js/components/scoring/ButtonsScoring.react');
var RatingInput = require('../../../js/components/scoring/RatingInput.react');
var SearchGlobalInput = require('../../../js/components/scoring/SearchGlobalInput.react');
var ScoreMessage = require('../../../js/components/scoring/ScoreMessage.react');

var SpotUtils = require('../../../js/utils/SpotUtils');

var ScoreNotebook = React.createClass({
  // mixins: [GridPanelMixin],
  emptySetMessage: 'There is no data available for selected date.',
  propTypes: {
    date: React.PropTypes.string.isRequired,
  },
  getInitialState: function () {
    return {
      scoredEmelents: []
    };
  },
  componentDidMount: function() {
    SuspiciousStore.addChangeDataListener(this._onChange);
    NotebookStore.addChangeDataListener(this._onChange);
    this.setState({size: NotebookStore.completeClass})
  },
  componentWillUnmount: function () {
    SuspiciousStore.removeChangeDataListener(this._onChange);
    NotebookStore.addChangeDataListener(this._onChange);
  },
  render: function () {
    var content, state, data, cssCls, uriArr = [];
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
    else if (!state.data || state.data.length === 0)
    {
      content = (
        <div className="text-center">
          {this.emptySetMessage || ''}
        </div>
      );
    }
    else
    {
      state.data.map((obj) => {
        if(uriArr.indexOf(obj.fulluri) === -1) {
          uriArr.push(obj.fulluri);
        }
      });

      data = [
              {value: 1, name: 'High', radioName: 'optradio', selected: true},
              {value: 2, name: 'Medium', radioName: 'optradio', selected: false},
              {value: 3, name: 'Low', radioName: 'optradio', selected: false}
            ];

      content = (
        <div>
            <div className="margin-up-down">
              <SearchGlobalInput col="6" maxlength="255"/>
              <RatingInput data={data} col="6"/>
            </div>
            <div className="margin-up-down">
              <ButtonsScoring name="Score" action="score" onChange={this.score.bind(this)} col="3"/>
              <ButtonsScoring name="Save" action="save" onChange={this.save.bind(this)} col="3"/>
              <ButtonsScoring name="Reset Scoring" action="reset" onChange={this.reset.bind(this)} col="3"/>
            </div>
            <div className="margin-up-down">
              <FilterSelectInput nameBox="URI..." idSelect="#fullUri" idInput="fullUriIn" col="9"/>
            </div>
            <div className="margin-up-down">
              <SelectInput title="Source IP" who="fullUri" options={uriArr} col="9"/>
            </div>
            <div className="margin-up-down">
              <ScoreMessage who="scoreMsg"/>
            </div>
          </div>
        );
    }
    cssCls = this.state.size ? 'col-md-6 col-lg-6 col-xs-12' : 'col-md-offset-3 col-lg-offset-3 col-md-6 col-lg-6 col-xs-12';

    return(
      <div className={cssCls + ' spot-frame'}>
        {content}
      </div>
    )
  },
  _onChange: function() {
    const data = SuspiciousStore.getData();
    this.setState(data);
  },
  reset: function() {
    swal({
      title: 'Are you sure?',
      text: "You won't be able to revert this!",
      type: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, reset all!'
    }).then(() => {
      EdInActions.resetScoring(SpotUtils.getCurrentDate());
      swal({
        title: 'Done!',
        text: "All scores have been reset.",
        type: 'success',
        showCancelButton: false,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Ok!'
      }).then(() => {
        this.setState({loading: true});
      });
    });
  },
  save: function() {
    let variables = [];

    if (this.state.scoredEmelents.length === 0) {
      swal('Warning.','You should score at least 1 threat.','warning');
    } else {
      this.state.scoredEmelents.map((row) => {
        variables.push({
          'date': SpotUtils.getCurrentDate(),
          'uri':  row[0] || "None",
          'score': row[1],
        });
      });

      EdInActions.saveScoring(variables);
      $('#scoreMsg').addClass("hidden");
      this.setState({scoredEmelents: [], loading: true});
    }
  },
  score: function() {
    //this should be changed to take all data at the time, time needed.
    let dataScored = this.state.scoredEmelents || [];
    let quickIpScoring = document.getElementById('globalTxt').value;
    let fullUri = document.getElementById('fullUri').value;
    let rating  = $('input[name="optradio"]:checked').val();

    //callback from the father
    if(quickIpScoring !== '') {
      dataScored.push([quickIpScoring, rating]);
    } else {
      dataScored.push([fullUri, rating]);
    }

    this.removeSelected([fullUri, quickIpScoring]);
    $('#scoreMsg').removeClass("hidden");
    this.setState({scoredEmelents: dataScored});
  },
  removeSelected: function(data) {
    //when an user score, all elements selected need to be removed.
    data.map((element) => data.map((e) => e !== '' ? $(`option[value="${e}"]`).remove() : ''));
    $(".select-picker, #globalTxt").val('');
  }

});


module.exports = ScoreNotebook;
