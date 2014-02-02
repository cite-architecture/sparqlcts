package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestVR extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)

    CtsUrn caesar = new CtsUrn("urn:cts:latinLit:stoa0069.stoa001.stoa001")
    CtsUrn caesar1 = new CtsUrn("urn:cts:latinLit:stoa0069.stoa001.stoa001:1")

    CtsUrn iliad = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01")

    void testDepthQuery() {
        assert ctsg.getLeafDepth(caesar1) == 3
    }

    void testWorkQuery() {
        def numBooks = 3
        def numChaps = 243

        def caesarChaps =   ctsg.getValidReffForWork(caesar, 2)
        def root = new XmlParser().parseText("<root>${caesarChaps}</root>")
        assert root.urn.size() == numChaps

        def caesarBooks  =   ctsg.getValidReffForWork(caesar, 1)
        def book = new XmlParser().parseText("<root>${caesarBooks}</root>")
        assert book.urn.size() == numBooks


        def caesarAll = ctsg.getValidReffForWork(caesar, 3)
        def sectionReff = new XmlParser().parseText("<root>${caesarAll}</root>")



        Integer leaf = ctsg.getLeafDepth(caesar)
        def caesarAllImplict = ctsg.getValidReffForWork(caesar, leaf)
        def implicitReff = new XmlParser().parseText("<root>${caesarAllImplict}</root>")

        assert sectionReff.urn.size() == implicitReff.urn.size() 
    }


    void testNodeReff() {
        CtsUrn iliadline = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.1")
        String expectedReply = "<urn>urn:cts:greekLit:tlg0012.tlg001.chs01:1.1</urn>"
        String actualReply = ctsg.getValidReffForNode(iliadline, 2) 
        // strip white spaced/nl and compare:
        //assert actualReply == expectedReply
        

        String cbook1 = ctsg.getValidReffForNode(caesar1, 1)
        def book = new XmlParser().parseText("<root>${cbook1}</root>")
        assert book.urn.size() == 1

        String book1chaps = ctsg.getValidReffForNode(caesar1, 2)
        def expectedChaps = 87
        def chaps = new XmlParser().parseText("<root>${book1chaps}</root>")
        assert chaps.urn.size()  == expectedChaps

        String book1sects = ctsg.getValidReffForNode(caesar1, 3)
        def expectedSects = 432
        def sects = new XmlParser().parseText("<root>${book1sects}</root>")
        assert  sects.urn.size() == expectedSects
    }


    void testRangeQuery() {
        CtsUrn twobooks = new CtsUrn("urn:cts:latinLit:stoa0069.stoa001.stoa001:1-2")
        def bks = new XmlParser().parseText("<root>${ctsg.getValidReffForRange(twobooks, 1)}</root>")
        assert bks.urn.size() == 2


        String fillReply = ctsg.getFillVR(1,10,2, "urn:cts:greekLit:tlg0012.tlg001.chs01")
        def fillLines = new XmlParser().parseText("<root>${fillReply}}</root>")
        assert fillLines.urn.size() == 8

        CtsUrn iliadlines = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.1-1.10")
        def tenlines = ctsg.getValidReffForRange(iliadlines, 2)
        def tenparsed = new XmlParser().parseText("<root>${tenlines}</root>")
        assert tenparsed.urn.size() == 10
    }

    // Add ataul reply to inspect?


}
