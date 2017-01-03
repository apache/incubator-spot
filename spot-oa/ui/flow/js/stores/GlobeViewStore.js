const assign = require('object-assign');
const d3 = require('d3');

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const NetflowConstants = require('../constants/NetflowConstants');
const SpotConstants = require('../../../js/constants/SpotConstants');
const JsonStore = require('../../../js/stores/JsonStore');

const IP_FILTER_NAME = 'ip';
let WORLD_DATA = null;

const GlobeViewStore = assign(new JsonStore(NetflowConstants.API_GLOBE_VIEW), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(NetflowConstants.API_GLOBE_VIEW.replace('${date}', date.replace(/-/g, '')));
    },
    setIp: function (value)
    {
        this.setRestFilter(IP_FILTER_NAME, value);
    },
    getIp: function ()
    {
        return this.getRestFilter(IP_FILTER_NAME);
    },
    setData: function (data)
    {
        this._data = data;

        this.emitChangeData();
    },
    getWorldData() {
        return WORLD_DATA;
    },
    reload() {
        if (WORLD_DATA instanceof Object) {
            Object.getPrototypeOf(GlobeViewStore).reload.call(this);
        }
        else if (WORLD_DATA===true) {
            // Do nothing, already loading world data
        }
        else {
            WORLD_DATA = true; // Signal world data is loading
            d3.json(NetflowConstants.API_WORLD_110M, (error, response) => {
                WORLD_DATA = response;
                Object.getPrototypeOf(GlobeViewStore).reload.call(this);
            });
        }
    }
});

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            GlobeViewStore.setDate(action.date);

            break;
        case SpotConstants.RELOAD_COMMENTS:
            GlobeViewStore.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            GlobeViewStore.setIp(action.comment.ip);
            GlobeViewStore.reload();

            break;
    }
});

module.exports = GlobeViewStore;
