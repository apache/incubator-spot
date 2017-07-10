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
var ScoreMessage = require('../../../js/components/scoring/ScoreMessage.react');
var RatingInput = require('../../../js/components/scoring/RatingInput.react');
var SearchGlobalInput = require('../../../js/components/scoring/SearchGlobalInput.react');

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
    var content, state, data, cssCls, srcIpArr = [], dstIpArr = [], srcPortArr = [], dstPortArr = [];
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
        if(srcIpArr.indexOf(obj.srcIP) === -1) {
          srcIpArr.push(obj.srcIP);
        }
        if(dstIpArr.indexOf(obj.dstIP) === -1) {
          dstIpArr.push(obj.dstIP);
        }
        if(srcPortArr.indexOf(obj.sport) === -1) {
          srcPortArr.push(obj.sport);
        }
        if(dstPortArr.indexOf(obj.dport) === -1) {
          dstPortArr.push(obj.dport);
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
              <SearchGlobalInput col="6"/>
              <RatingInput data={data} col="6"/>
            </div>
            <div className="margin-up-down">
              <ButtonsScoring name="Score" action="score" onChange={this.score.bind(this)} col="3"/>
              <ButtonsScoring name="Save" action="save" onChange={this.save.bind(this)} col="3"/>
              <ButtonsScoring name="Reset Scoring" action="reset" onChange={this.reset.bind(this)} col="3"/>
            </div>
            <div className="margin-up-down">
              <FilterSelectInput nameBox="Source Ip" idSelect="#srcIp" idInput="srcIpIn" col="3"/>
              <FilterSelectInput nameBox="Dest IP" idSelect="#dstIp" idInput="dstIpIn" col="3"/>
              <FilterSelectInput nameBox="Src Port" idSelect="#srcPort" idInput="srcPortIn" col="3"/>
              <FilterSelectInput nameBox="Dst Port" idSelect="#dstPort" idInput="dstPortIn" col="3"/>
            </div>
            <div className="margin-up-down">
              <SelectInput title="Source IP" who="srcIp" options={srcIpArr} col="3"/>
              <SelectInput title="Dest IP" who="dstIp" options={dstIpArr} col="3"/>
              <SelectInput title="Src Port" who="srcPort" options={srcPortArr} col="3"/>
              <SelectInput title="Dst Port" who="dstPort" options={dstPortArr} col="3"/>
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
      swal('Warning.','You need to score at least 1 threat.','warning');
    } else {
      this.state.scoredEmelents.map((row) => {
        variables.push({
          'date': SpotUtils.getCurrentDate(),
          'score': row[4],
          'srcIp':  row[0] || null,
          'dstIp': row[1] || null,
          'srcPort': row[2] || null,
          'dstPort': row[3] || null
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
    let srcIp   = document.getElementById('srcIp').value;
    let dstIp   = document.getElementById('dstIp').value;
    let srcPort = document.getElementById('srcPort').value;
    let dstPort = document.getElementById('dstPort').value;
    let rating  = $('input[name="optradio"]:checked').val();

    //callback from the father
    if(quickIpScoring !== '') {
      dataScored.push([quickIpScoring, dstIp, srcPort, dstPort, rating]);
      dataScored.push([srcIp, quickIpScoring, srcPort, dstPort, rating]);
    } else {
      dataScored.push([srcIp, dstIp, srcPort, dstPort, rating]);
    }

    $('#scoreMsg').removeClass("hidden");
    this.removeSelected([quickIpScoring, srcIp, dstIp, srcPort, dstPort]);
    this.setState({scoredEmelents: dataScored});
  },
  removeSelected: function(data) {
    //when an user score, all elements selected need to be removed.
    data.map((element) => data.map((e) => e !== '' ? $(`option[value="${e}"]`).remove() : ''));
    $(".select-picker, #globalTxt").val('');
  }

});


module.exports = ScoreNotebook;
