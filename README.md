**Current status**: Not working yet

![Travis CI build status](https://api.travis-ci.org/bneijt/unitrans.svg)

Getting started
---------------
For development

    mvn verify exec:java

Project overview
----------------
The project consists of a few basic ideas:

 - Data is stored in blocks and references by metadata blocks
 - Metadata blocks can reference other blocks (including metadata blocks) and other files
 - All blocks are stored by hash of their content
 - All blocks are therefore immutable and never directly deleted but forgotten and garbage collected
 - There is no access control on the data blocks
 - There is no access control on metadata blocks
 - Each user has a single metadata block as their root, from there all their accessible blocks are referenced
 - Each user is authenticated by a client certificate



To test
-------
 - Is it possible to create a recursive metadata block in the system which will generate an endless stream?
 - Is Messagepack robust against parsing invalid data?
