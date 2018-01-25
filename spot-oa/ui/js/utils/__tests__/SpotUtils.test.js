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


const DateUtils = require('../DateUtils');
const SpotUtils = require('../SpotUtils');

test('Access to current date', () => {
    const today = new Date();

    expect(SpotUtils.getCurrentDate()).toBe(DateUtils.formatDate(today));
    const someDate = '1985-01-12';
    window.location.hash = 'date='+ someDate;
    expect(SpotUtils.getCurrentDate()).toBe(DateUtils.formatDate(DateUtils.parseDate(someDate)));
});

test('Multiple dates', () => {
    window.location.hash = 'start-date=1985-01-12|end-date=2016-11-29';

    expect(SpotUtils.getCurrentDate('start-date')).toBe('1985-01-12');
    expect(SpotUtils.getCurrentDate('end-date')).toBe('2016-11-29');
});

test('Date to String', () => {
    const today = new Date();

    expect(SpotUtils.getDateString(today)).toBe(DateUtils.formatDate(today));
});

test('Access to current filter', () => {
    const someFilter = '127.0.0.1';
    window.location.hash = 'date=1985-01-12|filter='+ someFilter;

    expect(SpotUtils.getCurrentFilter()).toBe(someFilter);
});

test('URL params', () => {
    window.location.hash = '';

    const someDate = '1985-01-12';
    const someFilter = '127.0.0.1';

    SpotUtils.setUrlParam('date', someDate);
    SpotUtils.setUrlParam('filter', someFilter);

    expect(SpotUtils.getUrlParam('date')).toBe(someDate);
    expect(SpotUtils.getUrlParam('filter')).toBe(someFilter);
});

test('Reputation data', () => {
    //{'3': 'danger', '2': 'warning', '1': 'info', '0': 'default', '-1': 'default'}
    let unknown = 'unknown:Unknown:-1:CatName1|GroupName1;CatName2|GroupName2';
    let unverify = 'unverify:Unverified:0';
    let minimal = 'minimal:Info:1';
    let warning = 'warning:Medium:2';
    let danger = 'danger:High:3';

    const rawRep = unknown + '::' + unverify + '::' + minimal + '::' + warning + '::' + danger;

    const rep = SpotUtils.parseReputation(rawRep);

    expect(rep).toBeInstanceOf(Object);

    expect(rep.unknown).toBeInstanceOf(Object);
    expect(rep.unknown.text).toBe('Unknown');
    expect(rep.unknown.value).toBe(-1);
    expect(rep.unknown.cssClass).toBe('default');
    expect(rep.unknown.categories).toBeInstanceOf(Array);
    expect(rep.unknown.categories.length).toBe(2);
    expect(rep.unknown.categories[0].name).toBe('CatName1');
    expect(rep.unknown.categories[0].group).toBe('GroupName1');
    expect(rep.unknown.categories[1].name).toBe('CatName2');
    expect(rep.unknown.categories[1].group).toBe('GroupName2');

    expect(rep.unverify).toBeInstanceOf(Object);
    expect(rep.unverify.text).toBe('Unverified');
    expect(rep.unverify.value).toBe(0);
    expect(rep.unverify.cssClass).toBe('default');
    expect(rep.unverify.categories).toBeNull();

    expect(rep.minimal).toBeInstanceOf(Object);
    expect(rep.minimal.text).toBe('Info');
    expect(rep.minimal.value).toBe(1);
    expect(rep.minimal.cssClass).toBe('info');
    expect(rep.minimal.categories).toBeNull();

    expect(rep.warning).toBeInstanceOf(Object);
    expect(rep.warning.text).toBe('Medium');
    expect(rep.warning.value).toBe(2);
    expect(rep.warning.cssClass).toBe('warning');
    expect(rep.warning.categories).toBeNull();

    expect(rep.danger).toBeInstanceOf(Object);
    expect(rep.danger.text).toBe('High');
    expect(rep.danger.value).toBe(3);
    expect(rep.danger.cssClass).toBe('danger');
    expect(rep.danger.categories).toBeNull();

    const highestRep = SpotUtils.getHighestReputation(rep);
    expect(highestRep).toBe(3);
});

test('Id encoding', () => {
    const rawId = 'apache.spot.incubating';

    const encodedId = SpotUtils.encodeId(rawId);
    const id = SpotUtils.decodeId(encodedId);

    expect(encodedId).not.toBe(rawId);
    expect(id).toBe(rawId);
});
