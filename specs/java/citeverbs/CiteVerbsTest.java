package citeverbs;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.concordion.integration.junit3.ConcordionTestCase;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/* Run this class as a JUnit test. */

public class CiteVerbsTest extends ConcordionTestCase {

    // SPARQL endpoint to use for tests:
    String url = "http://localhost:8888/sparqlcts/api";
    
    public String getVersion()
    throws Exception {
	String version  = "";
	try {
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    Document document = db.parse(new URL(url + "?request=GetVersion").openStream());
	    NodeList root = document.getChildNodes();

	    for (int i=0; i< root.getLength();i++) {
		Node node= root.item(i);
		if(node.getNodeName().equalsIgnoreCase("version")) {
		    version =  node.getTextContent();
		}
	    }
	} catch (Exception e) {
	    throw e;
	}
	return version;
    }


}
