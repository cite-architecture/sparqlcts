package edu.holycross.shot.sparqlcts


import edu.harvard.chs.cite.CtsUrn


/** A class using knowledge of the CTS schema to generate appropriate
* SPARQL queries
*/
class QueryGenerator {

    /** Empty constructor.*/
    QueryGenerator() {
    }


    // for a version-level query
    String getDescrQuery(CtsUrn psgUrn) {
		def vers = "${psgUrn.getUrnWithoutPassage()}"
        return """
        ${CtsDefinitions.prefixPhrase}

        SELECT ?gname ?title ?lab WHERE {
          <${vers}> dcterms:title ?lab .
          <${vers}> cts:belongsTo ?wk .
          ?wk dcterms:title ?title .
          ?wk cts:belongsTo ?tg .
          ?tg dcterms:title ?gname .
        }
        """
    }



    String getVersionMetadataQuery(CtsUrn psgUrn) {
        return """
        ${CtsDefinitions.prefixPhrase}
        SELECT ?vlang ?ns ?abbr WHERE {
          <${psgUrn.toString()}> cts:belongsTo ?vers .
          ?vers cts:belongsTo ?doc .
          ?vers cts:xmlns ?ns .
           ?vers cts:translationLang ?vlang .
          ?ns cts:abbreviatedBy ?abbr .
        }
        """
    }


    /** Builds SPARQL query string to retrieve
    * default namespace for the specified passage.
    * @param urn A version-level urn with passage ref 
    * to find metadata for.
    * @returns 
    */
    String getDocMetadataQuery(CtsUrn psgUrn) {
        return """
        ${CtsDefinitions.prefixPhrase}
        SELECT ?psg ?ns ?abbr ?lang WHERE {
          <${psgUrn.toString()}> cts:belongsTo ?doc .
          ?doc cts:xmlns ?ns .
          ?doc cts:belongsTo ?wk .
          ?wk cts:lang ?lang .
          ?ns cts:abbreviatedBy ?abbr .
		  BIND(<${psgUrn.toString()}> as ?psg)
        }
        """
    }

    /** Builds SPARQL query string to retrieve
    * version mappings for a work-level CTS URN.
    * @param urn The work-level urn to find versions for.
    * @returns A complete SPARQL query string.
    */
    String getVersionQuery(CtsUrn workLevelUrn) {
        return """
        ${CtsDefinitions.prefixPhrase}
       SELECT   ?vers ?wk
        WHERE {
        ?vers cts:belongsTo  <${workLevelUrn.getUrnWithoutPassage()}>  .
	    BIND (<${workLevelUrn.getUrnWithoutPassage()}> as ?wk)
        }
        """
    }



    /** Builds SPARQL query string to retrieve
    * text content of a leaf citation node.
    * @param urn The urn of the passage to retrieve.
    * @returns A complete SPARQL query string.
    */
    String getLeafNodeQuery(CtsUrn urn) {
     return getLeafNodeQuery(urn, 0)
    }

	String getLeafNodeQuery(CtsUrn urn, Integer context) {
			//    String docUrnStr = "urn:cts:${urn.getCtsNamespace()}:${urn.getTextGroup()}.${urn.getWork()}"
			return """
					${CtsDefinitions.prefixPhrase}

			SELECT ?psg ?txt ?anc ?xpt WHERE {
					?psg cts:belongsTo+ <${urn.getUrnWithoutPassage()}> .
							?psg cts:hasTextContent ?txt .
							?psg cts:hasSequence ?s .
							?psg hmt:xpTemplate ?xpt .
							?psg hmt:xmlOpen ?anc  .

							{
									{ SELECT (xsd:int(?seq) + ${context} AS ?max)
											WHERE {
													<${urn}> cts:hasSequence ?seq .
											}
									}

									{ SELECT (xsd:int(?seq) - ${context} AS ?min)
											WHERE {
													<${urn}> cts:hasSequence ?seq .
											}
									}
							}

					FILTER (?s <= ?max) .
							FILTER (?s >= ?min) .

			}
			ORDER BY ?s 
					"""        
	}   


    /** Builds SPARQL query string to determine if a 
    * CTS URN refers to a leaf citation node.
    * @param urn The urn to test.
    * @returns A complete SPARQL query string.
    */
    String getIsLeafQuery(CtsUrn urn) {
      return """
        ${CtsDefinitions.prefixPhrase}
        ASK
         WHERE {
           <${urn}> cts:hasTextContent ?txt .
         }"""
    }



// NS:  MOD THESE NEXT TWO!

    /** Builds SPARQL query string to find the URN and
    * sequence number of the last citable node contained in
    * a given URN.  If the URN is a leaf node, returns the
    * the query to identify URN and sequence of that node.
    * @param urn The urn to test.
    * @returns A complete SPARQL query string.
    */
    String getLastContainedQuery(CtsUrn containingUrn) {

       return """
        ${CtsDefinitions.prefixPhrase}
        SELECT   ?urn ?seq
         WHERE {
            ?urn  cts:containedBy*  <${containingUrn}>  .
            ?urn cts:hasSequence ?seq .        
         }
        ORDER BY DESC(?seq)
        LIMIT 1
       """
      }

    /** Builds SPARQL query string to find the URN and
    * sequence number of the first citable node contained in
    * a given URN.  If the URN is a leaf node, returns the
    * the query to identify URN and sequence of that node.
    * @param urn The urn to test.
    * @returns A complete SPARQL query string.
    */
    String getFirstContainedQuery(CtsUrn containingUrn) {
      return """
      ${CtsDefinitions.prefixPhrase}
      SELECT   ?urn ?seq
         WHERE {
            ?urn  cts:containedBy*  <${containingUrn}>  .
            ?urn cts:hasSequence ?seq .        
         }
      ORDER BY ?seq
      LIMIT 1
      """
     }




    /** Builds SPARQL query string to find the URN 
    * of the immediately containing node of the CTS work hierarchy.
    * @param urn The urn to test.
    * @returns A complete SPARQL query string.
    */
    String getWorkContainerQuery(CtsUrn urn) {
     CtsUrn queryUrn = new CtsUrn(urn.getUrnWithoutPassage())
     return """
      ${CtsDefinitions.prefixPhrase}
      SELECT   ?container 
         WHERE {
            <${queryUrn}>  cts:belongsTo  ?container  .
         }
      """
     }


// Check if we need transitive query ...?

    /** Builds magical SPARQL query string getting ordered text contents
    * of all URNs contained within a given URN.  The URN must be
    * single node (not a range), but may be at any level of the
    * URN version hierarchy.
    * @param urn The urn to test.
    * @returns A complete SPARQL query string.
    */
    String getContainedTextQuery(CtsUrn urn, CtsUrn docUrn) {
    
    return """
     ${CtsDefinitions.prefixPhrase}
     SELECT ?u ?txt ?anc ?xpt WHERE {
    	?u cts:hasSequence ?s .
    	?u cts:hasTextContent ?txt .
        ?u cts:belongsTo <${docUrn}> .
        ?u hmt:xpTemplate ?xpt .
        ?u hmt:xmlOpen ?anc  .

    
    {	{ SELECT ?seq1  ?urn1
    	(ROUND (?seq1) AS ?min)
    	WHERE  {
                ?urn1  cts:containedBy* <${urn}>   .
                ?urn1 cts:hasSequence ?seq1 .        
             }
    	ORDER BY ?seq1
    	LIMIT 1 
    	} .
    
    
    	{ SELECT ?seq2 ?urn2
    	(ROUND (?seq2) AS ?max)
    	WHERE  {
                ?urn2  cts:containedBy*  <${urn}>  .
                ?urn2 cts:hasSequence ?seq2 .        
             }
    	ORDER BY DESC(?seq2)
    	LIMIT 1
    	} .
    }    
    
    
    	FILTER (?s <= ?max) .
    	FILTER (?s >= ?min) .
    }
    ORDER BY ?s

    """
    }



    /** Builds SPARQL query string to find URN and text contents
    * of all URNs between a given pair of URNs, non-inclusive.
    * @param urn The urn to test.
    * @returns A complete SPARQL query string.
    */
// modify to use belongsTo verb?
   String getFillQuery(Integer startSeq, Integer endSeq, String workUrnStr) {
   return """
     ${CtsDefinitions.prefixPhrase}

    SELECT ?u ?txt ?anc ?xpt WHERE {
    	?u cts:hasSequence ?s .
    	?u cts:hasTextContent ?txt .
        ?u hmt:xpTemplate ?xpt .
        ?u hmt:xmlOpen ?anc  .

    	FILTER (?s > "${startSeq}"^^xsd:integer) .    
    	FILTER (?s < "${endSeq}"^^xsd:integer) .
        FILTER (regex(str(?u), "${workUrnStr}" ) ) .
    }
    ORDER BY ?s
    """
    }


   String isTransQuery(CtsUrn urn) {
     String urnStr = urn.toString()
     return """
       ${CtsDefinitions.prefixPhrase}
       SELECT ?psg ?u  WHERE {
         ?psg cts:belongsTo ?u .
         ?u rdf:type cts:Translation .
         FILTER(str(?psg) =  "${urnStr}" ) .
       }
      """
   }

   String getNextUrnQuery(CtsUrn urn) {
    String workUrnStr = urn.getUrnWithoutPassage()
    return """
    ${CtsDefinitions.prefixPhrase}
    
    SELECT ?nextUrn ?nextSeq WHERE {
    ?nextUrn cts:hasSequence ?nextSeq .
    { SELECT  ?seq
        	WHERE  {
                   <${urn}> cts:hasSequence ?seq .       
                 }
      } .
    FILTER (?nextSeq = ?seq + 1) .
    FILTER (regex(str(?nextUrn), "${workUrnStr}" ) ) .
    }
    """
  }


   String getPrevUrnQuery(CtsUrn urn) {
    String workUrnStr = urn.getUrnWithoutPassage()
    return """
    ${CtsDefinitions.prefixPhrase}
    
    SELECT ?prevUrn ?prevSeq WHERE {
    ?prevUrn cts:hasSequence ?prevSeq .
    { SELECT ?seq
        	WHERE  {
                   <${urn}> cts:hasSequence ?seq .       
                 }
      } .
    FILTER (?prevSeq = ?seq - 1) .
    FILTER (regex(str(?prevUrn), "${workUrnStr}" ) ) .
    }
    """
  }

  String getSeqQuery(CtsUrn urn) {
   return """
    ${CtsDefinitions.prefixPhrase}
    SELECT ?urn ?seq
    	WHERE  {
               ?urn cts:hasSequence ?seq .       
               FILTER (str(?urn)="${urn}") .
             }
    """
  }



  /** Forms SPARQL query to find greatest citation depth
  * anywhere in a work.
  */
  String getLeafDepthForWorkQuery(CtsUrn urn) {
    return """
    ${CtsDefinitions.prefixPhrase}
    SELECT (MAX(?d) AS ?deepest)
    WHERE {
      ?leaf cts:containedBy* ?c .
      ?leaf cts:citationDepth ?d .
      FILTER (regex(str(?c), "${urn}" ) ) .
    }
    """
  }


  String getLeafDepthQuery(CtsUrn urn) {
    return """
    ${CtsDefinitions.prefixPhrase}
    SELECT (MAX(?d) AS ?deepest)
    WHERE {
      ?leaf cts:containedBy* ?c .
      ?leaf cts:citationDepth ?d .
      FILTER (str(?c) = "${urn}" ) .
    }
    """
  }

  String getWorkGVRQuery(CtsUrn urn, Integer level) {
    return """
    ${CtsDefinitions.prefixPhrase}
    SELECT ?ref (MIN(?s) AS ?st)
    WHERE {
      ?ref cts:citationDepth ${level} .
      ?leaf cts:containedBy* ?ref .
      ?leaf cts:hasSequence ?s .
      FILTER (regex(str(?ref), "${urn}" ) ) .
    }
    GROUP BY ?ref
    ORDER BY ?st
    """
  }


// level ...???
  String getFillGVRQuery(Integer startCount, Integer endCount, Integer level, String workUrn) {
    return """
    ${CtsDefinitions.prefixPhrase}
    SELECT ?ref 
    WHERE {
      ?ref cts:citationDepth ${level} .
      ?u cts:containedBy* ?ref .
      ?u cts:hasSequence ?s .
      FILTER (?s > "${startCount}"^^xsd:integer) .    
      FILTER (?s < "${endCount}"^^xsd:integer) .
      FILTER (regex(str(?u), "${workUrn}" ) ) .
    }
    ORDER BY ?s
   """
  }

  String getGVRNodeQuery(CtsUrn urn, Integer level) {
    return """
    ${CtsDefinitions.prefixPhrase}
    SELECT ?ref (MIN(?s) AS ?startSeq)
    WHERE {
      ?ref cts:containedBy* ?c .
      ?ref cts:citationDepth  ${level} .
      ?leaf cts:containedBy* ?ref .
      ?leaf cts:hasSequence ?s .
      FILTER (str(?c) = "${urn}" )  .
    }
    GROUP BY ?ref
    ORDER BY ?startSeq
    """
  }
}
