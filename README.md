# `sparqlcts` #

## Overview ##

`sparqlcts` is a servlet implementing the Canonical Text Services protocol backed by a SPARQL end point.

## Shortest path to running using all default settings ##


To be added

## How to use `sparqlcts` more generally

### Prerequisites ###

- a SPARQL endpoint (such as fuseki) with triple statements in the CTS RDF vocabulary


### Test data set

To be added

### Configuring

To be added.

### Running ###

You can start an included jetty server with `sparqlcts` by running

    gradle jettyRunWar

By default, the server will run on localhost, port 8888.

You can build a `war` file you can add to any servlet container by running

    gradle war

The resulting `war` file will be in the `build/libs` directory.

## More documentation to be added ##

- Full documentation of the CTS RDF vocabulary.  Initial notes  in the `TBD.md` file list all verbs recognized or required in this implementation.

## Building releases ##

Since sparql cts is a library, the only publishable artifact in this project is a `.jar` file.  It is part of the `public` configuration, so you can build it with

    gradle buildPublib

and can publish it to a configured maven repository with

    gradle uploadPublib

