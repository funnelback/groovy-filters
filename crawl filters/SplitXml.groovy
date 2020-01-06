package com.funnelback.filter;

@Grapes([
  @Grab(group='us.codecraft', module='xsoup', version='0.3.1')
])

import java.net.URI;
import java.util.*;
import com.funnelback.filter.api.*;
import com.funnelback.filter.api.documents.*;
import com.funnelback.filter.api.filters.*;

/*
import org.junit.*;
import org.junit.Test;
import com.funnelback.filter.api.mock.*;*/

// Jsoup imports
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

// XML imports
import org.jsoup.parser.Parser
import us.codecraft.xsoup.Xsoup;

/*
 * Filter to split an XML file. See accompanying SplitXml.md for information on usage.
 */


@groovy.util.logging.Log4j2
public class SplitXml implements StringDocumentFilter {

    // Object holding the XML split configuration
    def splitRules = [:]
    def defaultXPath

    public SplitXml(File searchHome, String collectionName) {
        // Constructor to read config

        // Read the split configuration
        def splitConfigFile = new File(searchHome.getAbsolutePath()+"/conf/"+collectionName+"/filter.splitxml.cfg")

        if (splitConfigFile.exists()) {
            splitConfigFile.readLines().each {
                def splitRule = it.split("\t")
                if (splitRule.size() == 2) {
                    splitRules[splitRule[0]]=splitRule[1]
                }
            }
        }

    }


    @Override
    public PreFilterCheck canFilter(NoContentDocument document, FilterContext context) {
        //Only filter XML documents.
        if(document.getDocumentType().isXML()){
            defaultXPath = context.getConfigValue("filter.splitXml.defaultSplitPath").orElse("")
            log.debug("Setting default split pattern to: "+defaultXPath)
            if ((splitRules[document.getURI().toString()] != null) || (defaultXPath != "")) {
                return PreFilterCheck.ATTEMPT_FILTER;
            }
            else {
                log.info ("Skipping as no matching rule was found")
                return PreFilterCheck.SKIP_FILTER;
            }
        }
        return PreFilterCheck.SKIP_FILTER;
    }

    @Override
    public FilterResult filterAsStringDocument(StringDocument document, FilterContext context) {

        def filterName="SplitXmlFilter"
        List<StringDocument> documents = new ArrayList<>();

        // Read config from collection.cfg / set default values
        def splitPath
        if (splitRules[document.getURI().toString()] != null) {
            splitPath = splitRules[document.getURI().toString()]
            log.error ("Splitting "+document.getURI().toString()+" with xPath: "+splitRules[document.getURI().toString()])
        }
        else if (defaultXPath != "") {
            splitPath = defaultXPath
            log.error ("Splitting "+document.getURI().toString()+" with default xPath: "+defaultXPath)
        }
        else {
            log.error("Skipping "+document.getURI().toString()+" as a matching Xml Split rule could not be found")
            return FilterResult.of(document);
        }


        try {


            // Parse the document content as a JSOUP object and clone the metadata values indicated by the jsoup selectors.
            Document xmlDoc=Jsoup.parse(document.getContentAsString(), "", Parser.xmlParser())

            // Counter for URL generation
            def i = 0;

            Xsoup.compile(splitPath).evaluate(xmlDoc).getElements().each { record ->

                def xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"+record.outerHtml()
                log.debug("Extracted record: "+xmlString)

                // Create a URL for the document
                def newUrl = document.getURI().toString() + "/" + (i++);
                log.info("Creating document with URL: " + newUrl);

                //Create a new document with the new url and the XML node
                StringDocument newDocument = document.cloneWithURI(URI.create(newUrl)).cloneWithStringContent(DocumentType.MIME_XML_TEXT, xmlString);
            
                // Had to clone an empty mock document otherwise the cloned document seems to contain html
                // StringDocument newDocument = MockDocuments.mockEmptyStringDoc().cloneWithURI(URI.create(newUrl)).cloneWithStringContent(DocumentType.MIME_XML_TEXT, xmlString);
                documents.add(newDocument);
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }

        //Return all the documents we created.
        return FilterResult.of(documents);
    }
}        