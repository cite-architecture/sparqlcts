package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestComplexRetrieval extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"

    groovy.xml.Namespace tei = new groovy.xml.Namespace("http://www.tei-c.org/ns/1.0")

    CtsUrn iliad1 = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1")

    void testGetContainerNs() {
        def ctsg =  new CtsGraph(serverUrl)
        String expectedDecl = ' xmlns:tei="http://www.tei-c.org/ns/1.0" xml:lang="grc" '
        assert ctsg.getMetadataAttrs(iliad1) == expectedDecl
    }

    void testGetContainerNodeText() {
        def ctsg =  new CtsGraph(serverUrl)
        String iliad1lines = ctsg.getNodeText(iliad1)
        CtsUrn docUrn = new CtsUrn ("urn:cts:greekLit:tlg0012.tlg001.chs01")
        //System.err.println "Query : " + ctsg.qg.getContainedTextQuery(iliad1, docUrn)

        def root = new XmlParser().parseText("<root ${ctsg.getMetadataAttrs(iliad1)}>${iliad1lines}</root>")

        // Allen has 611 lines in book 1?
        assert  root[tei.TEI][tei.text][tei.body][tei.div][tei.l].size() == 611
    }



    void testRangeText() {
        def ctsg =  new CtsGraph(serverUrl)
        CtsUrn rangeUrn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.1-1.2")
        String rangeText = ctsg.getRangeText(rangeUrn)
        def root = new XmlParser().parseText("<root ${ctsg.getMetadataAttrs(rangeUrn)}>${rangeText}</root>")
        assert root[tei.TEI][tei.text][tei.body][tei.div][tei.l].size() == 2
    }


    void testRangeXing() {
        def ctsg =  new CtsGraph(serverUrl)
        CtsUrn rangeUrn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.609-2.2")
        String rangeText = ctsg.getRangeText(rangeUrn)

        System.err.println "RANGE: " + rangeText
        def root = new XmlParser().parseText("<root ${ctsg.getMetadataAttrs(rangeUrn)}>${rangeText}</root>")
        // 5 lines spanning 2 books:
        assert root[tei.TEI][tei.text][tei.body][tei.div].size() == 2
        assert root[tei.TEI][tei.text][tei.body][tei.div][tei.l].size() == 5

    }
}
