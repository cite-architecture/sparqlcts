package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestXpath extends GroovyTestCase {

    /** Object to test */
    XmlFormatter formatter = new XmlFormatter()


    // Test values
    CtsUrn  urn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1.26")
    String ancestorXpath = "/tei:TEI/tei:text/tei:body/tei:div[@n = '1']"
    String xpTemplate = "/tei:TEI/tei:text/tei:body/tei:div[@n = '?']/tei:l[@n = '?']"


    void testTrimOpen() {
        String expectedOpen = "<tei:div n = '1'>"
        assert formatter.trimAncestors(ancestorXpath, xpTemplate, 1) == expectedOpen
    }


    void testTrimClose() {
        String expectedClose = "</tei:div>"
        assert formatter.trimClose(ancestorXpath, xpTemplate, 1)  == expectedClose
    }


    void testCitationIndices() {
        def expectedIndex = [4,5]
        assert formatter.citationIndices(xpTemplate) == expectedIndex
    }


    void testStripFilters() {
        String expectedXpString = "/tei:TEI/tei:text/tei:body/tei:div/tei:l"
        assert formatter.stripFilters(xpTemplate) == expectedXpString
    }


    void testOpening() {
        String expectedXml = "<tei:TEI><tei:text><tei:body><tei:div n = '1'>"
        assert formatter.openAncestors(ancestorXpath) == expectedXml
    }


    void testClosing() {
        String expectedClosing = "</tei:div></tei:body></tei:text></tei:TEI>"
        assert formatter.closeAncestors(ancestorXpath) == expectedClosing
    }

    void testOpeningWithLimits() {
        // 0 limit is equivalent to using whole ancestor path:
        assert formatter.openAncestors(ancestorXpath, xpTemplate, 0) == formatter.openAncestors(ancestorXpath)

        String expectedLevel1 = "<tei:div n = '1'>"
        String expectedLevel2 = ""
        assert formatter.openAncestors(ancestorXpath, xpTemplate, 1) == expectedLevel1
        assert formatter.openAncestors(ancestorXpath, xpTemplate, 2) == expectedLevel2

        // too large: only 2 levels in this citation scheme
        assert shouldFail {
            formatter.openAncestors(ancestorXpath, xpTemplate, 3)
        }
    }



/*
    void testClosingWithLimits() {
        // 0 limit is equivalent to using whole ancestor path:
        assert formatter.closeAncestors(ancestorXpath, xpTemplate, 0) == formatter.closeAncestors(ancestorXpath)
        System.err.println "CLOSE 0 : " + formatter.closeAncestors(ancestorXpath)

        String expectedLevel1 = "</tei:div>"
        String expectedLevel2 = ""

        assert formatter.closeAncestors(ancestorXpath, xpTemplate, 1) == expectedLevel1
        assert formatter.closeAncestors(ancestorXpath, xpTemplate, 2) == expectedLevel2

        // too large: only 2 levels in this citation scheme
        assert shouldFail {
            formatter.openAncestors(ancestorXpath, xpTemplate, 3)
        }
    }

*/

}
