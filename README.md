#sparqlcts

A servlet implementing the Canonical Text Services protocol backed by a SPARQL end point.

## Building releases ##

Since sparql cts is a library, the only publishable artifact in this project is a `.jar` file.  It is part of the `public` configuration, so you can build it with

    gradle buildPublib

and can publish it to a configured maven repository with

    gradle uploadPublib

