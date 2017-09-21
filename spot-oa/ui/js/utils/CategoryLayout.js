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

class CategoryLayout {
    constructor() {
        this._size = [1,1];
        this._category = d => d.category;
        this._categories = null;
        this._id = d => d.id;
        this._link = (n1, n2) => {
            let source = n1.id < n2.id ? n2 : n1;
            let target = n1.id < n2.id ? n1 : n2;

            return {source, target};
        };
    }

    _layoutCategory(nodes, idx, categoryCount) {
        let size, xOffset, yOffset;

        size = this.size();

        xOffset = size[0]/categoryCount;
        xOffset = xOffset * (idx+.5);

        yOffset = size[1]/nodes.length;

        return nodes.map((node, idx) => {
            node.x = xOffset;
            node.y = yOffset * (idx + .5);

            return node;
        });
    }

    size(size) {
        return size ? (this._size=size, this) : this._size;
    }

    nodes(rawNodes) {
        let catKeys, categories = {};

        if (this._categories) {
            catKeys = this._categories;
        }
        else {
            catKeys = [];
        }

        // Add nodes to categories
        rawNodes.forEach(node => {
            let category = this._category(node);

            if (!(category in categories)) {
                catKeys.indexOf(category)<0 && catKeys.push(category);
                categories[category] = [];
            }

            categories[category].push(node);
        });

        let nodes = [], n=catKeys.length;
        catKeys.forEach((catName, idx) => {
            let category = categories[catName];

            if (!(category instanceof Array) || category.length==0) return;

            let catNodes = this._layoutCategory(category, idx++, n);

            nodes.push.apply(nodes, catNodes);
        });

        return nodes;
    }

    link(accessor) {
        return accessor ? (this._link=accessor, this) : this._link;
    }

    links(nodes) {
        let nodeIds = {}, links = {};

        nodes.forEach(n => nodeIds[n.id] = true);

        nodes.forEach(node => {
            for (let key in node.links) {
                let link = this._link(node, node.links[key]);

                if (!nodeIds[link.source.id] || !nodeIds[link.target.id]) continue;

                let linkId = link.source.id+link.target.id;

                links[linkId] = link;
            }
        });

        return Object.keys(links).map(key => links[key]);
    }

    category(accessor) {
        return accessor ? (this._category=accessor, this) : this._category;
    }

    categories(categories) {
        if (categories) {
            this._categories = categories instanceof Function ? categories() : categories instanceof Array ? categories : null;

            return this;
        }
        else {
            return this._categories;
        }
    }

    id(accessor) {
        return accessor ? (this._id=accessor, this) : this._id;
    }
}

module.exports = CategoryLayout;
