// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var SuspiciousGridMixin = require('../../../js/components/SuspiciousGridMixin.react.js');
var SpotUtils = require('../../../js/utils/SpotUtils.js');
var SuspiciousStore = require('../stores/SuspiciousStore');

var SuspiciousPanel = React.createClass({
    mixins: [GridPanelMixin, SuspiciousGridMixin],
    store: SuspiciousStore,
    getDefaultProps: function () {
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
    _renderIpCell: function (keyPrefix, repClassName, ip, isInternal) {
        const cssClassName = isInternal ? 'label label-info' : `text-${repClassName}`;

        return (
            <span key={keyPrefix} className={cssClassName}>{ip}</span>
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

        reps = SpotUtils.parseReputation(item.srcIP_rep);
        highestRep = SpotUtils.getHighestReputation(reps);

        const repClassName = SpotUtils.CSS_RISK_CLASSES[highestRep];
        return this._renderIpCell('src_' + idx, repClassName, item.srcIP, +item.srcIpInternal);
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

        reps = SpotUtils.parseReputation(item.dstIP_rep);
        highestRep = SpotUtils.getHighestReputation(reps);

        const repClassName = SpotUtils.CSS_RISK_CLASSES[highestRep];
        return this._renderIpCell('dst_' + idx, repClassName, item.dstIP, +item.destIpInternal);
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

        reps = SpotUtils.parseReputation(srcIP_rep);
        highestRep = SpotUtils.getHighestReputation(reps);

        content = this._renderInfoCell('src_info' + idx, +item.srcIpInternal, item.srcGeo, item.srcDomain, reps);

        return (
            <p key={'src_info_' + idx} className={'srcIP text-' + SpotUtils.CSS_RISK_CLASSES[highestRep]}>
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

        reps = SpotUtils.parseReputation(dstIP_rep);
        highestRep = SpotUtils.getHighestReputation(reps);

        content = this._renderInfoCell('dst_info' + idx, +item.dstIpInternal, item.dstGeo, item.dstDomain, reps);

        return (
            <p key={'dst_info_' + idx} className={'dstIP text-' + SpotUtils.CSS_RISK_CLASSES[highestRep]}>
                {content}
            </p>
        );
    },
    // Hidden cells
    _render_destIpInternal_cell: false,
    _render_dstDomain_cell: false,
    _render_dstGeo_cell: false,
    _render_flag_cell: false,
    _render_score_cell: false,
    _render_srcDomain_cell: false,
    _render_srcGeo_cell: false,
    _render_srcIpInternal_cell: false
});

module.exports = SuspiciousPanel;
