package edu.holycross.shot.sparqlcts 


import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

// Utilities for things like resolving version and citation
// hierarchies
class TestUtils extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg =  new CtsGraph(serverUrl)

// methods to test:
/*
findVersion
resolveVersion
isLeafNode
getWorkHierarchyContainer
getSequence

*/


    void testVersionResolve() {
        CtsUrn workLevel = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001:1.1")
        String expectedVersion = "urn:cts:greekLit:tlg0012.tlg001.fuPers:1.1"
        assert ctsg.resolveVersion(workLevel).toString() == expectedVersion
    }


    void testContainerResolve() {
        String expectedContainer = "urn:cts:greekLit:tlg0012.tlg001:1.1"
        assert expectedContainer == ctsg.getWorkHierarchyContainer(new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1.1"))
    }


    // add test for these methods when submitted URN is a leaf node!
    void testFirstLastContainment() {
        CtsUrn iliad1 = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1")
        assert ctsg.getFirstSequence(iliad1) == 1
        // only 610 lines in VenA book1!
        //assert ctsg.getLastSequence(iliad1) == 611
    }


    void testGetSeq() {
        CtsUrn iliadline1 = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1.1")
        assert ctsg.getSequence(iliadline1) == 1

        // getSequence method requires leaf-node URN:
        CtsUrn iliadbook1 = new CtsUrn("urn:cts:greekLit:tlg0012.tlg001.fuPers:1")
        assert shouldFail {
            cts.getSequence(iliadbook1)
        }
    }

}
