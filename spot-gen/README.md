Simple data generator
======================

This tool produces random logs for any preconfigured source.
It allows to simulate real source and can be used for testing or demonstrating purposes.
For example, after adding to cron (run every hour and generate logs for last hour) and copying result log files to collector's directory data will be always actual and reports will be non-empty.
Currently generator works only in batch mode (generate log files for some period).

Included configuration files for generating cisco asa, su/sudo and windows (nxlog format) events.

Usage:
```
# datagen.py --help
usage: datagen.py [-h] [--write WRITE] [--period PERIOD] [--sort] config

Generate sample data

positional arguments:
  config           Config file

optional arguments:
  -h, --help       show this help message and exit
  --write WRITE    Write to file
  --period PERIOD  Generate data for N last days [suffix d] or last N hours
                   [suffix h] or last N minutes [suffix m]
  --sort           Use timestamp to sort events (slow)
```

Example of configuration file `conf/example.yaml`

```
---
timeformat: "%Y-%m-%d %H:%M:%S"
linebreak: "\n"

# Templates - sample of events with placeholders that must be replaced
# Parameters:
#  - samples: source for log samples
#  - period: for N seconds event can be generated from 'min' to 'max' times

templates:
  # It can be loaded from file or ...
  - { type: 'file', samples: 'example/events1.txt', period: 60, min: 1, max: 3 }
  # ... specified inline
  - { type: 'list', samples: [ '%TS% - Domain is: %DOMAIN%', '%TS% - Random int is: %_INT% random again: %_INT%' ], period: 120, min: 2, max: 4 }

# Replaces
replaces:
  # Can be loaded from file (one item per line) or ...
  - [ '%DOMAIN%', 'example/domains.txt' ]
  # ... specified inline or ...
  - [ '%CHAR%', ['a', 'b', 'c'] ]
  # ... call some python function from module or ...
  #     note on "_" prefix. It means that every occurence in one line
  #     of this parameter will be replaced by another value
  -
    - '%_INT%'
    - !!python/name:random.randint
    - [ 1, 10 ]
  # ... call your own function from some module
  -
    - '%IP%'
    - !!python/name:example.utils.get_ip
    - []
...
```
