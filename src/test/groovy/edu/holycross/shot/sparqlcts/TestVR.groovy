package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestVR extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)

    CtsUrn herodotus = new CtsUrn("urn:cts:greekLit:tlg0016.tlg001.grc:")
    CtsUrn herodotus1 = new CtsUrn("urn:cts:greekLit:tlg0016.tlg001.grc:1")

    CtsUrn iliad = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:")

    void testDepthQuery() {
        assert ctsg.getLeafDepth(herodotus1) == 2
    }

    void testWorkQuery() {
        def numBooks = 9
        def numChaps = 1571

        def herodotusChaps =   ctsg.getValidReffForWork(herodotus, 2)
        def root = new XmlParser().parseText("<root>${herodotusChaps}</root>")
        assert root.urn.size() == numChaps

        def herodotusBooks  =   ctsg.getValidReffForWork(herodotus, 1)
        def book = new XmlParser().parseText("<root>${herodotusBooks}</root>")
        assert book.urn.size() == numBooks


        def herodotusAll = ctsg.getValidReffForWork(herodotus, 2)
        def sectionReff = new XmlParser().parseText("<root>${herodotusAll}</root>")



        Integer leaf = ctsg.getLeafDepth(herodotus)
        def herodotusAllImplict = ctsg.getValidReffForWork(herodotus, leaf)
        def implicitReff = new XmlParser().parseText("<root>${herodotusAllImplict}</root>")

        assert sectionReff.urn.size() == implicitReff.urn.size() 
    }


    void testNodeReff() {
        CtsUrn iliadline = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1.1")
        String expectedReply = "<urn>urn:cts:greekLit:tlg0012.tlg001.fuPers:1.1</urn>"
        String actualReply = ctsg.getValidReffForNode(iliadline, 2) 
        // strip white spaced/nl and compare:
        //assert actualReply == expectedReply
        

        String cbook1 = ctsg.getValidReffForNode(herodotus1, 1)
        def book = new XmlParser().parseText("<root>${cbook1}</root>")
        assert book.urn.size() == 1

        String book1chaps = ctsg.getValidReffForNode(herodotus1, 2)
        def expectedChaps = 216 
        def chaps = new XmlParser().parseText("<root>${book1chaps}</root>")
        assert chaps.urn.size()  == expectedChaps

    }


    void testRangeQuery() {
        CtsUrn twobooks = new CtsUrn("urn:cts:greekLit:tlg0016.tlg001.grc:1-2")
        def bks = new XmlParser().parseText("<root>${ctsg.getValidReffForRange(twobooks, 1)}</root>")
        assert bks.urn.size() == 2


        String fillReply = ctsg.getFillVR(1,10,2, "urn:cts:greekLit:tlg0012.tlg001.fuPers:")
        def fillLines = new XmlParser().parseText("<root>${fillReply}}</root>")
        assert fillLines.urn.size() == 10

        CtsUrn iliadlines = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1.1-1.10")
        def tenlines = ctsg.getValidReffForRange(iliadlines, 2)
        def tenparsed = new XmlParser().parseText("<root>${tenlines}</root>")
        assert tenparsed.urn.size() == 10
    }

    // Add ataul reply to inspect?


}
