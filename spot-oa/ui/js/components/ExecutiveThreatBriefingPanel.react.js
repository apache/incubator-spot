var React = require('react');

var ContentLoaderMixin = require('./ContentLoaderMixin.react');
var StoryBoardActions = require('../actions/StoryboardActions');
var RestStore = require('../stores/RestStore');

var ExecutiveThreatBriefingPanel = React.createClass({
    mixins: [ContentLoaderMixin],
    propTypes: {
        store: React.PropTypes.instanceOf(RestStore).isRequired
    },
    getInitialState: function () {
        return {loading: true};
    },
    componentDidMount: function ()
    {
        this.props.store.addChangeDataListener(this._onChange);
    },
    componentWillUmount: function ()
    {
        this.props.store.removeChangeDataListener(this._onChange);
    },
    renderContent: function ()
    {
        var comments;

        comments = this.state.data.map(function (comment, idx) {
            return (
                <div key={'comment' + idx} className="oni-comment panel panel-default">
                    <div id={'comment' + idx + '_title'} className="panel-heading" role="tab">
                        <h4 className="panel-title text-center">
                            <a className="collapsed" role="button" data-toggle="collapse" data-parent="#oni-executive-threat-briefing" href={'#comment' + idx + '_summary'}
                               aria-expanded="false" aria-controls={'comment' + idx + '_summary'} onClick={() => {this._onSelect(comment)}}>
                                {comment.title}
                            </a>
                        </h4>
                    </div>
                    <div id={'comment' + idx + '_summary'} className="panel-collapse collapse" role="tabpanel"
                         aria-labelledby={'comment' + idx + '_title'}>
                        <div className="panel-body" dangerouslySetInnerHTML={{__html: (comment.summary || '').replace(/\\n/g, '<br />')}} />
                    </div>
                </div>
            );
        }.bind(this));

        return (
            <div id="oni-executive-threat-briefing" className="panel-group" role="tablist" aria-multiselectable="true">
                {comments}
            </div>
        );
    },
    _onChange: function ()
    {
        this.setState(this.props.store.getData());
    },
    _onSelect: function (comment)
    {
        StoryBoardActions.selectComment(comment);
    }
});

module.exports = ExecutiveThreatBriefingPanel;
