**Current status**: Not working yet


Getting started
---------------
For development

    mvn verify exec:java


Current "could be better" list
------------------------------

 - Corrupt metadata blocks are not removed/cleaned up yet
 - There is not check on disk usage or statistics yet
 - Is Messagepack robust against parsing invalid data?


To test
-------
 - Is it possible to create a recursive metadata block in the system which will generate an endless stream?
