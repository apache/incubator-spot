// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License


const CategoryLayout = require('../CategoryLayout');

test('Default layout properties', () => {
    const instance = new CategoryLayout();

    const size = instance.size();
    expect(size[0]).toBe(1);
    expect(size[1]).toBe(1);

    const obj1 = {id: 'key1'}, obj2 = {id: 'key2'};
    // Expect links to be always the same direction not matter how linked nodes are being provided
    var link = instance.link()(obj1, obj2);
    expect(link.source).toBe(obj2);
    expect(link.target).toBe(obj1);
    link = instance.link()(obj2, obj1);
    expect(link.source).toBe(obj2);
    expect(link.target).toBe(obj1);

    const catAccessor = instance.category();
    expect(catAccessor({category: 'foo'})).toBe('foo');

    const categories = ['foo', 'bar'];
    const categoryGenerator = () => ['bar', 'foo'];

    instance.categories(categories);
    expect(instance.categories()[0]).toBe('foo');
    expect(instance.categories()[1]).toBe('bar');

    instance.categories(categoryGenerator);
    expect(instance.categories()[0]).toBe('bar');
    expect(instance.categories()[1]).toBe('foo');

    const idAccessor = (obj) => obj.id2;

    const idObj = {id: 'id', id2: 'id2'};
    expect(instance.id()(idObj)).toBe('id');

    instance.id(idAccessor);
    expect(instance.id()(idObj)).toBe('id2');
});

const node4 = {id: 'node4'};
const node3 = {
    id: 'node3',
    category: 'Cat3',
    links: [node4]
};
const node2_1 = {
    id: 'node2.1',
    category: 'Cat2',
    links: [node3]
};
const node2_2 = {
    id: 'node2.2',
    category: 'Cat2',
    links: [node3]
};
const node1 = {
    id: 'node1',
    category: 'Cat1',
    links: [node2_1, node2_2]
};
const rawNodes = [node1, node2_1, node2_2, node3];

test('Node initialization', () => {
    const instance = new CategoryLayout();

    const nodes = instance.nodes(rawNodes);

    // Same for nodes have been returned
    expect(nodes.length).toBe(4);

    // Nodes have x and y properties
    nodes.forEach(node => expect(node.x).toBeDefined());
    nodes.forEach(node => expect(node.y).toBeDefined());

    // Same category nodes share x value but have different y values
    expect(nodes[1].x).toBe(nodes[2].x);
    expect(nodes[1].y).not.toBe(nodes[2].y);

    // Nodes are distributed along x axis
    expect(nodes[0].x).not.toBe(nodes[1].x);
    expect(nodes[1].x).not.toBe(nodes[3].x);
    expect(nodes[3].x).not.toBe(nodes[0].x);
});

test('Links', () => {
    const instance = new CategoryLayout();

    const nodes = instance.nodes(rawNodes);
    const links = instance.links(nodes);

    expect(links.length).toBe(4);
});
