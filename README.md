db-shovler
==========
[![Build Status](https://travis-ci.org/jhberges/db-shovler.png)](https://travis-ci.org/jhberges/db-shovler)

This is an extract of running code, that performs batch inserts into databases based on JMS messages of either explicit SQLs or prepared statements.

The component is Spring based.

Donated by onSMSC Ltd and released under the Apache License 2.0.

Setup
=====
Add dependency to 

    groupId: com.onsmsc
    artifactId: db-shovler
    version: 1.0.10
    
Add the ShovlerBean to your applicationContext as shown in the IT test.

1.0.10
===============
Return of the disappearing setMaxBatchSize()

1.0.8
=====
Stability - handle all DataAccessExceptions

1.0.7
=====
Pause on TransientDataAccessException

1.0.5
=====
Small fix to handle Spring's very own JmsException

1.0.4 + 1.0.3
=============
Some release-plugin screwups untangled.

1.0.2
=====
Available in Sonatype's OSS repository

1.0.1
=====
* Setter for maxBatchSize
* Oracle responses

1.0.0
=====
Poll messages from JMS destination and use a converter to feed a JDBC batch statement.
