package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestResolveVersion extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)
    CtsUrn fake = new CtsUrn("urn:cts:greekLit:NONEXISTENTWORK:3")

    void testBogus() {
      assert shouldFail {
	CtsUrn urn = ctsg.resolveVersion(fake)
      }
    }

}
