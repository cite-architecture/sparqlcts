package edu.holycross.shot.sparqlcts


import edu.harvard.chs.cite.CtsUrn


/**
* A class for working with XPath values and XPath templates for citation. 
* Provides methods formatting Strings to express the opening and closing XML markup of 
* XPath values.
*/
class XmlFormatter {

    /** Empty constructor.*/
    XmlFormatter() {
    }


	/**
	* Given two ancestor xpath statements, report on whether, and report 
	* the shallowest (furthest left) level at which they are the same.
	* e.g. 
	*      '1.1.1.1.5' and '1.1.1.1.5' --> 0
	*      '1.1.1.2.1' and '1.1.1.1.1' --> 4
	*      '1.1.1.1.1' and '1.1.2.1.1' --> 3
	*      '1.1.1.1.1' and '1.2.1.1.1' --> 2
	*	   An error (xpaths of different length, for example, returns -1
	* @param anc1 The first Ancestor Xpath
	* @param anc2 The second Ancestor Xpath
	* @param xp The XPath template
	* @returns An integer, -1 for error, 0 for perfect match, otherwise indicating the level of the shallowest point of difference
	*/
    Integer levelDiff ( String anc1, String anc2, String xp){
		def pathParts1 = anc1.split(/\//)
		def pathParts2 = anc2.split(/\//)
		String temp1 = ""
		String temp2 = ""
		Integer counter
		def retVal = 0
        if (pathParts1.size() != pathParts2.size()){
                retVal =  -1;
        } else {
                def citeIndex = citationIndices(xp)
                Integer howMany = citeIndex.size()
                Integer lastIndex = citeIndex[howMany - 1].toInteger()
                Integer firstIndex = citeIndex[0].toInteger()
                lastIndex--
                firstIndex--
                counter = 0
                for (i in firstIndex .. lastIndex){
                        if (pathParts1[i] != pathParts2[i]){
                                retVal = counter
                                        break
                        }
                        counter++
                }
                if (counter > lastIndex){ retVal = 0 }
        }
        return retVal
	}


    /**
    * Converts an XPath expression for the ancestors of a node
    * to the opening XML markup of that node in the
    * XML serialization of a citable node of text.
    * @param xpAncestor A String specifying an XPath with citation values 
    * for a citable node of a document.
    * @returns A String with the XML of the opening element markup
    * of ancestor elements given in xpTemplate.
    */
    String openAncestors (String xpAncestor) {
        return openAncestors(xpAncestor, 0)
    }

    String trimClose(String xpAncestor, String xpt, Integer limit) {
			if (limit < 1) { limit = 1 }
			StringBuffer formatted = new StringBuffer()
			def pathParts = xpt.split(/\//)
			def citeIndex = citationIndices(xpt)
			if (limit >= citeIndex.size()){ limit = (citeIndex.size() - 1) }
			def limitIndex = citeIndex[limit-1]
			def pathMax = citeIndex[citeIndex.size() - 2]
			for (i in pathMax .. limitIndex) {
				formatted.append("</" + stripFilters(pathParts[i]) + ">")
			}
	return formatted.toString()
    }

    String trimAncestors(String xpAncestor, String xpt, Integer limit) {
	StringBuffer formatted = new StringBuffer()
	def pathParts = xpAncestor.split(/\//)
    def citeIndex = citationIndices(xpt) 
    def limitIndex = citeIndex[limit-1] //  
    def pathMax = citeIndex[citeIndex.size() - 2]
    for (i in pathMax .. limitIndex) {
			formatted.insert(0,"<" + filtersToAttrs(pathParts[i]) + ">")
    }
	return formatted.toString()
    }

    String openAncestors(String xpAncestor, Integer limit) {
	StringBuffer formatted = new StringBuffer()
	def pathParts = xpAncestor.split(/\//)

	pathParts.each {
	    if (it != "") {  formatted.append("<" + filtersToAttrs(it) + ">") }
	}
        
	return formatted.toString()
   }


    /** Indexes which elements in the hierarchy of an XPath are
    * citation templates.
    * @param citationTemplate The XPath template to examine.
    * @returns A list of 1-origin indices into the XPath's
    * hierarchy of document elements.
    */
    ArrayList citationIndices(String citationTemplate) {
        def templateParts = citationTemplate.split(/\//)
        def citationIndexes = []
        def max = templateParts.size() - 1
        // cwb: Perhaps we only skip the initial run of parts-without-citation values?
        //      That is, skip over the TEI/text/body/ part, but once you have _any_ citation-element,
        //      Grab all the rest. This seems to work better.
        def startCounting = false
        for (i in 1..max) {

            if (templateParts[i] =~ /\?/) {
                startCounting = true
            }
            if (startCounting){ 
                citationIndexes.add(i)
            }
        }
        return citationIndexes
    }

    /** Converts an XPath expression for the ancestors of a node
    * to the opening XML markup of that node in the XML serialization of 
    * a citable node of text, limited to a specified level of the
    * citation hierarchy.
    * @param ancestorPath A String specifying an XPath with citation values 
    * for a citable node of a document.
    * @param citationTemplate An XPath String with placeholders for citation 
    * values marked by question marks.
    * @returns A String with the XML of the opening element markup
    * of ancestor elements given in xpTemplate.
    * @limit Number of levels of the citation scheme that the resulting 
    * XML should be limited to.
    * @throws Exception if limit is greater than the number of levels
    * of the citation scheme, or if limit is negative.
    */
    String openAncestors (String ancestorPath, String citationTemplate, int limit) 
    throws Exception {
	StringBuffer formatted = new StringBuffer()
        def citationIndexes = citationIndices(citationTemplate)
        // validate limit parameter:
        if (limit == citationIndexes.size() ) {
            // valid request:  but needs no wrapper, just the
            // citation node
            return "" 

        } else if (limit == 0) {
            // use whole ancestor path:
            return openAncestors(ancestorPath)

        } else  if (limit > citationIndexes.size() -1) {
            // invalid: requested limit > number of citation elements!
            throw new Exception("XmlFormatter exception:  citation template has smaller size (${citationIndexes.size()}) than requested limit (${limit})")

        } else if (limit < 0) {
            throw new Exception("XmlFormatter exception:  requested limit (${limit}) cannot be negative.")
        }

        def limitIndex = citationIndexes[limit-1]
        // The citation template includes the leaf node, which we want to skip,
        // so the maximum index of the last element to look it will *not*
        // be size() - 1, but size() - 2
        def pathMax = citationIndexes[citationIndexes.size() - 2]
	def pathParts = ancestorPath.split(/\//)
        for (i in pathMax .. limitIndex) {
            formatted.insert(0, "<" + filtersToAttrs(pathParts[i]) + ">")
        }
        return formatted.toString()
    }



    /**
    * @param ancestorPath An XPath expression representing the full,
    * explicit path of the nodeset's ancestors.
    * @returns A String with the XML of the closing element markup
    * of ancestor elements given in ancestorPath
    */
    String closeAncestors (String ancestorPath) {
	StringBuffer formatted = new StringBuffer()
	//println "AP: ${ancestorPath}"

	def pathParts = ancestorPath.split(/\//)
	pathParts.reverse().each {
	    if (it != "") {formatted.append("</" + stripFilters(it) + ">")}
	}
	return formatted.toString()
   }


    String closeAncestors (String ancestorPath, String citationTemplate, int limit) 
    throws Exception {
	StringBuffer formatted = new StringBuffer()
        def citationIndexes = citationIndices(citationTemplate)
        // validate limit parameter:
        if (limit == citationIndexes.size() ) {
            // valid request:  but needs no wrapper, just the
            // citation node
            return "" 

        } else if (limit == 0) {
            // use whole ancestor path:
            return closeAncestors(ancestorPath)

        } else  if (limit > citationIndexes.size() -1) {
            // invalid: requested limit > number of citation elements!
            throw new Exception("XmlFormatter exception:  citation template has smaller size (${citationIndexes.size()}) than requested limit (${limit})")

        } else if (limit < 0) {
            throw new Exception("XmlFormatter exception:  requested limit (${limit}) cannot be negative.")
        }

        def limitIndex = citationIndexes[limit-1]
        // The citation template includes the leaf node, which we want to skip,
        // so the maximum index of the last element to look it will *not*
        // be size() - 1, but size() - 2
        def pathMax = citationIndexes[citationIndexes.size() - 2]
	def pathParts = ancestorPath.split(/\//)
        for (i in pathMax..limitIndex) {
            formatted.append( "</" + stripFilters(pathParts[i]) + ">")
        }

	return formatted.toString()
    }





    /**
    * Converts XPath filter expressions to attribute tags for an XML document. 
    * E.g., an expression like "div[@n = '1']" becomes "div n='1'".
    * @param xpStr The XPath expression to convert.
    * @returns A String with XML corresponding to
    * the submitted XPath expression.
    */
    String  filtersToAttrs(String xpStr) {
	def returnStr = xpStr
	def emptyStr = ""
	// Hairy reg.exp. to pluck element name and bracketed filter
	// expressions from an XPath expression:
	return xpStr.replaceAll(/([^\[]+)\[(.+)\]/) { wholePattern, elName, filter ->
	    def attrs = filter.replaceAll(/@/,emptyStr)
	    attrs = attrs.replaceAll(" and ", " ")
	    returnStr = "${elName} ${attrs}"
	}
	return returnStr
    }	
    

    /** Removes all filter expressions from an XPath String.
    * @param xpStr An XPath expression, as a String, from which 
    * to remove all filter expressions.
    * @returns An XPath String with no filter expressions.
    */
    String stripFilters(String xpStr) {
	def emptyStr = ""
	return xpStr.replaceAll(/\[[^\]]+\]/, emptyStr)
    }



}
