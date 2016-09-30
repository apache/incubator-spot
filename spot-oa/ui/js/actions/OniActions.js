var $ = require('jquery');

var OniDispatcher = require('../dispatchers/OniDispatcher');
var OniConstants = require('../constants/OniConstants');
var OniUtils = require('../utils/OniUtils');

var OniActions = {
    setDate: function (date, name) {
        var regex;

        name = name || 'date';
        OniUtils.setUrlParam(name, date);

        // Update links to match date

        regex = new RegExp('\\${(?:[^}]+\\|)?' + name + '(?:\\|[^}]+)?}', 'g');
        $('a[data-href]').each(function () {
            var link = $(this);

            if (regex.test(link.data('href'))) {
                link.attr('href', link.data('href').replace(regex, date));
            }
        });

        OniDispatcher.dispatch({
            actionType: OniConstants.UPDATE_FILTER,
            filter: ''
        });

        OniDispatcher.dispatch({
            actionType: OniConstants.UPDATE_DATE,
            date: date,
            name: name
        });
    },
    expandPanel: function (panel) {
        OniDispatcher.dispatch({
            actionType: OniConstants.EXPAND_PANEL,
            panel: panel
        });
    },
    restorePanel: function (panel) {
        OniDispatcher.dispatch({
            actionType: OniConstants.RESTORE_PANEL,
            panel: panel
        });
    },
    toggleMode: function (panel, mode) {
        OniDispatcher.dispatch({
            actionType: OniConstants.TOGGLE_MODE_PANEL,
            panel: panel,
            mode: mode
        });
    }
};

module.exports = OniActions;
