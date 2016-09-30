var React = require('react');

var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var SuspiciousMixin = require('../../../js/components/SuspiciousGridMixin.react.js');
var OniUtils = require('../../../js/utils/OniUtils.js');
var SuspiciousStore = require('../stores/SuspiciousStore');

var SuspiciousPanel = React.createClass({
    mixins: [GridPanelMixin, SuspiciousMixin],
    store: SuspiciousStore,
    getInitialState: function () {
        return {iterator: SuspiciousStore.ITERATOR};
    },
    _renderCatCell: function (keyPrefix, reps) {
        var keys, services, tooltipContent;

        keys = Object.keys(reps);
        if (keys.length===0) return '';

        services = keys.map((serviceName, idx) => {
            var service, categories;

            service = reps[serviceName];
            if (!service.categories) return;

            categories = service.categories.map((category, catIdx) => [
                <dt key={keyPrefix + '_list_srv' + idx + '_cat' + catIdx} className="text-uppercase">
                    <strong>{category.name}</strong>
                </dt>,
                <dd key={keyPrefix + '_list_srv' + idx + '_group' + catIdx}>
                    {category.group}
                </dd>
            ]);

            return (
                <li key={keyPrefix + '_list_srv' + idx + '_name'}>
                    <span className={'label label-' + OniUtils.CSS_RISK_CLASSES[service.value] + ' text-uppercase'}>{serviceName}</span>
                    <dl>
                        {categories}
                    </dl>
                </li>
            );
        }).filter(service => !!service);

        if (services.length===0) return '';

        tooltipContent = React.renderToStaticMarkup(
            <div key={keyPrefix + '_list'}>
                <ul className="list-unstyled">
                    {services}
                </ul>
            </div>
        );

        return (
            <span key={keyPrefix + '_icon'} className="glyphicon glyphicon-list" data-container="body" data-toggle="popover"
               data-placement="right" data-content={tooltipContent}>
            </span>
        );
    },
    _render_p_date_cell: function (date, item) {
        return date + ' ' + item['p_time'];
    },
    _render_host_cell: function (host, item, idx) {
        var reps, highestRep;

        reps = OniUtils.parseReputation(item.uri_rep);
        highestRep = OniUtils.getHighestReputation(reps);

        return (
            <p key={'host_' + idx} className={'oni-text-wrapper text-' + OniUtils.CSS_RISK_CLASSES[highestRep]} data-toggle="tooltip">
                {host}
            </p>
        );
    },
    _render_uri_rep_cell: function (uri_rep, item, idx) {
        var reps, highestRep, uriRep, uriCat;

        reps = OniUtils.parseReputation(uri_rep);
        highestRep = OniUtils.getHighestReputation(reps);

        uriRep = this._renderRepCell('host_rep_' + idx, reps);
        uriCat = this._renderCatCell('host_cat_' + idx, reps);

        return (
            <p key={'uri_info_' + idx} className={'uri_info text-' + OniUtils.CSS_RISK_CLASSES[highestRep]}>
                {uriRep} {uriCat}
            </p>
        );
    },
    _render_webcat_cell: function (webcat) {
        var categories;

        if (webcat.indexOf(';')<0) return webcat;

        return (
            <ol className="text-left">
                {webcat.split(';').map((cat,idx) => <li key={'webcat' + idx}>{cat}</li>)}
            </ol>
        );
    },
// Hidden cells
    _render_p_time_cell: false,
    _render_reqmethod_cell: false,
    _render_useragent_cell: false,
    _render_resconttype_cell: false,
    _render_duration_cell: false,
    _render_username_cell: false,
    _render_referer_cell: false,
    _render_respcode_cell: false,
    _render_uriport_cell: false,
    _render_uripath_cell: false,
    _render_uriquery_cell: false,
    _render_serverip_cell: false,
    _render_scbytes_cell: false,
    _render_csbytes_cell: false,
    _render_fulluri_cell: false,
    _render_uri_sev_cell: false,
    _render_hash_cell: false,
    _render_subdomainentropy_cell: false,
    _render_top_domain_cell: false,
    _render_word_cell: false,
    _render_score_cell: false,
    _render_network_context_cell: false
});

module.exports = SuspiciousPanel;
