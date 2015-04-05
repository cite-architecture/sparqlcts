package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestFUPN extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)

    CtsUrn wh = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fu01:")
    CtsUrn kjv = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fuKJV:")

    @Test void testPrevNext() {

        CtsUrn kjv2 =  new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fuKJV:1.2")

        assert ctsg.getPrevSeq(kjv2) == 1
        assert  ctsg.getPrevUrn(kjv2).toString() == "urn:cts:greekLit:tlg0527.tlg001.fuKJV:1.1"

        CtsUrn kjv1 =  new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fuKJV:1.1")
        assert ctsg.getPrevSeq(kjv1) == null
        assert ctsg.getPrevUrn(kjv1) == ""
    }

}
