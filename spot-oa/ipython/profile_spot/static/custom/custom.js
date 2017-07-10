// #
// # Licensed to the Apache Software Foundation (ASF) under one or more
// # contributor license agreements.  See the NOTICE file distributed with
// # this work for additional information regarding copyright ownership.
// # The ASF licenses this file to You under the Apache License, Version 2.0
// # (the "License"); you may not use this file except in compliance with
// # the License.  You may obtain a copy of the License at
// #
// #    http://www.apache.org/licenses/LICENSE-2.0
// #
// # Unless required by applicable law or agreed to in writing, software
// # distributed under the License is distributed on an "AS IS" BASIS,
// # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// # See the License for the specific language governing permissions and
// # limitations under the License.
// #
/**
 * Trigger reload event on parent document
 */
function reloadParentData()
{
    window.parent.iframeReloadHook && window.parent.iframeReloadHook();
}

require(['jquery', 'bootstrap'], function($, bootstrap)
{
    function retry(query) {
        var regex = new RegExp(query + '=(\\d)');

        var matcher = regex.exec(window.location.hash);

        var attempt = matcher ? ++matcher[1] : 2;

        // Set hash
        window.location.hash = '#' + query + '=' + attempt;
        window.location.reload();
    }

    // jQuery must be available for easy mode to woark properly
    if ($===undefined) {
        if (/#spot_jquery_retry=[015-9]/.test(window.location.hash)) {
            alert('Notebook was not able to load jQuery after 5 attempts. Please try again later, this is a known issue.');
            console.warn('Bootstrap\'s tooltip was not loaded.');
        }
        else {
            confirm('Notebook failed to load jQuery, easy mode would not work properly without it. Would you you like to try again?')
            && retry('spot_jquery_retry');
        }

        return;
    }

    if ($.fn.tooltip===undefined) {
        if (/#spot_bootstrap_retry=\d/.test(window.location.hash)) {
            alert('Notebook was not able to load bootstrap\'s tooltip plugin. You can still work withuot this feature.');
        } else {
            confirm('Notebook was not able to load bootstrap\'s tooltip plugin. You can still work withuot this feature. Would you like to try again?')
            && retry('spot_bootstrap_retry');
        }
    }

    var easyMode = {
        stage: 0,
        KERNEL_READY: 1,
        NOTEBOOK_READY: 2,
        DOM_READY: 4,
        ENV_READY: 7,
        building: false,
        present: false,
        ready: false,
        cells: {
            total: 0,
            execution_queue: []
        }
    }

    /**
     *  Show EasyMode
     *
     *  Hide everything but IPython form
     */
    function showEasyMode()
    {
        $(document.body).addClass('spot_easy_mode').removeClass('spot_ninja_mode');
    }

    /**
     *  Hide Easy Mode
     *
     *  Show full IPython Notebook
     */
    function hideEasyMode()
    {
        $(document.body).addClass('spot_ninja_mode').removeClass('spot_easy_mode');
    }

    function insertProgressIndicator()
    {
        $(document.body).append(
            '<div id="spot_easy_mode_loader">' +
                '<span id="spot_easy_mode_loader_details"></span>' +
                '<span id="spot_easy_mode_loader_spinner"></span>' +
            '</div>'
        );
    }

    /**
     *  Remove progress indicator
     */
    function removeProgressIndicator()
    {
        $(document.body).removeClass('spot_easy_mode_loading');

        $('#spot_easy_mode_loader').remove();
    }

    /**
     *  Updates progress indicator's details
     */
    function updateProgressIndicator(content)
    {
        $('#spot_easy_mode_loader_details').html(content);
    }

    /**
     *  Show a progress indicator to users
     */
    function showBuildingUiIndicator()
    {
        $(document.body).addClass('spot_easy_mode_loading');

        insertProgressIndicator();

        updateProgressIndicator(
            'Building UI <span id="spot_easy_mode_loader_progress">0</span>%'
        );
    }

    /**
     *  Update progress indicator
     */
    function updateBuildingUiIndicator()
    {
        var p;

        p = (easyMode.cells.total-easyMode.cells.execution_queue.length) * 100 / easyMode.cells.total;

        $('#spot_easy_mode_loader_progress').text(Math.floor(p));
    }

    function easyModeBootStrap (IPython)
    {
        if (easyMode.stage!=easyMode.ENV_READY) return;

        // 1 Build widgets
        easyMode.building = true;

        console.info('Spot: Building easy mode...');

        // 2 Create an execution queue to display progress
        easyMode.cells.execution_queue = [];

        easyMode.cells.total = 0;
        IPython.notebook.get_cells().forEach(function (cell)
        {
            if (cell.cell_type==='code')
            {
                easyMode.cells.total++;
            }
        });

        // Add an extra cell to show progress when requesting execution
        easyMode.cells.total++;

        // 3 Execute all cells ( Generate UI )
        IPython.notebook.execute_all_cells();

        updateBuildingUiIndicator();
    }

    function isEasyModeAvailable()
    {
        return window.parent!=window;
    }

    function isEasyModeEnabled() {
        return /showEasyMode/.test(window.location.hash);
    }

    function isNinjaModeEnabled() {
        return /showNinjaMode/.test(window.location.hash);
    }

    if (isEasyModeAvailable()) {
        // Add spot CSS classes (easymode enabled by default)
        $(document.body).addClass('spot').addClass('spot_easy_mode').addClass('spot_easy_mode_loading');

        // Listen for URL's hash changes
        $(window).on('hashchange', function ()
        {
            if (isEasyModeEnabled()) showEasyMode();
            else if (isNinjaModeEnabled()) hideEasyMode();
        });

        $(function () {
            // Show progress indicator
            showBuildingUiIndicator();
        });
    }

    // Enable spot tooltip text wrapper
    $(function () {
        $('body').tooltip({
            selector: '.spot-text-wrapper[data-toggle]',
            container: 'body',
            template: '<div class="spot-tooltip tooltip" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>',
            title: function () {
                return $(this).html();
            },
            html: true
        });

        $('body').on('show.bs.tooltip', '.spot-text-wrapper', function () {
            return this.clientWidth !== this.scrollWidth || this.clientHeight !== this.scrollHeight;
        });
    });

    /**
     * The following code enables toggle from normal user mode (wizard) and ninja node (notebook UI)
     */
    require(['base/js/namespace', 'base/js/events'], function(IPython, events)
    {
        // Do nothing when running stand alone
        if (!isEasyModeAvailable()) return;

        // We are running inside and iframe from Spot. Let's have some fun!

        // Let Notebook be aware it is running on an iframe
        IPython._target = '_self';

        events.on('kernel_busy.Kernel', function ()
        {
            // Skip this event while building UI
            if (!easyMode.ready) return;

            $('#notebook button.btn:not([disabled])').addClass('spotDisabled').attr('disabled', 'disabled');

            insertProgressIndicator();
        });

        events.on('kernel_idle.Kernel', function ()
        {
            // Skip this event while building UI
            if (!easyMode.ready) return;

            removeProgressIndicator();

            $('#notebook button.btn.spotDisabled').removeClass('spotDisabled').removeAttr('disabled');
        });

        events.on('kernel_ready.Kernel', function ()
        {
            console.info('Spot: Kernel is ready');

            easyMode.stage |= easyMode.KERNEL_READY;

            easyModeBootStrap(IPython);
        });

        events.on('notebook_loaded.Notebook', function ()
        {
            console.info('Spot: Notebook loaded');

            easyMode.stage |= easyMode.NOTEBOOK_READY;

            easyModeBootStrap(IPython);
        });

        events.on('shell_reply.Kernel', function (evt, data)
        {
            var reply, cell, cellIdx;

            reply = data.reply;
            cell = easyMode.cells.execution_queue.shift();

            console.log('Last execution status: ' + reply.content.status);

            if ((easyMode.building || easyMode.ready) && reply.content.status==='error')
            {
                // First error found
                easyMode.building = false;
                easyMode.ready = false;
                isEasyModeEnabled() && alert('Ooops some code failed. Please go to ipython notebook mode and manually fix the error.');
                $(document.body).removeClass('spot');
                removeProgressIndicator();
                hideEasyMode();
                // Select and scroll to first cell with errors
                cellIdx = IPython.notebook.find_cell_index(cell);
                IPython.notebook.scroll_to_cell(cellIdx);
                IPython.notebook.select(cellIdx);
            }

            if (!easyMode.building)
            {
                return;
            }

            if (easyMode.cells.execution_queue.length===0)
            {
                console.info('Spot: Cell execution has finished');

                easyMode.ready = true;
                easyMode.building = false;

                removeProgressIndicator();
                isEasyModeEnabled() && showEasyMode();
            }
            else
            {
                updateBuildingUiIndicator();
            }
        });

        events.on('execute.CodeCell', function (ev, obj)
        {
            var cell;

            if (!easyMode.building && !easyMode.ready) return;

            cell = obj.cell;

            easyMode.cells.execution_queue.push(cell);

            console.info('Spot: Cell execution requested: ' + easyMode.cells.execution_queue.length + ' of ' + easyMode.cells.total);

            cell.clear_output(false);
            // There seems to be a bug on IPython sometimes cells with widgets dont get cleared
            // Workaround:
            cell.output_area.clear_output(false, true);
        });

        $(function ()
        {
            console.info('Spot: DOM is ready');

            easyMode.stage |= easyMode.DOM_READY;

            easyModeBootStrap(IPython);
        });
    });
});
