// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

$(function () {
    $('body').tooltip({
        selector: '.spot-text-wrapper[data-toggle]',
        container: 'body',
        html: true,
        template: '<div class="spot-tooltip tooltip" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>',
        title: function () {
            return $(this).html();
        }
    });
});

$('body').on('show.bs.tooltip', '.spot-text-wrapper', function () {
    return this.clientWidth !== this.scrollWidth || this.clientHeight !== this.scrollHeight;
});
