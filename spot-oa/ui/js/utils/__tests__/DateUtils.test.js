const DateUtils = require('../DateUtils');

function d2s(date) {
    return date.toISOString().substr(0, 10);
}

test('Date calculation', () => {
    const today = new Date();
    let calculatedDate;

    // Go to yesterday
    const yesterday = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate()-1,
        today.getHours(),
        today.getMinutes(),
        today.getSeconds(),
        today.getMilliseconds()
    );
    calculatedDate = DateUtils.calcDate(today, -1, 'day');
    expect(d2s(calculatedDate)).toBe(d2s(yesterday));

    // Go to last Month
    const lastMonth = new Date(
        today.getFullYear(),
        today.getMonth()-1,
        today.getDate(),
        today.getHours(),
        today.getMinutes(),
        today.getSeconds(),
        today.getMilliseconds()
    );

    calculatedDate = DateUtils.calcDate(today, -1, 'month');
    expect(d2s(calculatedDate)).toBe(d2s(lastMonth));

    // Go to last Year
    const lastYear = new Date(
        today.getFullYear()-1,
        today.getMonth(),
        today.getDate(),
        today.getHours(),
        today.getMinutes(),
        today.getSeconds(),
        today.getMilliseconds()
    );

    calculatedDate = DateUtils.calcDate(today, -1, 'year');
    expect(d2s(calculatedDate)).toBe(d2s(lastYear));

    // Going to the future is forbiden, we should get today
    calculatedDate = DateUtils.calcDate(today, 1, 'day');
    expect(d2s(calculatedDate)).toBe(d2s(today));
    calculatedDate = DateUtils.calcDate(today, 1, 'month');
    expect(d2s(calculatedDate)).toBe(d2s(today));
    calculatedDate = DateUtils.calcDate(today, 1, 'year');
    expect(d2s(calculatedDate)).toBe(d2s(today));
});

test('Date formatting', () => {
    const today = new Date();

    expect(DateUtils.formatDate(today)).toBe(d2s(today));
});

test('Date parsing', () => {
    const today = d2s(new Date());

    const parsedDate = DateUtils.parseDate(today);
    expect(d2s(parsedDate)).toBe(today);
});
