package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test




/** Class testing methods related to Previous/Next sequencing
* of citable nodes.
* The four methods tested request 
* [prev|next] with return value [urn|sequence number].
*/
class TestPN extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)


    void testBasicPrevNext() {
        CtsUrn nodeUrn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.25")

        assert  ctsg.getNextUrn(nodeUrn) == "urn:cts:greekLit:tlg0012.tlg001.chs01:1.26"
        assert ctsg.getPrevUrn(nodeUrn) ==  "urn:cts:greekLit:tlg0012.tlg001.chs01:1.24"

        assert ctsg.getPrevSeq(nodeUrn) == 24
        assert ctsg.getNextSeq(nodeUrn) == 26


        // works across containing units:
        CtsUrn newBook = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:2.1")

        assert ctsg.getPrevUrn(newBook) ==  "urn:cts:greekLit:tlg0012.tlg001.chs01:1.611"
        assert ctsg.getNextUrn(newBook) ==  "urn:cts:greekLit:tlg0012.tlg001.chs01:2.2"

//        assert ctsg.getPrevSeq(newBook) == 611
//        assert ctsg.getNextSeq(newBook) == 613
    }

    // Edge-of-world test cases:
    void testEdges() {
        CtsUrn firstNode = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.1")
        CtsUrn lastNode = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:24.804")
        
        // String methods return empty (0-length) string:
        assert ctsg.getPrevUrn(firstNode).size() == 0
        assert ctsg.getNextUrn(lastNode).size() == 0
        
        // Integer methods return null:
        assert  ctsg.getPrevSeq(firstNode) == null
        assert  ctsg.getNextSeq(lastNode) == null

    }


    void testRanges() {
        CtsUrn rangeUrn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.20-1.25")

        assert  ctsg.getNextUrn(rangeUrn) == "urn:cts:greekLit:tlg0012.tlg001.chs01:1.26"
        assert ctsg.getPrevUrn(rangeUrn) ==  "urn:cts:greekLit:tlg0012.tlg001.chs01:1.19"

        assert ctsg.getPrevSeq(rangeUrn) == 19
        assert ctsg.getNextSeq(rangeUrn) == 26
        
    }


    void testContainers() {
        assert 1 == 1
    }

}
