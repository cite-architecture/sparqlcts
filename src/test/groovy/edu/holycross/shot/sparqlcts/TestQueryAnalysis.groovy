package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test


class TestQueryAnalysis extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"

    void testCiteTypes() {
        def ctsg =  new CtsGraph(serverUrl)
        assert ctsg.isLeafNode(new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1.1"))
        assert (ctsg.isLeafNode(new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1")) == false)

    }

    void testVersion() {
        def ctsg =  new CtsGraph(serverUrl)
        String expectedVersion = "fuPers"
        assert ctsg.findVersion(new CtsUrn("urn:cts:greekLit:tlg0012.tlg001:1.1")) == expectedVersion
        assert ctsg.findVersion(new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1.1")) == expectedVersion
    }

}
