package edu.holycross.shot.sparqlcts

import edu.harvard.chs.cite.CiteUrn
import edu.harvard.chs.cite.CtsUrn


/*

The source code in this file is organized in 
four sections:

1. Utility methods.
2. Methods for working with sequences of citable nodes.
3. Methods for retrieving text contents of citable nodes.
4. Methods for working with valid references.
5. Methods formulating complete replies to CTS requests.

*/




/** Implementation of all requests of the Canonical Text Services
* protocol, using an RDF graph expressing the text contents in the
* CTS RDF vocabulary.  The RDF graph must be available from a
* SPARQL end point, that can be dynamically configured.
*
*/
class CtsGraph {

    boolean debug = false

    /** SPARQL query endpoint for HMT graph triples.   */
    String tripletServerUrl

    /** QueryGenerator object formulating SPARQL query strings. */
    QueryGenerator qg

    /** Utility object handling issues of wrapping appropriate XML
    * markup around replies.
    */
    XmlFormatter formatter

    /** XML namespace for SPARQL vocabulary, as groovy Namespace object. */
    static groovy.xml.Namespace sparql = new groovy.xml.Namespace("http://www.w3.org/2005/sparql-results#")


    /** Constructor initializing required value for SPARQL endpoint.   */
    CtsGraph(String serverUrl) {
        this.tripletServerUrl = serverUrl
        this.qg = new QueryGenerator()
        this.formatter = new XmlFormatter()
    }



    /* *********************************************************
    * 
    * 1.
    * Basic utility methods for working with the SPARQL endpoint,
    * and inferring information about request URNs from the RDF
    * triple store.
    *
    * ********************************************************* */


    /** Submits a SPARQL query to the configured endpoint
    * and returns the text of the reply.
    * @param acceptType  Value to use for headers.Accept in 
    * http request.  If the value of acceptType is 'applicatoin/json'
    * fuseki's additional 'output' parameter is added to the 
    * http request string so that the string returned for the
    * the request will be in JSON format.  This separates the 
    * concerns of forming SPARQL queries from the decision about
    * how to parse the reply in a given format.
    * @param query Text of SPARQL query to submit.
    * @returns Text content of reply. 
    */
    String getSparqlReply(String acceptType, String query) {
        String replyString
        def encodedQuery = URLEncoder.encode(query)
        def q = "${tripletServerUrl}query?query=${encodedQuery}"
        if (acceptType == "application/json") {
            q +="&output=json"
        } else {
			q +="&output=xml"
		}
        URL queryUrl = new URL(q)
        return queryUrl.getText("UTF-8")
        
        // HTTPBuilder is problematic with groovelets.
        /*
        def http = new HTTPBuilder(q)
        http.request( Method.GET, ContentType.TEXT ) { req ->
            headers.Accept = acceptType
            response.success = { resp, reader ->
                replyString = reader.text
            }
        }
        return replyString
        */
    }

    /**  Determines whether or not a CTS URN refers to a leaf
    * citation node.  Consults the SPARQL endpoint
    * @returns Boolean true if urn refers to a leaf citation node.
    */
    boolean isLeafNode(CtsUrn requestUrn) {
        String replyText = ""
        CtsUrn urn = resolveVersion(requestUrn)
        String reply = getSparqlReply("text/xml", qg.getIsLeafQuery(urn))
        def root = new XmlParser().parseText(reply)
        def replyNode = root[sparql.boolean][0]
        replyText = replyNode.text()
        return (replyText == "true")
    }

    boolean isTranslation(CtsUrn requestUrn) {
        String reply = getSparqlReply("application/json",qg.isTransQuery(requestUrn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(reply)
        return (parsedReply.results.bindings.size() == 1)

    }
    
    /** Composes an XML attribute String specifying a
    *  default XML namespace, and an xml:lang for a document.
    * @param requestUrn Urn to get metadata for.
    * @return A String to include in the root element of
    * GetPssage* replies.
    */
    String getMetadataAttrs(CtsUrn requestUrn) {
        if (isTranslation(requestUrn)) {
            return getTranslationAttrs(requestUrn)
        } else {
            return getWorkAttrs(requestUrn)
        }
    }


    String getTranslationAttrs(CtsUrn requestUrn) {
        StringBuffer replyText = new StringBuffer()
        CtsUrn queryUrn
        if (requestUrn.isRange()) {
            CtsUrn nsUrn = new CtsUrn("${requestUrn.getUrnWithoutPassage()}:${requestUrn.getRangeEnd()}")

			// The citations in a range may not be leaf-nodes. But any contained leaf-node will do!
			if ( isLeafNode(nsUrn) ){
					queryUrn = resolveVersion(nsUrn)
		    } else {
					Integer lastSequenceOfUrn = getLastSequence(nsUrn)
					String lastSeqUrn = getUrnForSequence(lastSequenceOfUrn, nsUrn.getUrnWithoutPassage())
					queryUrn = new CtsUrn(lastSeqUrn)	
			}

        } else if (isLeafNode(requestUrn)) {
            queryUrn = resolveVersion(requestUrn)
        } else {
            // single container URN
            String reply = getSparqlReply("application/json", qg.getFirstContainedQuery(requestUrn))
            def slurper = new groovy.json.JsonSlurper()

            def parsedReply = slurper.parseText(reply)
            parsedReply.results.bindings.each { b ->
                queryUrn = new CtsUrn(b.urn?.value)
            }
        }

        String reply = getSparqlReply("application/json", qg.getVersionMetadataQuery(queryUrn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(reply)
        parsedReply.results.bindings.each { b ->
            replyText.append(" xmlns:${b.abbr?.value}="  + '"' + b.ns?.value + '" xml:lang="' + b.vlang?.value + '" ' )
        }
        return replyText.toString()

    }

    String getWorkAttrs(CtsUrn requestUrn) {
        StringBuffer replyText = new StringBuffer()
        CtsUrn queryUrn
        if (requestUrn.isRange()) {
            CtsUrn nsUrn = new CtsUrn("${requestUrn.getUrnWithoutPassage()}:${requestUrn.getRangeEnd()}")

			// The citations in a range may not be leaf-nodes. But any contained leaf-node will do!
			if ( isLeafNode(nsUrn) ){
					queryUrn = resolveVersion(nsUrn)
		    } else {
					Integer lastSequenceOfUrn = getLastSequence(nsUrn)
					String lastSeqUrn = getUrnForSequence(lastSequenceOfUrn, nsUrn.getUrnWithoutPassage())
					queryUrn = new CtsUrn(lastSeqUrn)	
			}

        } else if (isLeafNode(requestUrn)) {
            queryUrn = resolveVersion(requestUrn)
        } else {
            // single container URN
            String reply = getSparqlReply("application/json", qg.getFirstContainedQuery(requestUrn))
            def slurper = new groovy.json.JsonSlurper()

            def parsedReply = slurper.parseText(reply)
            parsedReply.results.bindings.each { b ->
                queryUrn = new CtsUrn(b.urn?.value)
            }
        }

        String reply = getSparqlReply("application/json", qg.getDocMetadataQuery(queryUrn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(reply)
        parsedReply.results.bindings.each { b ->
            replyText.append(" xmlns:${b.abbr?.value}="  + '"' + b.ns?.value + '" xml:lang="' + b.lang?.value + '" ' )
        }
        return replyText.toString()
    }
    
    /** Determines a valid version value for a CTS URN.
    * If the URN is at the version level, this value is simply the
    * version element of the URN.  If the URN is at the work level,
    * the SPARQL endpoint is consulted and the identifier of 
    * the first version mapped to this work is returned.
    * @param urn CTS URN to find version for.
    * @returns A string with the version identifier component of a
    * version-level URN, without namespace qualifier.
    */
    String findVersion(CtsUrn urn) 
    throws Exception {
        String vers = urn.getVersion(false)
        if (vers == null) {
            String reply = getSparqlReply("text/xml", qg.getVersionQuery(urn))
            def root = new XmlParser().parseText(reply)
            root[sparql.results][sparql.result].each { r ->
                def versNode = r[sparql.binding].find {it.'@name' == 'vers'}
                versNode.children().each {
                    String urnVal = it.text()
					// we only want the first valid one!
					if (vers == null){
							try {
								CtsUrn workUrn = new CtsUrn(urnVal)
								vers = workUrn.getVersion(false)
							} catch (Exception e) {
								throw new Exception("CtsGraph:findVersion: bad urn value from triple store ${urnVal}, ${e}")
							}
					}
                }
            }
        }
        return vers
    }

    /** For a given URN, finds the URN of the immediately containing node 
    * in the CTS work hierarchy. That is, if urn is a version-level urn,
    * this method finds the corresponding work-level urn.
    * @param urn CTS URN to test.
    * @returns Containing URN
    */
    String getWorkHierarchyContainer(CtsUrn urn) {
        String container
        String containerReply = getSparqlReply("application/json", qg.getWorkContainerQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(containerReply)
        parsedReply.results.bindings.each { b ->
            if (b.container) {
                container = b.container.value
            }
        }
        if (urn.getPassageComponent()) {
            return container + ":${urn.getPassageComponent()}"
        } else {
            return container
        }
    }

    /**  Constructs a version-level CTS URN for a
    * given URN.  If the request URN already has a version,
    * it is returned unchanged.  If the request URN is at the
    * work level, a version attested in the triple store is chosen,
    * and URN referring to that version for the same passage is returned.
    * @param urn A CTS URN reference to resolve to a version-level CTS URN.
    * @returns A CTS URN at the version level citing the passage in 
    * the request urn.
    */
    CtsUrn resolveVersion(CtsUrn urn) 
    throws Exception {
				if (urn.getVersion(false) != null) {
					// already has a version component
					return urn
				} else {
					String version = findVersion(urn)
					if (version) {
						String resolvedStr 
						if (urn.getPassageComponent() != null) {
							resolvedStr  =  "${urn.getUrnWithoutPassage()}.${version}:${urn.getPassageComponent()}"
						} else {
						   resolvedStr  =  "${urn.getUrnWithoutPassage()}.${version}"
						}
						try {
							return (new CtsUrn(resolvedStr))
						} catch (Exception e) {
							throw e
						}
					} else {
						throw new Exception("CtsGraph: resolveVersion: no version found for urn ${urn}")
					}
				}
    }


    /** Finds sequence number for a requested leaf-node CtsUrn.
    * @param urn The requested CtsUrn.
    * @returns The sequence number of the requested URN.
    * @throws Exception if urn is not a leaf-node URN, or if a
    * sequence could not be found in the triple store.
    */
    Integer getSequence(CtsUrn urn) 
    throws Exception {
        if (isLeafNode(urn)) {
            StringBuffer reply = new StringBuffer()
            String ctsReply =  getSparqlReply("application/json", qg.getSeqQuery(urn))
            def slurper = new groovy.json.JsonSlurper()
            def parsedReply = slurper.parseText(ctsReply)
            parsedReply.results.bindings.each { b ->
                if (b.seq) {
                    reply.append(b.seq?.value)
                }
            }
            try {
                return reply.toString().toInteger()
            } catch (Exception e) {
                throw new Exception ("CtsGraph:getSequence: could not find sequence for ${urn}. ${e}")
            }
        } else {
            throw new Exception("CtsGraph:getSequence: ${urn} is not a leaf-node URN.")
        }
    }

    
    /** Finds the maximum citation depth of CTS URNs
    * contained by a given CtsUrn.
    * @param urn The containing URN at any level
    * (work, or passage).
    * @returns Depth of citation hierarchy of this URN, or
    * null if no depth could be determined.
    */
    Integer getLeafDepth(CtsUrn requestUrn) {
        Integer deepestInt = null
        CtsUrn urn = resolveVersion(requestUrn)
        String ctsReply 
        if (urn.getPassageComponent() == null) {
            ctsReply = getSparqlReply("application/json", qg.getLeafDepthForWorkQuery(urn))
        } else {
            ctsReply = getSparqlReply("application/json", qg.getLeafDepthQuery(urn))
        }
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
        
        String intStr
        parsedReply.results.bindings.each { b ->
            if (b.deepest) {
                intStr = b.deepest?.value
            } else {
                System.err.println "No deepest node found for query " + qg.getLeafDepthQuery(urn)
            }
            try {
                deepestInt = intStr.toInteger()
            } catch (Exception e) {
                System.err.println "Could not parse sequence ${intStr} as Integer: ${e}"
            }
        }
        return deepestInt
    }


    /* ***********************************************
    *
    * 2.
    * Methods for working with sequences of citable nodes:
    *
    * ************************************************ */

    /** Finds the URN for a given work, with a given sequence
	* Returns only one value, if there are more than one.
    * @param urn A work-level URN.
	* @param a sequence number.
    * @returns A urn, as string.
    */
	String getUrnForSequence(Integer seq, String versionUrnStr ){

        String ctsReply = getSparqlReply("application/json", qg.urnForSequence(seq, versionUrnStr))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)

        String urnStr
        parsedReply.results.bindings.each { b ->
            if (b.urn) {
                urnStr = b.urn?.value
            }
        }
        return urnStr
	}
	
    /** Finds the first sequence value for a set of URNs contained 
    * by a given URN.
    * @param urn A containing URN.
    * @returns The sequence property of this URN, as an Integer.
    */
    Integer getFirstSequence(CtsUrn urn) {
        Integer firstInt = null
        String ctsReply = getSparqlReply("application/json", qg.getFirstContainedQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
        
        String intStr
        parsedReply.results.bindings.each { b ->
            if (b.seq) {
                intStr = b.seq?.value
            }
            try {
                firstInt = intStr.toInteger()
            } catch (Exception e) {
                System.err.println "Could not parse sequence ${intStr} as Integer: ${e}"
            }

        }
        return firstInt
    }

    /** Finds the last sequence value for a set of URNs contained 
    * by a given URN.
    * @param urn A containing URN.
    * @returns The sequence property of this URN, as an Integer.
    */
    Integer getLastSequence(CtsUrn urn) {
        Integer lastInt = null
        String ctsReply = getSparqlReply("application/json", qg.getLastContainedQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
        
        String intStr
        parsedReply.results.bindings.each { b ->
            if (b.seq) {
                intStr = b.seq?.value
            }
            try {
                lastInt = intStr.toInteger()
            } catch (Exception e) {
                System.err.println "Could not parse sequence ${intStr} as Integer: ${e}"
            }
        }
        return lastInt
    }

    /** Finds the next sequence number following a given URN.
    * @param urn CTS URN to test.
    * @returns The sequence property of this URN, as an Integer, or
    * null if there is no following citable node.
    */
    Integer getNextSeq(CtsUrn requestUrn) {
        CtsUrn urn
        if (requestUrn.isRange()) {
            urn = new CtsUrn("${requestUrn.getUrnWithoutPassage()}:${requestUrn.getRangeEnd()}")
        } else {
            urn  = requestUrn
        }

        StringBuffer reply = new StringBuffer()

        String ctsReply =  getSparqlReply("application/json", qg.getNextUrnQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
        parsedReply.results.bindings.each { b ->
            if (b.nextSeq) {
                reply.append(b.nextSeq?.value)
            }
        }
        try {
            return reply.toString().toInteger()
        } catch (Exception e) {
            return null
        }
    }

    /** Finds the previous sequence number preceding a given URN.
    * @param urn CTS URN to test.
    * @returns The sequence property of this URN, as an Integer, or 
    * null if there is no preceding citable node.
    */
    Integer getPrevSeq(CtsUrn requestUrn) {
        CtsUrn urn
        if (requestUrn.isRange()) {
            urn = new CtsUrn("${requestUrn.getUrnWithoutPassage()}:${requestUrn.getRangeBegin()}")
        } else {
            urn  = requestUrn
        }

        StringBuffer reply = new StringBuffer()
        String ctsReply =  getSparqlReply("application/json", qg.getPrevUrnQuery(urn))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
        parsedReply.results.bindings.each { b ->
            if (b.prevSeq) {
                reply.append(b.prevSeq?.value)
            }
        }
        try {
            return reply.toString().toInteger()            
        } catch (Exception e) {
            return null
        }
    }


    /** Finds the previous URN preceding a given URN.
	* If the URN is a leaf node, returns the preceeding leaf node.
	* If the URN is a non-leaf node, returns the URN of the preceeding
	* citation at that level of the hierarchy.
    * @param urn CTS URN to test.
    * @returns The urn of the preceding citable node, as a String,
    * or a null String ("") if there is no preceding citable node.
    */
    String getPrevUrn(CtsUrn requestUrn) {
        CtsUrn urn
		String replyString = ""
        if (requestUrn.isRange()) {
            urn = new CtsUrn("${requestUrn.getUrnWithoutPassage()}:${requestUrn.getRangeBegin()}")
        } else {
            urn  = requestUrn
        }

		if (isLeafNode(urn)){
				StringBuffer reply = new StringBuffer()
				String ctsReply =  getSparqlReply("application/json", qg.getPrevUrnQuery(urn))
				def slurper = new groovy.json.JsonSlurper()
				def parsedReply = slurper.parseText(ctsReply)
				parsedReply.results.bindings.each { b ->
					if (b.prevUrn) {
						reply.append(b.prevUrn?.value)
					}
				}
				replyString = reply.toString()
		} else {
		    	
    		Integer depthUrn = urn.getCitationDepth()
			Integer firstSequenceOfUrn = getFirstSequence(urn)
			String firstSeqUrn = getUrnForSequence(firstSequenceOfUrn, urn.getUrnWithoutPassage())
		    String prevLeafUrnStr= getPrevUrn(new CtsUrn(firstSeqUrn))	
			if (prevLeafUrnStr != ""){
					CtsUrn prevLeafUrn = new CtsUrn(prevLeafUrnStr)
					CtsUrn prevUrn = new CtsUrn("${prevLeafUrn.trimPassage(depthUrn)}")
					replyString = prevUrn.asString
			} else { replyString = "" }
			
				  	
	    }	
        return replyString
    }



    /** Finds the next URN following a given URN.
    * @param urn CTS URN to test.
    * @returns The urn of the following citable node, as a String,
    * or a null String ("") if there is no following citable node.
    */
    String getNextUrn(CtsUrn requestUrn) {
        CtsUrn urn
		String replyString = ""
        if (requestUrn.isRange()) {
            urn = new CtsUrn("${requestUrn.getUrnWithoutPassage()}:${requestUrn.getRangeEnd()}")
        } else {
            urn  = requestUrn
        }

		if (isLeafNode(urn)){

				StringBuffer reply = new StringBuffer()
				String ctsReply =  getSparqlReply("application/json", qg.getNextUrnQuery(urn))
				def slurper = new groovy.json.JsonSlurper()
				def parsedReply = slurper.parseText(ctsReply)
				parsedReply.results.bindings.each { b ->
					if (b.nextUrn) {
						reply.append(b.nextUrn?.value)
					}
				}
				replyString = reply.toString()

		} else {
		    	
    		Integer depthUrn = urn.getCitationDepth()
			Integer lastSequenceOfUrn = getLastSequence(urn)
			String lastSeqUrn = getUrnForSequence(lastSequenceOfUrn, urn.getUrnWithoutPassage())
		    String nextLeafUrnStr= getNextUrn(new CtsUrn(lastSeqUrn))	
			if (nextLeafUrnStr != ""){
					CtsUrn nextLeafUrn = new CtsUrn(nextLeafUrnStr)
					CtsUrn nextUrn = new CtsUrn("${nextLeafUrn.trimPassage(depthUrn)}")
					replyString = nextUrn.asString
			} else { replyString = "" }
							
		}	

				return replyString
			}



    /* ***********************************************
    *
    * 3.
    * Methods for retrieving text contents of citable nodes:
    *
    * ************************************************ */


    /** Finds text contents of all URNs between a given pair of sequence numbers, 
    * inclusive (!! n.b. the previous version worked non-inclusively)  
    * @param startInt Starting point in the sequence.
    * @param endInt End point in the sequence.
    * @param workUrnStr URN identifying a work this sequence belongs to,
    * as a String.
    * @returns An ordered String concatenation of the text contents of URNs
    * in the sequence greater than startInt and less than endInt.  Note that 
    * if the text contents are in XML and include more than one citable node,
    * this reply will not be a well-formed fragment.
    */
    String getFillText(Integer startInt, Integer endInt, String workUrnStr) 
    throws Exception {
        try {
        StringBuffer reply = new StringBuffer()
        String ctsReply =  getSparqlReply("application/json", qg.getFillQuery(startInt, endInt, workUrnStr))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
        String currentWrapper = ""
        // We need to grab this, because when texts have different sections with different depths,
        // we can't count on the Next section having the same structure as the previous one.
        String currentXpt = "" 
        String currentNext = ""
		def citeDiffLevel = 0
        if (parsedReply.results.bindings.size() < 1){ 
            throw new Exception("CtsGraph: getFillText. No results")
        }
        parsedReply.results.bindings.each { b ->
            if (b.anc) {
                if (b.nxt?.value != currentNext){
                        currentNext = b.nxt?.value
                        if (b.anc?.value != currentWrapper) {
                            citeDiffLevel = formatter.levelDiff(b.anc?.value, currentWrapper, b.xpt?.value)
                            if (currentWrapper == "") {
                                    reply.append(formatter.openAncestors(b.anc?.value))
                            } else  {
                                    if (citeDiffLevel < 0){
                                    reply.append(formatter.trimClose(b.anc?.value, currentXpt,1))
                                    reply.append(formatter.trimAncestors(b.anc?.value, b.xpt?.value, 1))
                                    } else {
                                    // We might need to change 'b.xpt?.value' in the line below to 'currentXpt'
                                    reply.append(formatter.trimClose(b.anc?.value, b.xpt?.value,citeDiffLevel))
                                    reply.append(formatter.trimAncestors(b.anc?.value, b.xpt?.value, citeDiffLevel))
                                    }
                            }
                            currentWrapper = b.anc?.value
                            currentXpt = b.xpt?.value
                        }
                    if (b.txt) {
                        reply.append(b.txt?.value)
                    }
                }
            }
        }
	    reply.append(formatter.closeAncestors(currentWrapper))
		
        return reply.toString()
        } catch (Exception e){
            throw new Exception("CtsGraph Exception: GetFillText ")
        }

    }


    String getRangeText(CtsUrn urn) 
    throws Exception {
        try {

        StringBuffer reply = new StringBuffer()

        CtsUrn urn1 = new CtsUrn("${urn.getUrnWithoutPassage()}:${urn.getRangeBegin()}")
        CtsUrn urn2 = new CtsUrn("${urn.getUrnWithoutPassage()}:${urn.getRangeEnd()}")
        Integer startAtStr 
        Integer endAtStr

        if (isLeafNode(urn1)) {
            startAtStr =  getSequence(urn1)
        } else {
            startAtStr = getFirstSequence(urn1)
        }
		
        if (isLeafNode(urn2)) {
            endAtStr = getSequence(urn2)
        } else {
            endAtStr = getLastSequence(urn2)
        }

	    reply.append(getFillText(startAtStr.toInteger(), endAtStr.toInteger(), "${urn.getUrnWithoutPassage()}"))



        return reply.toString()
        } catch (Exception e){
            throw new Exception("CtsGraphException: getRangeText")
        }
    }


    String getNodeText(String urnStr) 
    throws Exception {
        try {
            CtsUrn urn  = new CtsUrn(urnStr)
            return getNodeText(urn) } catch (Exception e) { throw new Exception ("getNodeText: ${e}") } } 
    /** Gets the text content of a single citation node.  Note that
    * this will be well-formed if the URN refers to a leaf citation node,
    * but could be a set of elements if the URN is a containing node.
    * The set of XML elements is guaranteed to be ordered, but not 
    * well formed.
    * @param URN of the text to retrieve.
    * @returns An XML string of the text content of this urn.
    */
    String getNodeText(CtsUrn requestUrn) {
        return getNodeText(requestUrn, 0)
    }

    String getNodeText(CtsUrn requestUrn, Integer context) {
        return  getNodeText(requestUrn, context, true, true)
    }

    String getNodeText(CtsUrn requestUrn, Integer context, boolean openXml, boolean closeXml) 
    throws Exception {
        try {
        StringBuffer reply = new StringBuffer()
        String ctsReply
        CtsUrn urn = resolveVersion(requestUrn)
        if (isLeafNode(urn)) {
            ctsReply = getSparqlReply("application/json", qg.getLeafNodeQuery(urn, context))
        } else {
            CtsUrn docUrn = resolveVersion(new CtsUrn("${requestUrn.getUrnWithoutPassage()}"))
            ctsReply = getSparqlReply("application/json", qg.getContainedTextQuery(urn, docUrn))
        }
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
        def currentWrapper = ""
		def citeDiffLevel = 0
        if (parsedReply.results.bindings.size() < 1){
            throw new Exception ("CtsGraph: GetNodeText: No results")
        }
        parsedReply.results.bindings.eachWithIndex { b, i ->
            if (b.anc) {
                if (b.anc?.value != currentWrapper) {
				    citeDiffLevel = formatter.levelDiff(b.anc?.value, currentWrapper, b.xpt?.value)
                    if (currentWrapper == "") {
                        if (openXml)  {
                            reply.append(formatter.openAncestors(b.anc?.value))
                        }
                    } else  {
							reply.append(formatter.trimClose(b.anc?.value, b.xpt?.value,citeDiffLevel))
							reply.append(formatter.trimAncestors(b.anc?.value, b.xpt?.value, citeDiffLevel))
                    }
                    currentWrapper = b.anc?.value
                }
            }
            if (b.txt) {
                reply.append(b.txt?.value)
            }
        }
        if (closeXml) {
            reply.append(formatter.closeAncestors(currentWrapper))
        }
        return reply.toString()
        } catch (Exception e){
            throw new Exception("CtsGraph: GetNodeText: ${e}")
        }
    }


    /* **************************************************
    *
    * 4.
    * Methods for working with valid references.
    *
    * ************************************************** */



    String getValidReffForWork(CtsUrn workUrn, Integer level) {
        StringBuffer reply = new StringBuffer()
        CtsUrn urn = resolveVersion(workUrn)
        if (isLeafNode(urn)) {
            reply.append("<urn>${urn}</urn>")
        } else {
            String ctsReply = getSparqlReply("application/json", qg.getWorkGVRQuery(urn, level))
            def slurper = new groovy.json.JsonSlurper()
            def parsedReply = slurper.parseText(ctsReply)
            parsedReply.results.bindings.each { b ->
                if (b.ref) {
                    reply.append("<urn>${b.ref?.value}</urn>\n")
                }
            }
        }
        return reply.toString()
    }



    // two possible query forms: when level is at max depth,
    // and when is higher in citation hierarchy
    String getValidReffForNode(CtsUrn urn, Integer level) {
        StringBuffer replyBuff = new StringBuffer()
        String ctsReply

        ctsReply = getSparqlReply("application/json", qg.getGVRNodeQuery(urn, level))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
		if (parsedReply.results.bindings.size() < 1){
			throw new Exception ("invalid urn")
		} else if ((parsedReply.results.bindings.size() == 1) && (parsedReply.results.bindings[0].ref?.value.size() < 1)  ){
			throw new Exception ("invalid urn")
		} else { 
				parsedReply.results.bindings.each { b ->
					if (b.ref) {
						replyBuff.append("<urn>${b.ref?.value}</urn>\n")
					}
				}
		}
        return replyBuff.toString()
    }



    String getValidReffForRange(CtsUrn urn, Integer level) {
        StringBuffer reply = new StringBuffer()
        //reply.append getValidReffForNode(new CtsUrn("${urn.getUrnWithoutPassage()}:${urn.getRangeBegin()}"), level)

        CtsUrn urn1 = new CtsUrn("${urn.getUrnWithoutPassage()}:${urn.getRangeBegin()}")
        CtsUrn urn2 = new CtsUrn("${urn.getUrnWithoutPassage()}:${urn.getRangeEnd()}")

        Integer startAtStr 
        Integer endAtStr

        if (isLeafNode(urn1)) {
            startAtStr =  getSequence(urn1)
        } else {
            startAtStr = getFirstSequence(urn1)
        }
		
        if (isLeafNode(urn2)) {
            endAtStr = getSequence(urn2)
        } else {
            endAtStr = getLastSequence(urn2)
        }
        // error check tehses...
        Integer int1 = startAtStr.toInteger()
        Integer int2 = endAtStr.toInteger()
        reply.append(getFillVR(int1, int2, level, "${urn.getUrnWithoutPassage()}"))

        //reply.append getValidReffForNode(new CtsUrn("${urn.getUrnWithoutPassage()}:${urn.getRangeEnd()}"), level)

        return reply.toString()
    }


    /** Retrieves valid references filling a range. */
    String getFillVR(Integer start, Integer end, Integer level, String workUrnStr) {
        StringBuffer reply = new StringBuffer()

        String fillReply = getSparqlReply("application/json", qg.getFillGVRQuery(start, end, level, workUrnStr))
        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(fillReply)
		if (parsedReply.results.bindings.size() < 2){
			throw new Exception ("invalid urn")
		} else { 
				parsedReply.results.bindings.each { b ->
					if (b.ref) {
						reply.append("<urn>${b.ref?.value}</urn>\n")
					}
				    return reply.toString()
				}
		}
			return reply.toString()
    }


    /* **************************************************
    *
    * 5.
    * Methods formulating complete replies to CTS requests.
    *
    * ************************************************** */


    String getDescrReply(String urnStr)     
    throws Exception {
        try {
            CtsUrn urn = new CtsUrn(urnStr)
            return getDescrReply(urn)
        } catch (Exception e) {
            throw new Exception("CtsGraph:getDescrReply: ${e}")
        }
    }

    String getDescrReply(CtsUrn requestUrn) {
        CtsUrn urn = resolveVersion(requestUrn)
        
        StringBuffer replyBuff = new StringBuffer("<GetDescription xmlns='http://chs.harvard.edu/xmlns/cts' xmlns:cts='http://chs.harvard.edu/xmlns/cts'>\n<cts:request>\n<urn>${urn}</urn>\n<version>${urn.getVersion()}</version>\n</cts:request>\n")
        replyBuff.append("<cts:reply>\n")
        replyBuff.append(getDescription(urn))
        replyBuff.append("</cts:reply>\n</GetDescription>\n")
        return replyBuff.toString()
    }

    
    /* maybe switch on level and call distint descr queries from qg */
    String getDescription(CtsUrn requestUrn) {
        StringBuffer reply = new StringBuffer("<label>\n")
        String ctsReply
        CtsUrn urn = resolveVersion(requestUrn)
        ctsReply = getSparqlReply("application/json", qg.getDescrQuery(urn))
        // psg gname title lab

        def slurper = new groovy.json.JsonSlurper()
        def parsedReply = slurper.parseText(ctsReply)
        def currentWrapper = ""
        parsedReply.results.bindings.each { b ->
            reply.append("\t<groupname>${b.gname?.value}</groupname>\n")
            reply.append("\t<title>${b.title?.value}</title>\n")
            reply.append("\t<version>${b.lab?.value}</version>\n")
        }

        reply.append("</label>\n")
        return reply.toString()
    }



    String getPrevNextReply(String urnStr) 
    throws Exception {
        try {
            CtsUrn urn = new CtsUrn(urnStr)
            return getPrevNextReply(urn)
        } catch (Exception e) {
					throw new Exception("CtsGraph:getPrevNextUrnReply: ${e}")
		}
    }

    String getPrevNextReply(CtsUrn requestUrn) 
			throws Exception {
				try {
				
				CtsUrn urn = resolveVersion(requestUrn)
				// Check for valid range, first
				if (urn.isRange()){
						CtsUrn urn1 = new CtsUrn("${urn.getUrnWithoutPassage()}:${urn.getRangeBegin()}")
						CtsUrn urn2 = new CtsUrn("${urn.getUrnWithoutPassage()}:${urn.getRangeEnd()}")
						Integer startAtStr 
						Integer endAtStr
						if (isLeafNode(urn1)) {
							startAtStr =  getSequence(urn1)
						} else {
							startAtStr = getFirstSequence(urn1)
						}
						
						if (isLeafNode(urn2)) {
							endAtStr = getSequence(urn2)
						} else {
							endAtStr = getLastSequence(urn2)
						}
						if (endAtStr < startAtStr){ throw new Exception("CtsGraph:getPrevNextUrnReply: invalid range.") }
				}

				StringBuffer replyBuff = new StringBuffer("<GetPrevNextUrn xmlns:cts='http://chs.harvard.edu/xmlns/cts' xmlns='http://chs.harvard.edu/xmlns/cts'>\n<cts:request>\n<requestName>GetPrevNextUrn</requestName>\n<requestUrn>${requestUrn.asString}</requestUrn>\n</cts:request>\n<cts:reply>\n<prevnext>\n")
				replyBuff.append("<prev><urn>${getPrevUrn(urn)}</urn></prev>")
				replyBuff.append("<next><urn>${getNextUrn(urn)}</urn></next>")
				replyBuff.append("</prevnext>\n</cts:reply></GetPrevNextUrn>")
				
				return  replyBuff.toString()
				} catch (Exception e){
					throw new Exception("CtsGraph:getPrevNextUrnReply: ${e}")
				}
			}


    /**
    */
    String getPassageReply(String urnStr, Integer context) 
    throws Exception {
        try {
            CtsUrn urn = new CtsUrn(urnStr)
            return getPassageReply(urn, context)
        } catch (Exception e) {
            throw new Exception("CtsGraph:getPassageReply: ${e}")
        }
    }

    /**
    * Composes a String validating against the .rng schema for the GetPassage reply.
    * @param requestUrn URN of passage to retrieve.
    * @param context Number of citation nodes on either side of requestUrn to include
    * in the reply.
    * @returns A valid reply to the CTS GetPassage request.
    */
    String getPassageReply(CtsUrn requestUrn, Integer context) 
    throws Exception {
        try {

                boolean isLeaf = isLeafNode(requestUrn)
                CtsUrn urn = resolveVersion(requestUrn)
                String nsDecls = getMetadataAttrs(urn)
                StringBuffer psgText = new StringBuffer()
                if (urn.isRange()) {
                    psgText.append(getRangeText(urn))
                } else {
                    psgText.append(getNodeText(urn, context))
                }

                StringBuffer replyBuff = new StringBuffer("<GetPassage xmlns:cts='http://chs.harvard.edu/xmlns/cts' xmlns='http://chs.harvard.edu/xmlns/cts'>\n<cts:request>\n<requestName>GetPassage</requestName>\n<requestUrn>${requestUrn}</requestUrn>\n<requestContext>${context}</requestContext></cts:request>\n")
                replyBuff.append("<cts:reply><urn>${urn}</urn><passage ${nsDecls}>${psgText.toString()}</passage>")
                replyBuff.append("</cts:reply></GetPassage>")
                        
                        return  replyBuff.toString()
    } catch (Exception e){
        throw new Exception("CtsGraph: getPassageReply ${e}")
    }
}


    String getPassagePlusReply(String urnStr, Integer context) 
    throws Exception {
        try {
            CtsUrn urn = new CtsUrn(urnStr)
            return getPassagePlusReply(urn, context)
        } catch (Exception e) {
            throw new Exception("CtsGraph:getPassagePlusReply: ${e}")
        }
    }


    /**
    * 
    */
    String getPassagePlusReply(CtsUrn requestUrn, Integer context) {
        boolean isLeaf = isLeafNode(requestUrn)
        CtsUrn urn = resolveVersion(requestUrn)
        String nsDecls = getMetadataAttrs(urn)
        StringBuffer replyBuff = new StringBuffer("<GetPassagePlus xmlns:cts='http://chs.harvard.edu/xmlns/cts' xmlns='http://chs.harvard.edu/xmlns/cts'>\n<cts:request>\n<requestName>GetPassagePlus</requestName>\n<requestUrn>${requestUrn}</requestUrn>\n<requestContext>${context}</requestContext></cts:request>\n")

        StringBuffer psgText = new StringBuffer()
        if (urn.isRange()) {
            psgText.append(getRangeText(urn))
        } else {
            psgText.append(getNodeText(urn, context))
        }

        replyBuff.append("<cts:reply><urn>${urn}</urn>${getDescription(urn)}<passage ${nsDecls}>${psgText.toString()}</passage>\n")
        replyBuff.append("<prevnext>\n")
        replyBuff.append("<prev><urn>${getPrevUrn(urn)}</urn></prev>")
        replyBuff.append("<next><urn>${getNextUrn(urn)}</urn></next>")
        replyBuff.append("</prevnext>\n")

        replyBuff.append("</cts:reply></GetPassagePlus>")

        return  replyBuff.toString()
    }




    String getGVRReply(String urnStr, Integer level )  {
        CtsUrn urn 
        try {
            urn = new CtsUrn(urnStr)
        } catch (Exception e) {
            throw new Exception("CtsGraph:getGVRReply: could not form urn from ${urnStr} ${e}")
        }
        try {
            return getGVRReply(urn, level)
        } catch (Exception e) {
            throw new Exception("CtsGraph:getGVRReply: error getting reply for ${urnStr} at level ${level}, ${e}")
        }
        
    }

    String getGVRReply(String urnStr) 
    throws Exception {
        CtsUrn urn 
        try {
            urn = new CtsUrn(urnStr)
        } catch (Exception e) {
            throw new Exception("CtsGraph:getGVRReply: could not form urn from ${urnStr} ${e}")
        }
        try {
            return getGVRReply(urn)
        } catch (Exception e) {
            throw new Exception("CtsGraph:getGVRReply: error getting reply for ${urnStr}, ${e}")
        }
    }


    /**
    * 
    */
    String getGVRReply(CtsUrn requestUrn) 
    throws Exception {
        CtsUrn urn = resolveVersion(requestUrn)
        Integer maxLevel = getLeafDepth(urn)
        return getGVRReply(urn, maxLevel)
    }


    /**
    * 
    */
    String getGVRReply(CtsUrn requestUrn, Integer level) {
        CtsUrn urn = resolveVersion(requestUrn)
        StringBuffer replyBuff = new StringBuffer("<GetValidReff xmlns='http://chs.harvard.edu/xmlns/cts' xmlns:cts='http://chs.harvard.edu/xmlns/cts'>\n<cts:request>\n<requestName>GetValidReff</requestName>\n<requestUrn>${requestUrn}</requestUrn>\n<level>${level}</level>\n</cts:request>\n")
        replyBuff.append("<cts:reply>\n<reff>\n")

        // 3 cases to consider:
        if (urn.getPassageComponent() == null) {
            // 1. no limiting passage reference:
            replyBuff.append(getValidReffForWork(urn, level))

        } else if (urn.isRange()) {
            // 2. range request
            replyBuff.append(getValidReffForRange(urn, level))
        } else {
            // 3. single citation node (leaf or container)
            if (isLeafNode(requestUrn)) {
                replyBuff.append("<urn>${requestUrn.toString()}</urn>\n")
            } else {
                replyBuff.append(getValidReffForNode(urn, level))
            }
        }

        replyBuff.append("</reff>\n</cts:reply>\n</GetValidReff>")
        return  replyBuff.toString()
    }


}

