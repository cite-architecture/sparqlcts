package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestTextRetrieval extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"

    groovy.xml.Namespace tei = new groovy.xml.Namespace("http://www.tei-c.org/ns/1.0")

    CtsUrn urn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.10")
    CtsUrn workUrn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001:1.10")
    CtsUrn rangeUrn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.10-1.18")
    CtsUrn workRangeUrn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001:1.10-1.18")

    void testNsDecls() {
        def ctsg =  new CtsGraph(serverUrl)
        String expected = ' xmlns:tei="http://www.tei-c.org/ns/1.0" xml:lang="grc" '

        assert ctsg.getMetadataAttrs(urn) == expected
        assert ctsg.getMetadataAttrs(workUrn) == expected
        assert ctsg.getMetadataAttrs(rangeUrn) == expected
        assert ctsg.getMetadataAttrs(workRangeUrn) == expected
    }



    void testGetLeafNodeText() {
        def ctsg =  new CtsGraph(serverUrl)

        // Since only one version in our test data set, these two
        // requests should produce identical results:
        assert ctsg.getNodeText(urn) == ctsg.getNodeText(workUrn)

        def root = new XmlParser().parseText("<root ${ctsg.getMetadataAttrs(urn)}>${ctsg.getNodeText(urn)}</root>")
        assert root[tei.TEI][tei.text][tei.body][tei.div][tei.l].size() == 1
    }

/*
    void testContext() {
        def ctsg =  new CtsGraph(serverUrl)

        // Cited node PLUS 5 lines context on either side:
        def root = new XmlParser().parseText("<root ${ctsg.getMetadataAttrs(urn)}>${ctsg.getNodeText(urn, 5)}</root>")
        assert root[tei.TEI][tei.text][tei.body][tei.div][tei.l].size() == 11

        CtsUrn iliad1one = new CtsUrn ("urn:cts:greekLit:tlg0012.tlg001.chs01:1.1")
        def iliad1root = new XmlParser().parseText("<root ${ctsg.getMetadataAttrs(iliad1one)}>${ctsg.getNodeText(iliad1one, 5)}</root>")
        // Iliad 1.1  PLUS 5 lines context
        assert iliad1root[tei.TEI][tei.text][tei.body][tei.div][tei.l].size() == 6

    }
*/
}
