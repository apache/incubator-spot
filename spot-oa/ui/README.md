# Apache Spot (incubating) - User Interface

Provides tools for interactive visualization, noise filters, white listing, and attack heuristics.

## Table of Contents

- [Apache Spot - User Interface](#apache-spot---user-interface)
  * [Table of Contents](#table-of-contents)
  * [Intended Audience](#intended-audience)
  * [Getting Started](#getting-started)
  * [Technical Documentation](#technical-documentation)
    + [ReactJS + Flux](#reactjs--flux)
      - [Development/Debugging process](#developmentdebugging-process)
      - [Building modules](#building-modules)
      - [Suspicious](#suspicious)
      - [Threat Investigation](#threat-investigation)
      - [Storyboard](#storyboard)
      - [Ingest Summary](#ingest-summary)
      - [App directory structure](#app-directory-structure)
        * [App Actions](#app-actions)
          + [Spot actions](#spot-actions)
          + [Suspicious actions](#suspicious-actions)
          + [Storyboard actions](#storyboard-actions)
        * [App Components](#app-components)
          + [SPOT/ui/js/components/ChartMixin.react.js](#spotuijscomponentschartmixinreactjs)
          + [SPOT/ui/js/components/ContentLoaderMixin.react.js](#spotuijscomponentscontentloadermixinreactjs)
          + [SPOT/ui/js/components/DateInput.react.js](#spotuijscomponentsdateinputreactjs)
          + [SPOT/ui/js/components/DendrogramMixin.react.js](#spotuijscomponentsdendrogrammixinreactjs)
          + [SPOT/ui/js/components/DetailsPanelMixin.react.js](#spotuijscomponentsdetailspanelmixinreactjs)
          + [SPOT/ui/js/components/ExecutiveThreatBriefingPanel.react.js](#spotuijscomponentsexecutivethreatbriefingpanelreactjs)
          + [SPOT/ui/js/components/GridPanelMixin.react.js](#spotuijscomponentsgridpanelmixinreactjs)
          + [SPOT/ui/js/components/IPythonNotebookPanel.react.js](#spotuijscomponentsipythonnotebookpanelreactjs)
          + [SPOT/ui/js/components/Panel.react.js](#spotuijscomponentspanelreactjs)
          + [SPOT/ui/js/components/PanelRow.react.js](#spotuijscomponentspanelrowreactjs)
          + [SPOT/ui/js/components/PolloNetworkViewMixin.react.js](#spotuijscomponentspollonetworkviewreactjs)
          + [SPOT/ui/js/components/SuspiciousGridMixin.react.js](#spotuijscomponentssuspiciousgridmixinreactjs)
          + [SPOT/ui/js/components/TimelineMixin.react.js](#spotuijscomponentstimelinemixinreactjs)
        * [App Constants](#app-constants)
          + [SPOT/ui/js/constants/SpotConstants.js](#spotuijsconstantsspotconstantsjs)
        * [App Dispatchers](#app-dispatchers)
          + [SPOT/ui/js/dispatchers/SpotDispatcher.js](#spotuijsdispatchersspotdispatcherjs)
        * [App Stores](#app-stores)
          + [SPOT/ui/js/stores/SpotStores.js](#spotuijsstoresspotstoresjs)
          + [SPOT/ui/js/stores/JsonStore.js](#spotuijsstoresjsonstorejs)
          + [SPOT/ui/js/stores/RestStore.js](#spotuijsstoresreststorejs)
          + [SPOT/ui/js/stores/SpotStore.js](#spotuijsstoresspotstorejs)
        * [App Utils](#app-utils)
          + [SPOT/ui/js/utils/CategoryLauout.js](#spotuijsutilscategorylauoutjs)
          + [DateUtils.js](#dateutilsjs)
          + [SPOT/ui/js/utils/SpotUtils.js](#spotuijsutilsspotutilsjs)
      - [Pipeline directory structure](#pipeline-directory-structure)
        * [Actions](#actions)
        * [Netflow Actions](#netflow-actions)
          + [SPOT/ui/flow/js/actions/InSumActions.js](#spotuiflowjsactionsinsumactionsjs)
        * [Components](#components)
        * [Netflow Components](#netflow-components)
          + [Suspicious](#suspicious-1)
          + [Storyboard](#storyboard-1)
          + [Ingest Summary](#ingest-summary-1)
        * [DNS Components](#dns-components)
          + [Suspicious](#suspicious-2)
          + [Storyboard](#storyboard-2)
        * [Proxy Components](#proxy-components)
          + [Suspicious](#suspicious-3)
          + [Storyboard](#storyboard-3)
        * [Stores](#stores)
        * [Netflow Stores](#netflow-stores)
          + [Suspicious](#suspicious-4)
          + [Storyboard](#storyboard-4)
          + [Ingest Summary](#ingest-summary-2)
          + [Notebook](#notebook-1)
        * [DNS Stores](#dns-stores)
          + [Suspicious](#suspicious-5)
          + [Storyboard](#storyboard-5)
          + [Notebook](#notebook-2)
        * [Proxy Stores](#proxy-stores)
          + [Suspicious](#suspicious-6)
          + [Storyboard](#storyboard-6)
          + [Notebook](#notebook-3)

## Intended Audience

This document is intended for front end developers who want to contribute to our user interface. To get the most benefit from this guide, you should already have an understanding of the following technologies:

- HTML
- CSS
- JavaScript
    - ReactJS + Flux
    - D3 library

## Getting Started

Here you will find useful information to get you started on how to contribute to our User Interface (UI). For more information on how to use "Apache Spot - User Interface" (Spot UI) please read our [User Guide](https://github.com/Open-Network-Insight/open-network-insight/wiki/User%20Guide)

## Technical Documentation

Our UI is built on top of Flux application architecture principles, having an understanding of this pattern is highly recommended, for more information about Flux, please go to [Flux web page](https://facebook.github.io/flux/docs/overview.html).

### ReactJS + Flux

This architecture allows for modular code that is also scalable and easy to maintain. We have chosen ReactJS to build our UI and on top of that we adopted the Flux pattern as a complement. This allows contributors to get on board quickly as they will find lots of information on the internet.

For more information about ReactJS and Flux, please go to:

- [https://facebook.github.io/react/](https://facebook.github.io/react/)
- [https://facebook.github.io/flux/](https://facebook.github.io/flux/)

From now on we assume you are familiar with ReactJS+Flux applications.

For every path found on this document, "SPOT" refers to the path where Spot UI is installed and "PIPELINE" the pipeline subfolder you want to work on.

#### Development/Debugging process

1. Install Spot UI. Follow this [guide](INSTALL.md#install-spot-ui).
2. Run Spot UI. Follow this [guide](INSTALL.md#how-to-run-spot-ui).
    1. Set SPOT_DEV env var to 1 to enable GraphiQL UI and run ipython in debug mode.
3. Start watching for code changes
    1. $ cd SPOT/ui/PIPELINE/
    2. Watch one of the following modules
        1. Suspicous: `$ npm run watch-suspicious`
        2. Threat Investigation: `$ npm run watch-threat_investigation`
        3. Storyboard: `$ npm run watch-story_board`
        4. Ingest Summary: `$ npm run watch-ingest-summary` for netflow.
4. Start making code changes

#### Building modules

At SPOT/ui/ you can:

- Build all modules: `npm run build-all`
- Build Netflow module: `npm run build-flow`
- Build DNS module: `npm run build-dns`
- Build Proxy module: `npm run build-proxy`

The build process will create the following files:

- Suspicious: `SPOT/ui/PIPELINE/js/suspicious.bundle.min.js`
- Threat Investigation: `SPOT/ui/PIPELINE/js/threat-investigation.bundle.min.js`
- Storyboard: `SPOT/ui/PIPELINE/js/storyboard.bundle.min.js`
- Ingest Summary: `SPOT/ui/PIPELINE/js/ingest-summary.bundle.min.js` for netflow

Each pipeline has the following sections:

1. [Suspicious](#suspicious)
2. [Threat Investigation](#threat-investigation)
3. [Storyboard](#storyboard)

As an extra, netflow pipeline has:

4. [Ingest Summary](#ingest-summary)

#### Suspicious

Shows Suspicious data, reads output files from OA.

HTML file:

- *SPOT/ui/PIPELINE/suspicious.html*

JavaScript file:

- *SPOT/ui/PIPELINE/js/suspicious.js*

JavaScript bundle minified file:
- *SPOT/ui/PIPELINE/js/suspicious.bundle.min.js*

#### Threat Investigation

Tools to manage high risk threats.

HTML file:

- *SPOT/ui/PIPELINE/threat-investigation.html*

JavaScript file:

- *SPOT/ui/PIPELINE/js/threat-investigation.js*

JavaScript bundle minified file:
- *SPOT/ui/PIPELINE/js/threat-investigation.bundle.min.js*

#### Storyboard

Displays extra information for high risk threats.

HTML file:

- *SPOT/ui/PIPELINE/storyboard.html*

JavaScript file:

- *SPOT/ui/PIPELINE/js/storyboard.js*

JavaScript bundle minified file:
- *SPOT/ui/PIPELINE/js/storyboard.bundle.min.js*

#### Ingest Summary

Displays statistical information of any ingested data.

HTML file:

- *SPOT/ui/flow/ingest-summary.html*

JavaScript file:

- *SPOT/ui/flow/js/ingest-summary.js*

JavaScript bundle minified file:
- *SPOT/ui/flow/js/ingest-summary.bundle.min.js*

#### App directory structure

Our code follows the recommendation for ReactJS+Flux applications, the project structure looks like this:

- *SPOT/ui/js/*
    - [_actions_](#app-actions)
    - [_components_](#app-components)
    - [_constants_](#app-constants)
    - [_dispatchers_](#app-dispatchers)
    - [_stores_](#app-stores)
    - [_utils_](#app-utils)

* For PIPELINE specific directory structure, please go [here](#pipeline-directory-structure).

##### App Actions

###### Spot actions

_SPOT/ui/js/actions/SpotActions.js_

Actions that are used through the application

>- UPDATE_DATE
>
>   Broadcasts the new selected date
>
>- EXPAND_PANEL
>
>   Request a "panel" to "expand" to full-screen mode
>
>- RESTORE_PANEL
>
>   Request expanded "panel" to "restore" to normal mode
>
>- TOGGLE_MODE_PANEL
>
>   Request a "panel" to switch to a new "mode"

###### Suspicious actions

_SPOT/ui/js/actions/EdInActions.js_

Suspicious related actions

>- UPDATE_FILTER
>
>   Broadcasts the new filter being applied
>
>- RELOAD_SUSPICIOUS
>
>   Request to reload suspicious data
>
>- RELOAD_DETAILS
>
>   Request to reload details data
>
>- RELOAD_DETAILS_VISUAL
>
>   Request to reload visual details data
>
>- HIGHLIGHT_THREAT
>
>   Request to highlight a threat
>
>- UNHIGHLIGHT_THREAT
>
>   Request to stop highlighting a threat
>
>- SELECT_THREAT
>
>   Broadcasts the threat being selected
>
>- SELECT_SRC_IP
>
>   Broadcasts the IP being selected

###### Storyboard actions

_SPOT/ui/js/actions/StoryboardActions.js_

Defines actions that belong to storyboard section

>- RELOAD_COMMENTS
>
>   Request to reload comments data
>
>- SELECT_COMMENT
>
>   Broadcasts the comment being selected


##### App Components

###### _SPOT/ui/js/components/ChartMixin.react.js_

This mixin takes care of deciding when a chart must be built and/or drawn. A component using this mixin must provide two methods:

> - buildChart()
>
>   Runs any initialization code. Gets called when no errors where found, and component has loaded new data.
>
>
> - draw()
>
>   Here is were the chart gets drawn. Gets called when no error where found, and data is available.

It must be used along with [ContentLoaderMixin.react.js](#contentloadermixin-react-js) as it does not defines a render method.

In order to decide when to build and draw a chart, it will look for special properties on the component's state, state.**error**, state.**loading**, state.**data**.

###### _SPOT/ui/js/components/ContentLoaderMixin.react.js_

Deals with showing and hiding a loading spinner. A component using this mixing must provide a method:

> - renderContent()
>
>   Renders the actual component's content

In order to render the loading spinner, this mixin checks the content of state.**loading** property. If state.**error** is present, it will be rendered instead of calling renderContent().

###### _SPOT/ui/js/components/DateInput.react.js_

A component that allows user to pick a date. After selecting the date this component will trigger an `UPDATE_DATE` action from [SpotActions](#spotactions-js).

###### _SPOT/ui/js/components/DendrogramMixin.react.js_

This mixin takes care of the creation of dendrogram charts, more specific components should be created in order to gather data to feed this mixing.

While executing the render function, this component will look for `this.state.root` which should be an object like:
```javascript
{
  name: 'Node Label',
  children: [
              ...
  ]
}
```
Each node element in the "_children_" list has the same structure.

__Note__:

> _"children"_ property could be empty or omitted.

###### _SPOT/ui/js/components/DetailsPanelMixin.react.js_

Extends [GridPanelMixin.react.js](#gridpanelmixin-react-js) and defines common functionality for those details grid panels from suspicious, such as an empty message and attaches change event listener to a store. Final components should only worry about providing custom cell render functions. For more information on custom render functions, go [here](#gridpanelmixin-react-js).

###### _SPOT/ui/js/components/ExecutiveThreatBriefingPanel.react.js_

Renders a list of available comments added using our threat investigation section. As soon as user selects a comment it will trigger a `SELECT_COMMENT` action from  [StoryBoardActions](#storyboardactions-js) at the same as a summary is shown for selected comment. Listen for change events from [CommentsStore](#CommentsStore.js).

###### _SPOT/ui/js/components/GridPanelMixin.react.js_

A helper component which renders a table. It allows for customization on final components.

While executing the render function, this component looks for:

> - `this.state.data`
>
>   An array of objects to be rendered as rows in the table body.
>
> - `this.state.error`
>
>   A custom error message to display.
>
> - `this.state.headers`
>
>   An object holding human readable labels for table headers.
>
> - `this.state.loading`
>
>   When true, mixin will display a loading indicator.
>
> - `this.state.selectedRows`
>
>   A list of data elements that should have extra CSS class to show a selected state.

A custom render function can be defined for individual cells. A render function must follow this naming convention:

```javascript
...
_render_{CELL_NAME}_cell : function () {
    ...
}
...
```

If no custom cell render is provided, a default function will be used to render the cell value as plain text.

Each table row support the following events:

> - onClick
>
>   Subscribe to this element by adding a `_onClickRow` function to the final component.
>
> - onMouseEnter
>
>   Subscribe to this element by adding a `_onMouseEnterRow` function to the final component.
>
> - onMouseLeave
>
>   Subscribe to this element by adding a `_onMouseLeaveRow` function to the final component.

###### _SPOT/ui/js/components/IPythonNotebookPanel.react.js_

Helper component that makes it easy to render an IPython Notebook.

It listen for date changes on and uses that date to build the PATH to a ipynb file.

Properties:

- title: Used as an ID to listen for TOGGLE_MODE_PANEL actions
- date: The initial date
- ipynb: The ipynb file to be rendered

###### _SPOT/ui/js/components/Panel.react.js_

A component to wrap content and deal with general panel events.

Properties:

- title: Panel's title.
- reloadable: `true` when panel can be reloaded, `false` otherwise.
- onReload: Callback to execute when user clicks the reload button.
- expandable: `true` when panel can be expanded/restored, `false` otherwise.
- toggleable: `true` when panel can toggle between modes, `false` otherwise.

Listen for [SpotStore](#SpotStore.js) events:

> - EXPAND_PANEL
>
>   Makes panel expand and take over available space on screen by hiding extra panels.
>
> - RESTORE_PANEL
>
>   Restores panel and makes every panel visible.
>
> - TOGGLE_MODE_PANEL
>
>   Make panel toggle switch to a new mode.

###### _SPOT/ui/js/components/PanelRow.react.js_

A panel container that allows expand/restore feature to work properly. Listens for EXPAND_PANEL/RESTORE_PANEL events on child panels and reacts in the same way, either expanding or restoring.

###### _SPOT/ui/js/components/PolloNetworkViewMixin.react.js_

This mixin takes care of the creation of force directed graph charts, more specific components should be created in order to gather data to feed this mixing.

This component will look for `this.state.data` which should be an object like:
```javascript
{
  maxNodes,
  nodes: [
      { ... },
      ...
  ],
  links: [
      { ... }
  ]
}
```
Each node element must look like:

```javascript
{
    id: "UNIQUE_NODE_ID",
    label: "NODE_LABEL",
    internalIp: true,
    hits: 121
}
```
internalIp field is used internal to render this node as a diamond, true, or circle, false.

hits counts the number of times this node is referenced on raw data. It serves as a input to calculate the size of the node.

###### _SPOT/ui/js/components/SuspiciousGridMixin.react.js_

Defines common functionality for a Suspicious grid panels, such as an empty message, attaches change event listener to a store, popover events and events to highlight rows when mouse over a row. Final components should only worry about providing custom cell render functions. For more information on custom render functions, go [here](#gridpanelmixin-react-js).

Also, provides a _renderRepCell helper to make it easy to render reputation information.

Extends [GridPanelMixin](#GridPanelMixin.react.js) and provides event handlers for:

> - onMouseClick
>
>   Uses [SpotActions](#SpotActions.js). Starts `SELECT_THREAT` action, loads threat details by starting `RELOAD_DETAILS` action.
>
> - onMouseEnter
>
>   Starts `HIGHLIGHT_THREAT` on [SpotActions](#SpotActions.js).
>
> - onMouseLeave
>
>   Starts `UNHIGHLIGHT_THREAT` on [SpotActions](#SpotActions.js).

###### _SPOT/ui/js/components/TimelineMixin.react.js_

This mixin takes care of the creation of timeline charts, more specific components should be created in order to gather data to feed this mixing.

While executing the render function, this component will look for `this.state.root` which should be an object like:
```javascript
{
  name: 'Node Label',
  children: [
              ...
  ]
}
```
Each node element in the "_children_" list has the same structure.

__Note__:

> _"children"_ property could be empty or omitted.

##### App Constants

###### _SPOT/ui/js/constants/SpotConstants.js_

Defines constant values for action names, panel identifiers, panel modes, notebook source path, etc.

Actions

> UPDATE_FILTER
>
> UPDATE_DATE
>
> EXPAND_PANEL
>
> RESTORE_PANEL
>
> TOGGLE_MODE_PANEL
>
> DETAILS_MODE
>
> VISUAL_DETAILS_MODE
>
> RELOAD_SUSPICIOUS
>
> RELOAD_DETAILS
>
> RELOAD_VISUAL_DETAILS
>
> HIGHLIGHT_THREAT
>
> UNHIGHLIGHT_THREAT
>
> SELECT_THREAT
>
> SELECT_IP
>
> RELOAD_COMMENTS
>
> SELECT_COMMENT

Panel Identifiers

> SUSPICIOUS_PANEL
>
> NETVIEW_PANEL
>
> NOTEBOOK_PANEL
>
> DETAILS_PANEL
>
> COMMENTS_PANEL
>
> INCIDENT_PANEL
>
> IMPACT_ANALYSIS_PANEL
>
> GLOBE_VIEW_PANEL
>
> TIMELINE_PANEL

Misc

> MAX_SUSPICIOUS_ROWS
>
> NOTEBOOKS_PATH

##### App Dispatchers

###### _SPOT/ui/js/dispatchers/SpotDispatcher.js_

As per Flux architecture, Spot defines its own action dispatcher.

##### App Stores

_SPOT/ui/js/stores/

###### _SPOT/ui/js/stores/SpotStores.js_

Provides methods to emit, listen and stop listening for following actions:

> - UPDATE_DATE
> - EXPAND_PANEL
> - RESTORE_PANEL
> - TOGGLE_MODE_PANEL

###### _SPOT/ui/js/stores/JsonStore.js_

This store makes it easy to retrieve JSON data from server.

The internal state for this store will look like:

```javascript
{
    loading: false,
    data: ... // Whatever we got from server
}
```

When `loading` is true, store is waiting for the server's response. If any error is send by remote server, the store will look for that error code in its `errorMessages` object and a friendly message will be stored under `error`.

> errorMessages: {}

A final Store could define custom error messages for different response status code by adding properties to this object. When no custom error is found, a default one would be used.

> setFilter(name, value)

Every filter set would be used to replace placeholders on the endpoint, ie say we have a filter named "foo" with a value of "bar" and an endpoint like "/some/path/${foo}", when the time comes, a request to the server will be made to "/some/path/bar".

> resetData()

Clear every filter and data.

> setData()

Replaces any stored data and broadcast that change.

> getData()

Retrieves data.

> addChangeEventListener(callback)

Attaches a listener for data changes.

> removeChangeEventListener(callback)

Detaches listener.

> reload()

Makes proper filter replacements to endpoint and request data from server.

###### _SPOT/ui/js/stores/RestStore.js_

This store makes it easy to retrieve data delimited files from server. A quick note, the name RestStore comes from a former version.

The internal state for a store can be retrieved by calling the getData method and it looks like:

```javascript
{
  loading: false,
  headers: {
    header1: "Label for header 1",
    ...
    headerN: "Label for header N"
  },
  data: [
    {
      header1: "Value for header1",
      ...,
      headerN: "Value for headerN"
    }
  ],
  error: undefined
}
```

When `loading` is true, store is waiting for the server's response. If any error is send by remote server, the store will look for that error code in its `errorMessages` object and a friendly message will be stored under `error`.

By default this mixin works with comma separated files (CSV), however a special property called `_parser` is available to set a custom parser. A parser must pass the d3's csv parser duck test.

> errorMessages: {}

A final Store could define custom error messages for different response status code by adding properties to this object. When no custom error is found, a default one would be used.

> headers: {}

Custom headers could be defined by adding properties to this object.

> _parser

A D3 DSV like parser.

> setFilter(name, value)

Every filter set would be used to replace placeholders on the endpoint, ie say we have a filter named "foo" with a value of "bar" and an endpoint like "/some/path/${foo}", when the time comes, a request to the server will be made to "/some/path/bar".

> resetData()

Clear every filter and data.

> setData()

Replaces any stored data and broadcast that change.

> getData()

Retrieves data.

> addChangeEventListener(callback)

Attaches a listener for data changes.

> removeChangeEventListener(callback)

Detaches listener.

> reload()

Makes proper filter replacements to endpoint and request data from server. `_parser` is used to parse the server's response.

###### _SPOT/ui/js/stores/SpotStore.js_

Stores global data

Provides methods to emit, listen and stop listening for following actions:

> - UPDATE_DATE
> - EXPAND_PANEL
> - RESTORE_PANEL
> - TOGGLE_MODE_PANEL

##### App Utils

###### _SPOT/ui/js/utils/CategoryLauout.js_

A layout to build D3 charts that positions nodes on a canvas. Nodes belonging to the same category are placed together.

###### DateUtils.js

Date related utilities.

> calcDate(date, delta, unit)

Returns a new date object. The result is calculated by adding/subtracting "delta" "unit's" (days, months, years) to the original date.

> formatDate(date)

Returns the first 10 digits of a date in an ISO format.

> parseDate(dateStr)

Creates a date object from a string that looks  like "1985-01-12".

###### _SPOT/ui/js/utils/SpotUtils.js_

> IP_V4_REGEX : RegExp

To test if something is an IP v4.

> CSS_RISK_CLASSES : Object

A mapper between a risk number a CSS class.

```javascript
{'3': 'danger', '2': 'warning', '1': 'info', '0': 'default', '-1': 'default'}
```

> getCurrentDate()

Gets a date object based on the content of the URL, or current date if no date is present on URL.

> getDateString()

Returns the first 10 digits of a date in an ISO format.

> getCurentFilter()

Gets the content of the "filter" URL parameter.

> getUrlParam(name)

Get the value of the "name" URL parameter.

> setUrlParam(name, value)

Sets/Updates the "name" URL parameter.

> parseReputation(rawReps)

Creates an object holding reputation data from a string that looks like:

```javascript
"GTI:High:3::REP_SERVICE1:MyRep:1:CAT1|GROUP1;CAT2|GROUP2; ... ;CATN|GROUPN:: ... ::REP_SERVICEN:REP_STRN:-1"
```

The object looks like:

```javascript
{
    "GTI": {
        "text": "High",
        "value": 3,
        "cssClass": 'danger',
        "categories": null
    },
    "REP_SERVICE1": {
        "text": "MyRep",
        "value": 1,
        "cssClass": "info",
        "categories": [
            {
                "name": "CAT1",
                "group" "GROUP1"
            },
            {
                "name": "CAT2",
                "group" "GROUP2"
            },
            ...
            {
                "name": "CATN",
                "group" "GROUPN"
            }
        ]
    },

    ...,

    "REP_SERVICEN": {
        "text": "REP_STRN",
        "value": -1
    },
}
```

CSS class names came from SpotUtils.CSS_RISK_CLASSES.

> getHighestReputation(reps)

Gets the highest reputation value from a structure like the one returned by SpotUtils.parseReputation.

#### Pipeline directory structure

- *SPOT/ui/PIPELINE/js/*
  - [_actions_](#netflow-actions)
  - [_components_](#netflow-components)
  - _constants_
  - [_stores_](#netflow-stores)

##### Actions

##### Netflow Actions

###### _SPOT/ui/flow/js/actions/InSumActions.js_

> - RELOAD_INGEST_SUMMARY
>
>   Request to reload ingest summary data

##### Components

##### Netflow Components

_SPOT/ui/js/flow/components/_

###### Suspicious

- FilterInput.react.js
- SuspiciousPanel.react.js

    See [SuspiciousGridMixin.react.js](#suspiciousgridmixin.react.js)

- NetworkViewPanel.react.js
- DetailsPanel.react.js
- DetailsTablePanel.react.js

    See [DetailsGridMixin.react.js](#detailsgridmixin.react.js)

- DetailsChordsPanel.react.js

###### Storyboard

- IncidentProgressionPanel.react.js
- Impact AnalysisPanel.react.js
- GlobeViewPanel.react.js
- TimelinePanel.react.js

    See [TimelineMixin.react.js](#timelinemixin.react.js)

###### Ingest Summary

- IngestSummaryPanel.react.js

##### DNS Components

_SPOT/ui/js/dns/components/_

###### Suspicious

- FilterInput.react.js
- SuspiciousPanel.react.js

    See [SuspiciousGridMixin.react.js](#suspiciousdendrogrampanel.react.js)

- NetworkViewPanel.react.js
- DetailsPanel.react.js
- DetailsTablePanel.react.js
- DetailsDendrogramPanel.react.js

    See [DendrogramMixim](#dendrogrammixin-react-js)

###### Storyboard

- IncidentProgressionPanel.react.js
    See [DendrogramMixin](#DendrogramMixin.react.js).

##### Proxy Components

_SPOT/ui/js/proxy/components/

###### Suspicious

- FilterInput.react.js
- SuspiciousPanel.react.js

    See [SuspiciousGridMixin.react.js](#suspiciousdendrogrampanel.react.js)

- NetworkViewPanel.react.js
- DetailsPanel.react.js

###### Storyboard

- TimelinePanel.react.js

    See [TimelineMixin.react.js](#timelinemixin.react.js)

- IncidentProgressionPanel.react.js

    See [DendrogramMixin](#DendrogramMixin.react.js).

##### Stores

##### Netflow Stores

_SPOT/ui/js/flow/stores/_

###### Suspicious

- [SpotStore.js](#spotstore.js)

- SuspiciousStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_FILTER
    > - UPDATE_DATE
    > - RELOAD_SUSPICIOUS
    > - HIGHLIGHT_THREAT
    > - UNHIGHLIGHT_THREAT
    > - SELECT_THREAT

- DetailsStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_THREAT
    > - RELOAD_SUSPICIOUS
    > - RELOAD_DETAILS

- ChrodsDiagramStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_IP
    > - RELOAD_DETAILS_VISUAL

###### Storyboard

- [SpotStore.js](#spotstore.js)

- CommentsStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - RELOAD_COMMENTS

- IncidentProgressionStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_COMMENT

- ImpactAnalysisStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_COMMENT

- GlobeViewStore.js

    Extends [JsonStore.js](#jsonstore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_COMMENT

- TimelineStore.js

    Extends [RestStore.js](#reststore.js)

> **Actions**
    > - UPDATE_DATE
    > - SELECT_COMMENT

###### Ingest Summary

- [SpotStore.js](#spotstore.js)

- IngestSummaryStore

    Extends [RestStore.js](#reststore.js)

###### Notebook

spot-oa/ui/flow/js/stores/NotebookStore.js

spot-oa/ui/flow/js/components/ScoreNotebook.react.js


##### DNS Stores

_SPOT/ui/js/dns/stores/_

###### Suspicious

- [SpotStore.js](#spotstore.js)

- SuspiciousStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_FILTER
    > - UPDATE_DATE
    > - RELOAD_SUSPICIOUS
    > - HIGHLIGHT_THREAT
    > - UNHIGHLIGHT_THREAT
    > - SELECT_THREAT

- DetailsStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_THREAT
    > - RELOAD_SUSPICIOUS
    > - RELOAD_DETAILS

- DendrogramStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_IP
    > - RELOAD_SUSPICIOUS
    > - RELOAD_DETAILS_VISUAL

###### Storyboard

- [SpotStore.js](#spotstore.js)

- CommentsStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - RELOAD_COMMENTS

- IncidentProgressionStore.js

    Extends [RestStore.js](#reststore.js)

> **Actions**
    > - UPDATE_DATE
    > - SELECT_COMMENT
    > - RELOAD_COMMENTS

###### Notebook

spot-oa/ui/dns/js/stores/NotebookStore.js

spot-oa/ui/dns/js/components/ScoreNotebook.react.js

##### Proxy Stores

_SPOT/ui/js/proxy/_

###### Suspicious

- [SpotStore.js](#spotstore.js)

- SuspiciousStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_FILTER
    > - UPDATE_DATE
    > - RELOAD_SUSPICIOUS
    > - HIGHLIGHT_THREAT
    > - UNHIGHLIGHT_THREAT
    > - SELECT_THREAT

- DetailsStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_THREAT
    > - RELOAD_SUSPICIOUS
    > - RELOAD_DETAILS

###### Storyboard

- [SpotStore.js](#spotstore.js)

- CommentsStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - RELOAD_COMMENTS

- TimelineStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_COMMENT

- IncidentProgressionStore.js

    Extends [RestStore.js](#reststore.js)

    > **Actions**
    > - UPDATE_DATE
    > - SELECT_COMMENT
    > - RELOAD_COMMENTS

###### Notebook

spot-oa/ui/proxy/js/stores/NotebookStore.js

spot-oa/ui/proxy/js/components/ScoreNotebook.react.js