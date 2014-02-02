package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestFUTexts extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)

    CtsUrn wh = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fugnt001")
    CtsUrn kjv = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu001")
    CtsUrn vulgate = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu002")



    @Test void testContainer() {
        assert ctsg.getWorkHierarchyContainer(kjv) == "urn:cts:greekLit:tlg0031.tlg001"
        CtsUrn psg = new CtsUrn ("urn:cts:greekLit:tlg0031.tlg001:1.2")
        assert ctsg.getWorkHierarchyContainer(psg) == "urn:cts:greekLit:tlg0031:1.2"
    }

    @Test void testNodeStatus() {
        CtsUrn leaf = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001:1.2")
        CtsUrn container = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001:1")
        assert ctsg.isLeafNode(leaf)
        assert ctsg.isLeafNode(container) == false
    }

    @Test void testFindVersion() {
        def possibleVersions = ["fugnt001", "fu001", "fu002"]
        CtsUrn oneLine = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001:1.1")
        assert ctsg.findVersion(oneLine) in possibleVersions
    }

    @Test void testDepthQuery() {
        // citation by chapter+verse
        assert ctsg.getLeafDepth(wh) == 2
        assert ctsg.getLeafDepth(kjv) == 2
        assert ctsg.getLeafDepth(vulgate) == 2
    }


    @Test void testValidReff() {
        // 28 chapters in Matthew.
        def numChaps = 28

        def kjvchaps = ctsg.getValidReffForWork(kjv, 1)
        def kjvroot = new XmlParser().parseText("<root>${kjvchaps}</root>")
        assert kjvroot.urn.size() == numChaps

        def vulgchaps = ctsg.getValidReffForWork(vulgate, 1)
        def vulgroot = new XmlParser().parseText("<root>${vulgchaps}</root>")
        assert vulgroot.urn.size() == numChaps

        def whchaps = ctsg.getValidReffForWork(wh,1)
        def whroot = new XmlParser().parseText("<root>${whchaps}</root>")
        assert whroot.urn.size() == numChaps
     

        // But how many verses?
        def numVerses = 1071

        def kjvvv = ctsg.getValidReffForWork(kjv,2)
        def kjvvroot = new XmlParser().parseText("<root>${kjvvv}</root>")
        assert kjvvroot.urn.size() == numVerses

        Integer leaf = ctsg.getLeafDepth(kjv)
        def kjvAllImplicit = ctsg.getValidReffForWork(kjv, leaf)
        def implicitReff = new XmlParser().parseText("<root>${kjvAllImplicit}</root>")

        assert kjvvroot.urn.size() == implicitReff.urn.size() 

        // Get list for testing against other versions:
        def kjvlist = []
        kjvvroot.urn.each {
            CtsUrn u = new CtsUrn(it.text())
            kjvlist.add(u.getPassageComponent())
        }


        def whvv = ctsg.getValidReffForWork(wh,2)
        def whvroot = new XmlParser().parseText("<root>${whvv}</root>")
        def whlist = []
        whvroot.urn.each {
            CtsUrn u = new CtsUrn(it.text())
            whlist.add(u.getPassageComponent())
        }

        def expectedDiff1 = ["17.21", "18.11", "23.14"]
        def diff1 = kjvlist - whlist
        assert diff1 == expectedDiff1

        def vulgvv = ctsg.getValidReffForWork(vulgate,2)
        def vulgvroot = new XmlParser().parseText("<root>${vulgvv}</root>")
        def vulglist = []
        vulgvroot.urn.each {
            CtsUrn u = new CtsUrn(it.text())
            vulglist.add(u.getPassageComponent())
        }

        def expectedDiff2 = ["17.27"]
        def diff2 = kjvlist - vulglist
        assert diff2 == expectedDiff2
    }

    @Test void testGVRforNode() {
        CtsUrn kjvLine = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu001:1.1")
        String expectedReply = "<urn>urn:cts:greekLit:tlg0031.tlg001.fu001:1.1</urn>"
        String actualRawReply = ctsg.getValidReffForNode(kjvLine,2)
        // strip white space for comparison:
        String actualStrippedReply = actualRawReply.replaceAll(/[\n \t]/,'')
        assert actualStrippedReply == expectedReply

        CtsUrn kjvChap1 = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu001:1")
        def chapLevel = ctsg.getValidReffForNode(kjvChap1,1)
        def chapLevelRoot = new XmlParser().parseText("<root>${chapLevel}</root>")
        assert chapLevelRoot.urn.size() == 1

        def verseLevel = ctsg.getValidReffForNode(kjvChap1,2)
        def verseLevelRoot = new XmlParser().parseText("<root>${verseLevel}</root>")

        def expectedVerseCount = 25
        def actualVerseCount = verseLevelRoot.urn.size()
        assert actualVerseCount == expectedVerseCount
    }



    void testRangeQuery() {
        CtsUrn twoChaps = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu001:1-2")
        def chaps = new XmlParser().parseText("<root>${ctsg.getValidReffForRange(twoChaps, 1)}</root>")
        assert chaps.urn.size() == 2

        def vvs =  new XmlParser().parseText("<root>${ctsg.getValidReffForRange(twoChaps, 2)}</root>")
        assert vvs.urn.size() == 48
    }

    @Test void testSequenceCounting() {
        CtsUrn chapTwo = new CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu001:2")
        Integer depth = ctsg.getLeafDepth(chapTwo)

        Integer seq1 = ctsg.getFirstSequence(chapTwo)
        Integer seq2 = ctsg.getLastSequence(chapTwo)

        String fillReply = ctsg.getFillVR(seq1,seq2,depth, chapTwo.toString())
        def fillLines = new XmlParser().parseText("<root>${fillReply}}</root>")
        // Compare gvr for the same range:
        // I.e., compare inclusive count of number in range with
        // difference of sequence values:
        def verseLevel = ctsg.getValidReffForNode(chapTwo,2)
        def verseLevelRoot = new XmlParser().parseText("<root>${verseLevel}</root>")
        assert verseLevelRoot.urn.size() - 1  == seq2 - seq1
        
    }

}
