//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
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
                if(response.data !== undefined && JSON.stringify(response.data).indexOf('"success":true') !== -1)
                  this.reloadElements();
            }
        })
        .fail((jqxhr, textStatus, error) => {
            console.error('Unexpected GraphQL error', jqxhr.responseJSON)
            this.setData({error: `${textStatus}: ${error}`})
        });
    }
}

module.exports = GraphQLStore;
