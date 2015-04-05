package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestFUPsg extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)

    CtsUrn wh = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fu01:")
    CtsUrn kjv = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fuKJV:")

    @Test void testGetNode() {
        CtsUrn leafNode = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fu01:1.1")
        System.err.println "LEAF: " + ctsg.getNodeText(leafNode)
    }

    @Test void testWorkLevel() {
        CtsUrn leafNode = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fu01:1.1")
        CtsUrn resolved = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.${ctsg.findVersion(leafNode)}:1.1")
        assert ctsg.getNodeText(leafNode) == ctsg.getNodeText(resolved)
    }


}
