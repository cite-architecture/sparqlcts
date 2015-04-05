package edu.holycross.shot.sparqlcts 

import edu.harvard.chs.cite.CtsUrn

import static org.junit.Assert.*
import org.junit.Test

class TestFUGP extends GroovyTestCase {
    // use default fuseki settings to test
    def serverUrl = "http://localhost:3030/ds/"
    def ctsg = new CtsGraph(serverUrl)

    CtsUrn wh = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fu01:")
    CtsUrn kjv = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fuKJV:")

    CtsUrn verseInWork = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fu01:1.2")

    CtsUrn verseInTrans = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fuKJV:1.2")

    @Test void testIsTrans() {
        System.err.println "TEST ${verseInTrans} = translation:  " + ctsg.isTranslation(verseInTrans)
//        assert ctsg.isTranslation(verseInTrans) == true
        //System.err.println ctsg.getMetadataAttrs(verseInTrans)
        //System.err.println ctsg.getMetadataAttrs(verseInWork)
    }




    @Test void testMetadata() {
    CtsUrn wh1 = new CtsUrn("urn:cts:greekLit:tlg0527.tlg001.fu01:1.1")
        String rawAttrs =  ctsg.getMetadataAttrs(wh1)
        String expectedAttrs = 'xmlns:tei="http://www.tei-c.org/ns/1.0" xml:lang="grc"'


        // Strip leading and trailing white for comparison:
        String actualStrippedAttrs = rawAttrs.replaceAll(/[ \n\t]+$/, '')
        actualStrippedAttrs = actualStrippedAttrs.replaceAll(/^[ \t]+/,'')

        assert expectedAttrs == actualStrippedAttrs

        String rawTransAttrs = ctsg.getMetadataAttrs(verseInTrans)
        String actualRawStripped = rawTransAttrs.replaceAll(/[ \n\t]+$/, '')
        actualRawStripped = actualRawStripped.replaceAll(/^[ \t]+/,'')

        assert actualRawStripped == 'xmlns:tei="http://www.tei-c.org/ns/1.0" xml:lang="eng"'

    }

    @Test void testGPP() {
        // Display one for inspection:

        System.err.println ctsg.getPassageReply(verseInWork, 0 )

    }

}
