const ObservableGraphQLStore = require('./ObservableGraphQLStore');

class ObservableWithHeadersGraphQLStore extends ObservableGraphQLStore {
    getData() {
        const data = super.getData();

        if (data.loading || data.error) return data;

        data.headers = this.headers;

        return data;
    }
}

module.exports = ObservableWithHeadersGraphQLStore
