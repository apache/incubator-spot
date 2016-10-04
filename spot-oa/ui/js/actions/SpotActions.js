var $ = require('jquery');

var SpotDispatcher = require('../dispatchers/SpotDispatcher');
var SpotConstants = require('../constants/SpotConstants');
var SpotUtils = require('../utils/SpotUtils');

var SpotActions = {
    setDate: function (date, name) {
        var regex;

        name = name || 'date';
        SpotUtils.setUrlParam(name, date);

        // Update links to match date

        regex = new RegExp('\\${(?:[^}]+\\|)?' + name + '(?:\\|[^}]+)?}', 'g');
        $('a[data-href]').each(function () {
            var link = $(this);

            if (regex.test(link.data('href'))) {
                link.attr('href', link.data('href').replace(regex, date));
            }
        });

        SpotDispatcher.dispatch({
            actionType: SpotConstants.UPDATE_FILTER,
            filter: ''
        });

        SpotDispatcher.dispatch({
            actionType: SpotConstants.UPDATE_DATE,
            date: date,
            name: name
        });
    },
    expandPanel: function (panel) {
        SpotDispatcher.dispatch({
            actionType: SpotConstants.EXPAND_PANEL,
            panel: panel
        });
    },
    restorePanel: function (panel) {
        SpotDispatcher.dispatch({
            actionType: SpotConstants.RESTORE_PANEL,
            panel: panel
        });
    },
    toggleMode: function (panel, mode) {
        SpotDispatcher.dispatch({
            actionType: SpotConstants.TOGGLE_MODE_PANEL,
            panel: panel,
            mode: mode
        });
    }
};

module.exports = SpotActions;
