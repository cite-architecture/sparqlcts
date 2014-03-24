<<<<<<< HEAD
# `sparqlcts` #

## Overview ##

`sparqlcts` is a servlet implementing the Canonical Text Services protocol backed by a SPARQL end point.

## Shortest path to running using all default settings ##


1. `cp gradle.properties-dist gradle.properties`
2. `/PATH/TO/fuseki-server --file=/PATH/TO/sparqlcts/testdata/CtsTtl.ttl /ds &`
3. `gradle jettyRunWar`
4. There is no step 4.  Point your browser at `http://localhost:8080/sparqlcts/`


## How to use `sparqlcts` more generally

### Prerequisites ###

- a SPARQL endpoint (such as fuseki) with triple statements in the CTS RDF vocabulary


### Test data set

 The `sample-data` directory includes a set of openly licensed editions, both as TEI-compliant XML, and as RDF statements in TTL format. The file `cts.ttl`  contains the complete contents of the editions, and the  CTS TextInventory is also copied in `src/main/webap/invs/inventory.xml`, so if you load `sample-data/cts.ttl` into a triple store, you can run `sparqlcts` with this data set.

### Configuring

1. Copy `gradle.properties-default` to `gradle.properties`
2. In `gradle.properties`, set the value of `tripleserver` to point to your SPARQL end point.  (By default, it points to fuseki's default end point running on `localhost`.)
3. In `gradle.properties`, set the value of `inventoryDir` to point to a directory containing one or more TextInventory files.  By default, `sparqlcts` looks for an inventory named `inventory.xml`, but any inventory in `inventoryDir` may be used by including a parameter named `inv` with the CTS `GetCapabilities` request.


### Running ###

You can start an included jetty server with `sparqlcts` by running

    gradle jettyRunWar

By default, the server will run on localhost, port 8888.

You can build a `war` file you can add to any servlet container by running

    gradle war

The resulting `war` file will be in the `build/libs` directory.

## More documentation to be added ##

- Full documentation of the CTS RDF vocabulary.  Initial notes  in the `TBD.md` file list all verbs recognized or required in this implementation.

=======
#sparqlcts

A servlet implementing the Canonical Text Services protocol backed by a SPARQL end point.

## Building releases ##

Since sparql cts is a library, the only publishable artifact in this project is a `.jar` file.  It is part of the `public` configuration, so you can build it with

    gradle buildPublib

and can publish it to a configured maven repository with

    gradle uploadPublib

>>>>>>> ea254113ea7f20373bcf54d6dbbd1b8208445929
