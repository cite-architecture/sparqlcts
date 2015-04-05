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
class TestDescrReply extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg =  new CtsGraph(serverUrl)

    // Correct settings for XML comparisons:
    def configureXMLUnit = {
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setNormalizeWhitespace(true)
        XMLUnit.setIgnoreComments(true)
    }


    void testGetDescrReply() {
        CtsUrn urn = new CtsUrn("urn:cts:greekLit:tlg0016.tlg001.g\
rc:2.3")
        def desc =  ctsg.getDescrReply(urn)
        System.err.println "Herdotus 2.3 reply: " + desc
        def wkDescr = ctsg.getDescrReply("urn:cts:greekLit:tlg0016.tlg001.grc:")
        System.err.println "WORK reply " + wkDescr

    }


}
