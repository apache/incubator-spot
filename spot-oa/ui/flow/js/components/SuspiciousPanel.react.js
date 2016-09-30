var React = require('react');

var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var SuspiciousGridMixin = require('../../../js/components/SuspiciousGridMixin.react.js');
var OniUtils = require('../../../js/utils/OniUtils.js');
var SuspiciousStore = require('../stores/SuspiciousStore');

var SuspiciousPanel = React.createClass({
    mixins: [GridPanelMixin, SuspiciousGridMixin],
    store: SuspiciousStore,
    getInitialState: function () {
        return {iterator: SuspiciousStore.ITERATOR};
    },
    _renderGeoCell: function (keyPrefix, geo, domain) {
        var toolTipContent;

        toolTipContent = '<p>Geo location: <strong>' + geo + '</strong></p>';
        toolTipContent += '<p>Domain: <strong>' + domain + '</strong></p>';

        return (
            <span
                key={keyPrefix + '_geo'} className="fa fa-lg fa-globe"
                data-container="body" data-toggle="popover" data-placement="right" data-content={toolTipContent}>
      </span>
        );
    },
    _renderIpCell: function (keyPrefix, ip, isInternal) {
        var internalCssCls;

        internalCssCls = isInternal ? 'label label-primary' : '';

        return (
            <span key={keyPrefix + '_label'} className={internalCssCls}>{ip} </span>
        );
    },
    /**
     *  Renders the source IP cell.
     *
     *  Renders the IP text along with a shild representing the reputation of that IP
     *
     *  @param  srcIP {String}  The source IP
     *  @param  item  {Object}  The current item being rendered.
     *  @param  idx   {Number}  The item index in the parent array
     *
     *  @return React Component
     **/
    _render_srcIP_cell: function (srcIp, item, idx) {
        var reps, highestRep, srcIpContent;

        reps = OniUtils.parseReputation(item.srcIP_rep);
        highestRep = OniUtils.getHighestReputation(reps);

        srcIpContent = this._renderIpCell('src_' + idx, item.srcIP, +item.srcIpInternal);

        return (
            <p key={'srcIP_' + idx} className={'srcIP text-' + OniUtils.CSS_RISK_CLASSES[highestRep]}>
                {srcIpContent}
            </p>
        );
    },
    /**
     *  Renders the destination IP cell.
     *
     *  @param  srcIP {String}  The destination IP
     *  @param  item  {Object}  The current item being rendered.
     *  @param  idx   {Number}  The item index in the parent array
     *
     *  @return React Component
     **/
    _render_dstIP_cell: function (dstIp, item, idx) {
        var reps, highestRep, dstIpContent;

        reps = OniUtils.parseReputation(item.dstIP_rep);
        highestRep = OniUtils.getHighestReputation(reps);

        dstIpContent = this._renderIpCell('dst_' + idx, item.dstIP, +item.destIpInternal);

        return (
            <p key={'dstIP_' + idx} className={'srcIP text-' + OniUtils.CSS_RISK_CLASSES[highestRep]}>
                {dstIpContent}
            </p>
        );
    },
    _renderInfoCell: function (keyPrefix, isInternal, geo, domain, reps) {
        if (isInternal) return [];

        return [
            this._renderRepCell(keyPrefix, reps),
            this._renderGeoCell(keyPrefix, geo, domain)
        ];
    },
    /**
     *  Renders the source IP info cell.
     *
     *  Renders the additional info about the source IP
     *
     *  @param  srcIP_rep   {String}  The source IP reputation
     *  @param  item        {Object}  The current item being rendered.
     *  @param  idx         {Number}  The item index in the parent array
     *
     *  @return React Component
     **/
    _render_srcIP_rep_cell: function (srcIP_rep, item, idx) {
        var reps, highestRep, content;

        reps = OniUtils.parseReputation(srcIP_rep);
        highestRep = OniUtils.getHighestReputation(reps);

        content = this._renderInfoCell('src_info' + idx, +item.srcIpInternal, item.srcGeo, item.srcDomain, reps);

        return (
            <p key={'src_info_' + idx} className={'srcIP text-' + OniUtils.CSS_RISK_CLASSES[highestRep]}>
                {content}
            </p>
        );
    },
    /**
     *  Renders the destination IP info cell.
     *
     *  Renders the additional info about the destination IP
     *
     *  @param  dstIP_rep   {String}  The destination IP reputation
     *  @param  item        {Object}  The current item being rendered.
     *  @param  idx         {Number}  The item index in the parent array
     *
     *  @return React Component
     **/
    _render_dstIP_rep_cell: function (dstIP_rep, item, idx) {
        var reps, highestRep, content;

        reps = OniUtils.parseReputation(dstIP_rep);
        highestRep = OniUtils.getHighestReputation(reps);

        content = this._renderInfoCell('dst_info' + idx, +item.dstIpInternal, item.dstGeo, item.dstDomain, reps);

        return (
            <p key={'dst_info_' + idx} className={'dstIP text-' + OniUtils.CSS_RISK_CLASSES[highestRep]}>
                {content}
            </p>
        );
    },
    // Hidden cells
    _render_destIpInternal_cell: false,
    _render_dstDomain_cell: false,
    _render_dstGeo_cell: false,
    _render_flag_cell: false,
    _render_lda_score_cell: false,
    _render_srcDomain_cell: false,
    _render_srcGeo_cell: false,
    _render_srcIpInternal_cell: false,
    _render_sev_cell: false
});

module.exports = SuspiciousPanel;
