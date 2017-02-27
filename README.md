**Current status**: Not working yet

![Travis CI build status](https://api.travis-ci.org/bneijt/unitrans.svg)

Getting started
---------------
For development

    mvn verify exec:java

**beware** this will create a blockstore location based at `<your home directory>/tmp/unitrans/blockstore`.

See `mvn clean verify; java -jar target/unitrans*.jar --help` for more information.

Project overview
----------------
The basic idea of this project is to provide a immutable, garbage collected, synchronizable,
multi-user block storage service exposed via an HTTPS interface for home use.


Access API
----------
There are two types of storage: metadata and data. Both are stored in blocks:
  - Metadata blocks are:
    - UUID identified
    - Reference any number of metadata blocks
    - Reference a ordered list of data blocks with should be concatinated
    - Contain a simple key-value map with summary information
  - Data blocks are:
    - SHA256 content identified blocks

All access requires a session token
When created a session token is connected to a ROOT metadata block. This is a normal
metadata block which references all other accessible metadata blocks. Therefore data blocks.

Create new session:
-----
`session/new` with HTTP basic auth username/password.

A redirect is send to `session/<session key>` upon success, or 401 upon failure.

Access to root metadata
-----

    session/<session key>
    
Redirects to the root metadata block for this session `meta/<session key>/<root metadata uuid>`

Access to metadata
-----

    meta/<session key>/<metadata uuid>

The `session id` gives access to the root metadata block, which verifies access to the `metadata uuid`.
The data is returned as a JSON object.

Access to data
----

    data/<session key>/<metadata uuid>/<data id>

The `session id` is used to find the root metadata block, which verifies access to the `metadata
uuid` and access to the `data id`

The data is returned as a BLOB.

Writing data
--------

    data/<session key>/<metadata uuid>
   
POST data to a given metadata uuid. On success a redirect to the new metadata UUID is given.

FAQ
====
- Why not use Murmur hash for data blocks
  
  Because the implemetation of Murmur3 128 bit is architecture dependent
  and therefore not globally sharable, see [Wikipedia Murmur hash](https://en.wikipedia.org/wiki/MurmurHash)

See also
========

- [Upspin](https://upspin.io/) from Google.
- [Resilio](https://www.resilio.com/)
- [Infinit](http://infinit.sh/)
