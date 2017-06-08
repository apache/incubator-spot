const $ = require('jquery');
const SpotConstants = require('../constants/SpotConstants');

class GraphQLStore {
    constructor() {
        this.variables = {};
        this.data = {};
    }

    getQuery() {
        return null;
    }

    setVariable(name, value) {
        this.variables[name] = value;
    }

    getVariable(name) {
        return this.variables[name];
    }

    unsetVariable(name) {
        delete this.variables[name];
    }

    setData(data) {
        this.data = data;
    }

    getData() {
        if (Object.keys(this.data).length==0 || this.data.loading || this.data.error) return this.data;

        return {loading: false, data: this.unboxData(this.data)};
    }

    resetData() {
        this.setData({});
    }

    sendQuery() {
        const query = this.getQuery();
        const variables = this.variables;

        this.setData({loading: true});
        $.post({
            accept: 'application/json',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({
                query,
                variables
            }),
            url: SpotConstants.GRAPHQL_ENDPOINT
        })
        .done((response) => {
            if (response.errors) {
                console.error('Unexpected GraphQL error', response)
                this.setData({error: 'Oops... something went wrong'});
            }
            else {
                this.setData(response.data);
            }
        })
        .fail((jqxhr, textStatus, error) => {
            console.error('Unexpected GraphQL error', jqxhr.responseJSON)
            this.setData({error: `${textStatus}: ${error}`})
        });
    }
}

module.exports = GraphQLStore;
