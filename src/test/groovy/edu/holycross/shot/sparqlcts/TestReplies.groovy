package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

// Use IBM's Normalizer class to canonicalize UTF-8 text data
import com.ibm.icu.text.Normalizer

// Use XML Unit for XML comparisons
import org.custommonkey.xmlunit.*


/** 
* XML comparisons are configured to ignore comments and white space.
*/
class TestReplies extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg =  new CtsGraph(serverUrl)

    // Correct settings for XML comparisons:
    def configureXMLUnit = {
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setNormalizeWhitespace(true)
        XMLUnit.setIgnoreComments(true)
    }


    // validate each of these tests by:
    // 1. Validating reply against GP schema.
    // 2. asserting XML equivalence to a sample document.

    void testGetPassageReply() {
        CtsUrn urn = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.1")
        def gp =  ctsg.getPassageReply(urn, 0)
        System.err.println "Iliad 1.1.: " + gp
    }

    void testGetValidReffReply() {
        // GVR on whole work
        CtsUrn caesar = new CtsUrn("urn:cts:latinLit:stoa0069.stoa001.stoa001")
        def gvrsects = ctsg.getGVRReply(caesar, 3)
//        System.err.println "CAESAR Sections: " + gvrsects

        // And with limiting URN:
        CtsUrn iliad = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01")
        def gvrbooks = ctsg.getGVRReply(iliad, 1)
//        System.err.println "ILIAD BOOKS: " + gvrbooks

        def gvrchaps = ctsg.getGVRReply(caesar, 2)
//        System.err.println "CAESAR CHAPTERS: " + gvrchaps

        // container
        CtsUrn iliad1 = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1")
        def gvrbook1 = ctsg.getGVRReply(iliad1)
//        System.err.println "ILIAD BOOK: " + gvrbook1



        // leaf node
        CtsUrn iliadline = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.chs01:1.1")
        def gvrline = ctsg.getGVRReply(iliadline)
  //      System.err.println "SINGLE LINE REPLY: " + gvrline



    }

}
