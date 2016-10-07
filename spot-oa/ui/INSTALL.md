# Apache Spot (incubating) - Installation Guide

Apache Spot - User Interface (aka Spot UI or UI) Provides tools for interactive visualization, noise filters, white listing, and attack heuristics.

Here you will find instructions to get Spot UI up and running. For more information about Spot look [here](../../).

## Requirements

- IPython with notebook module enabled (== 3.2.0) [link](https://ipython.org/ipython-doc/3/index.html)
- NPM - Node Package Manager [link](https://www.npmjs.com/)
- spot-oa output
> Spot UI takes any output from [spot-oa backend](../oa/), as input for the visualization tools provided. Please make sure there are files available under PATH_TO_SPOT/ui/data/${PIPELINE}/${DATE}/

## Install Spot UI

1. Go to Spot UI folder

	`$ cd PATH_TO_SPOT/ui/`

2. With root privileges, install browserify and uglify as global commands on your system.

	`# npm install -g browserify uglifyjs`

3. Install dependencies and build Spot UI

	`$ npm install`

## How to run Spot UI

1. Go to Spot UI folder

	`$ cd PATH_TO_SPOT/`

2. Start the web server

	`$ ./runIpython.sh`

3. Verify your installation by going to one of the URLs found [here](https://github.com/Open-Network-Insight/open-network-insight/wiki/Suspicious%20Connects).
