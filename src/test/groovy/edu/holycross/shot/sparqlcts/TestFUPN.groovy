package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestFUPN extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)

    CtsUrn wh = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fugnt001")
    CtsUrn kjv = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu001")
    CtsUrn vulgate = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu002")

    @Test void testPrevNext() {

        CtsUrn kjv2 =  new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu001:1.2")

        assert ctsg.getPrevSeq(kjv2) == 1
        assert  ctsg.getPrevUrn(kjv2).toString() == "urn:cts:greekLit:tlg0031.tlg001.fu001:1.1"

        CtsUrn kjv1 =  new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu001:1.1")
        assert ctsg.getPrevSeq(kjv1) == null
        assert ctsg.getPrevUrn(kjv1) == ""
    }

}
