package edu.harvard.chs.cite 

import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

import org.iso_relax.verifier.*;

import static org.junit.Assert.*
import org.junit.Test


/** Class to test the cite library's TextInventory class. 
*/
class TestGCSchema extends GroovyTestCase {


    String schemaPath = "testdata/schemas/cts/TextInventory.rng"
    File schemaFile = new File(schemaPath)


    String testTextInvPath = "unittests/data/hmtTI.xml"
    File f = new File(testTextInvPath)

    @Test void testSchema() {
        System.setProperty("javax.xml.validation.SchemaFactory:"+XMLConstants.RELAXNG_NS_URI,
    "com.thaiopensource.relaxng.jaxp.XMLSyntaxSchemaFactory");
        def factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI)
        def schema = factory.newSchema(schemaFile)
        def validator = schema.newValidator()


    }

}
