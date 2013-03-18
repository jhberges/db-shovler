db-shovler
==========

This is an extract of running code, that performs batch inserts into databases based on JMS messages of either explicit SQLs or prepared statements.

The component is Spring based.

Setup
=====
Add the maven repo:
     https://github.com/jhberges/jhberges-mvn-repo/releases
     
Add dependency to 
    groupId: com.onsmsc
    artifactId: db-shovler
    version: 1.0.0
    
Add the ShovlerBean to your applicationContext as shown in the IT test.

1.0.0
=====
Poll messages from JMS destination and use a converter to feed a JDBC batch statement.
