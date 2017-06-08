const EventEmitter = require('events').EventEmitter;

const GraphQLStore = require('./GraphQLStore');

const DATA_CHANGE_EVENT = 'data-change';

class ObervableGraphQLStore extends GraphQLStore {
    constructor() {
        super();
        this.eventEmitter = new EventEmitter();
    }

    addChangeDataListener(callback) {
        this.addListener(DATA_CHANGE_EVENT, callback);
    }

    removeChangeDataListener(callback) {
        this.removeListener(DATA_CHANGE_EVENT, callback);
    }

    addListener(eventName, callback) {
        this.eventEmitter.on(eventName, callback);
    }

    removeListener(eventName, callback) {
        this.eventEmitter.removeListener(eventName, callback);
    }

    notifyListeners(eventName) {
        this.eventEmitter.emit(eventName);
    }

    setData(data) {
        super.setData(data);
        this.notifyListeners(DATA_CHANGE_EVENT);
    }
}

module.exports = ObervableGraphQLStore;
