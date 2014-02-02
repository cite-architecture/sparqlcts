package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestContextSpan extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"

    groovy.xml.Namespace tei = new groovy.xml.Namespace("http://www.tei-c.org/ns/1.0")

    CtsUrn iliad2one = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:2.1")


    void testRangeXing() {
        def ctsg =  new CtsGraph(serverUrl)

        String spanText = ctsg.getNodeText(iliad2one, 2)
        def root = new XmlParser().parseText("<root ${ctsg.getMetadataAttrs(iliad2one)}>${spanText}</root>")
        // 5 lines spanning 2 books:
        assert root[tei.TEI][tei.text][tei.body][tei.div].size() == 2
        assert root[tei.TEI][tei.text][tei.body][tei.div][tei.l].size() == 5
    }
}
