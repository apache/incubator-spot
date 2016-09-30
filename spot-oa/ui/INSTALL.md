# Installation Guide

Open Network Insight User Interface (aka ONI UI or UI) Provides tools for interactive visualization, noise filters, white listing, and attack heuristics.

Here you will find instructions to get ONI UI up and running. For more information about ONI look [here](/Open-Network-Insight/open-network-insight).

## Requirements

- IPython with notebook module enabled (== 3.2.0) [link](https://ipython.org/ipython-doc/3/index.html)
- NPM - Node Package Manager [link](https://www.npmjs.com/)
- oni-oa output
> ONI UI takes any output from [oni-oa backend](/Open-Network-Insight/oni-oa/tree/master/oa/), as input for the visualization tools provided. Please make sure there are files available under PATH_TO_ONI/ui/data/${PIPELINE}/${DATE}/

## Install ONI UI

1. Go to ONI UI folder

	`$ cd PATH_TO_ONI/ui/`

2. With root privileges, install browserify and uglify as global commands on your system.

	`# npm install -g browserify uglifyjs`

3. Install dependencies and build ONI UI

	`$ npm install`

## How to run ONI UI

1. Go to ONI UI folder

	`$ cd PATH_TO_ONI/`

2. Start the web server

	`$ ./runIpython.sh`

3. Verify your installation by going to one of the URLs found [here](https://github.com/Open-Network-Insight/oni-docs/wiki/Suspicious%20Connects).
