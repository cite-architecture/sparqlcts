package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestNewMods extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)

    CtsUrn ti1 = new CtsUrn("urn:cts:greekLit:tlg0011.tlg004.fu02:1")
    CtsUrn ti3 = new CtsUrn("urn:cts:greekLit:tlg0011.tlg004.fu02.tok:1")
    CtsUrn ti5 = new CtsUrn("urn:cts:greekLit:tlg0011.tlg004.fu02.tok:1.1")


    void testDepthQuery() {
        assert ctsg.getLeafDepth(ti1) == 1
        assert ctsg.getLeafDepth(ti3) == 2
        assert ctsg.getLeafDepth(ti5) == 2
    }
	
    void testNS() {
		assert ctsg.getWorkAttrs(ti1) == ' xmlns:tei="http://www.tei-c.org/ns/1.0" xml:lang="grc" '
		assert ctsg.getWorkAttrs(ti3) == ' xmlns:tei="http://www.tei-c.org/ns/1.0" xml:lang="grc" '

	}

    void testNextUrn() {
		assert ctsg.getNextUrn(ti1) == 'urn:cts:greekLit:tlg0011.tlg004.fu02:2'
		assert ctsg.getNextUrn(ti3) == 'urn:cts:greekLit:tlg0011.tlg004.fu02.tok:2'
		assert ctsg.getNextUrn(ti5) == 'urn:cts:greekLit:tlg0011.tlg004.fu02.tok:1.2'

	}

	void testTrimCloseLeaf() 
	{
			def testTrimXp = "/tei:TEI/tei:text/tei:body/tei:div1[@n='?']/tei:div2[@n='?']/tei:div3[@n='?']/tei:div4[@n='?']/tei:l[@n='?']"
			def testTrimAnc = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='2']/tei:div3[@n='3']/tei:div4[@n='4']"

			assert ctsg.formatter.trimClose(testTrimAnc,testTrimXp,1) == "</tei:div4></tei:div3></tei:div2></tei:div1>"
			assert ctsg.formatter.trimClose(testTrimAnc,testTrimXp,2) == "</tei:div4></tei:div3></tei:div2>"
			assert ctsg.formatter.trimClose(testTrimAnc,testTrimXp,3) == "</tei:div4></tei:div3>"
			assert ctsg.formatter.trimClose(testTrimAnc,testTrimXp,4) == "</tei:div4>"

			assert ctsg.formatter.trimAncestors(testTrimAnc,testTrimXp,1) == "<tei:div1 n='1'><tei:div2 n='2'><tei:div3 n='3'><tei:div4 n='4'>"
			assert ctsg.formatter.trimAncestors(testTrimAnc,testTrimXp,2) == "<tei:div2 n='2'><tei:div3 n='3'><tei:div4 n='4'>"
			assert ctsg.formatter.trimAncestors(testTrimAnc,testTrimXp,3) == "<tei:div3 n='3'><tei:div4 n='4'>"
			assert ctsg.formatter.trimAncestors(testTrimAnc,testTrimXp,4) == "<tei:div4 n='4'>"

			assert ctsg.formatter.openAncestors(testTrimAnc,0) == "<tei:TEI><tei:text><tei:body><tei:div1 n='1'><tei:div2 n='2'><tei:div3 n='3'><tei:div4 n='4'>"
			assert ctsg.formatter.openAncestors(testTrimAnc) == "<tei:TEI><tei:text><tei:body><tei:div1 n='1'><tei:div2 n='2'><tei:div3 n='3'><tei:div4 n='4'>"

	}


	void testLevelDiff()
	{
			def xp = "/tei:TEI/tei:text/tei:body/tei:div1[@n='?']/tei:div2[@n='?']/tei:div3[@n='?']/tei:div4[@n='?']/tei:l[@n='?']"
			def anc11 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='2']/tei:div3[@n='3']/tei:div4[@n='4']"
			def anc12 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='2']/tei:div3[@n='3']/tei:div4[@n='4']"

			assert ctsg.formatter.levelDiff(anc11, anc12, xp) == 0

			anc11 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='2']/tei:div3[@n='3']/tei:div4[@n='3']"
			anc12 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='2']/tei:div3[@n='3']/tei:div4[@n='4']"
			
			assert ctsg.formatter.levelDiff(anc11, anc12, xp) == 4

			anc11 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='2']/tei:div3[@n='3']/tei:div4[@n='4']"
			anc12 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='2']/tei:div3[@n='4']/tei:div4[@n='4']"
			
			assert ctsg.formatter.levelDiff(anc11, anc12, xp) == 3

			anc11 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='4']/tei:div3[@n='3']/tei:div4[@n='4']"
			anc12 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='2']/tei:div3[@n='4']/tei:div4[@n='4']"
			
			assert ctsg.formatter.levelDiff(anc11, anc12, xp) == 2

			anc11 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='1']/tei:div2[@n='4']/tei:div3[@n='3']/tei:div4[@n='4']"
			anc12 = "/tei:TEI/tei:text/tei:body/tei:div1[@n='2']/tei:div2[@n='2']/tei:div3[@n='4']/tei:div4[@n='4']"
			
			assert ctsg.formatter.levelDiff(anc11, anc12, xp) == 1
	}



}
