package com.funnelback.filter;

@Grapes([
  @Grab(group='us.codecraft', module='xsoup', version='0.3.1')
])

import java.util.*;
import org.junit.*;
import org.junit.Test;
import com.funnelback.filter.api.*;
import com.funnelback.filter.api.documents.*;
import com.funnelback.filter.api.filters.*;
import com.funnelback.filter.api.mock.*;
import com.google.common.collect.ListMultimap;
import static com.funnelback.filter.api.DocumentType.*;

// Jsoup imports
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

// XML imports
import org.jsoup.parser.Parser
import us.codecraft.xsoup.Xsoup;

/**
 * Clones metadata fields specified in collection.cfg to FUNkgNodeNames and FUNkgNodeLabel fields.
 *
 * This allows the existing metadata fields to be mapped normally and remain unaffected by KG
 */
@groovy.util.logging.Log4j2
public class KGMetadata implements StringDocumentFilter {

    // KG meta field names
    public static final String KGLABEL = "FUNkgNodeLabel";
    public static final String KGNAMES = "FUNkgNodeNames";
    public url = "";

    public PreFilterCheck canFilter(NoContentDocument document, FilterContext context) {

        url = document.getURI().getPath();

        // read possible regex to test URLs against.  if set only apply the other rules if it matches this regex
        def kgUrlFilter = context.getConfigValue("kg.urlfilter").orElse("")

        //Run on XML and HTML documents
        if (document.getDocumentType().isHTML() || document.getDocumentType().isXML()) {

            //Check against the kg.urlfilter regex and only filter if there's a match or it's not set.
            if ((kgUrlFilter == "") || (url =~ kgUrlFilter)) {
                log.info("Applying KG metadata to '"+url+"'")
                return PreFilterCheck.ATTEMPT_FILTER;
            }
            else {
                log.info("Skipped '{}' due to URL filter '{}'",url,kgUrlFilter)
                return PreFilterCheck.SKIP_FILTER;
            }
        }
        else {
            return PreFilterCheck.SKIP_FILTER;
        }
    }

    @Override
    public FilterResult filterAsStringDocument(StringDocument document, FilterContext context) throws RuntimeException,
        FilterException {

        // Read an optional Label value to apply to every record in the collection.  This would normally be achieved using external metadata but
        // some URL types are not supported so this is a way to work around that issue.
        def kgLabelAllValue = context.getConfigValue("kg.labels.allValue").orElse("")  


        // The following options allow several items to be specified and combined.  
        // Each item is delimited with the following 3 character string: #,#
        // Each item is made up of one or more concatenated field components determined by the field path, or by a double quoted static value.  
        //   The components are delimited with the following 3 character string: #+#
        //   e.g. kg.names.meta=firstname#+#" "#+#lastname#,#lastname#+#", "#+#firstname will emit two FUNkgNodeNames values of 'firstname lastname' and 'lastname, firstname'

        // Read comma separated list of HTML metadata field names to extract as KG metadata for HTML documents
        def kgLabelMetaFields = context.getConfigValue("kg.labels.meta").orElse("")
        def kgNamesMetaFields = context.getConfigValue("kg.names.meta").orElse("")

        // Read comma separated list of jsoup selectors to extract contents as FG metadata for HTML documents
        def kgLabelJsoupSelectors = context.getConfigValue("kg.labels.jsoup").orElse("")
        def kgNamesJsoupSelectors = context.getConfigValue("kg.names.jsoup").orElse("")
 
        // Read comma separated list of XPaths to extract as KG metadata for XML documents
        def kgLabelXPaths = context.getConfigValue("kg.labels.xml").orElse("")
        def kgNamesXPaths = context.getConfigValue("kg.names.xml").orElse("")

        // Read comma separated list of Funnelback filter extracted metadata names to clone as KG metadata for all documents
        def kgLabelFilterMeta = context.getConfigValue("kg.labels.filtermeta").orElse("")
        def kgNamesFilterMeta = context.getConfigValue("kg.names.filtermeta").orElse("")

        // Read all collection.cfg keys (required for non-specific metadata field cloning keys)
        def configKeys = context.getConfigKeys();

        //Ensure we get the existing metadata from the document, to preserve existing
        //metadata
        ListMultimap<String, String> metadata = document.getCopyOfMetadata();

        // Apply the All value as metadata if specified
        if (kgLabelAllValue != "") {
            log.info("Added all value from config "+kgLabelAllValue.replaceAll(/^\"|\"$/,"")+" to "+KGLABEL);
            metadata.put(KGLABEL,kgLabelAllValue.replaceAll(/^\"|\"$/,""))
        }

        if (document.getDocumentType().isHTML()) {
            try {
                // Parse the document content as a JSOUP object and clone the metadata values indicated by the jsoup selectors.
                Document doc=Jsoup.parse(document.getContentAsString())

                // Clone each metadata field indicated by the selectors. Support both content and property attributes.
                if (kgLabelMetaFields != "") {
                    kgLabelMetaFields.split('#,#').each { item ->
                        cloneHtmlMetaFields(doc, item, KGLABEL, metadata)
                    }
                }
                if (kgNamesMetaFields != "") {   
                    kgNamesMetaFields.split('#,#').each { item ->
                        cloneHtmlMetaFields(doc, item, KGNAMES, metadata)
                    }
                }

                // Extract the text from specified jsoup selectors as KG metadata.
                if (kgLabelJsoupSelectors != "") {                       
                    kgLabelJsoupSelectors.split('#,#').each { item ->
                        cloneJsoupSelectors(doc, item, KGLABEL, metadata)                        
                    } 
                }
                if (kgNamesJsoupSelectors != "") {                       
                    kgNamesJsoupSelectors.split('#,#').each { item ->
                        cloneJsoupSelectors(doc, item, KGNAMES, metadata)    
                    }
                }

                // Clone other fields for KG - other fields to clone have config names of 
                // kg.property.<property-name>=<metadata-field-name>
                // This will clone the <metadata-field-name> to a metadata class of kg.property.<property-name>
                // You then need to map these fields using the metadata mapping interface.

                configKeys.each { key ->
                    if (key.startsWith("kg.property.")) {
                        def value = context.getConfigValue(key).orElse("")
                        cloneHtmlMetaFields(doc, value, key, metadata)                
                    }
                }

            } catch(e) {
                log.error ("Failed extracting KG metadata for "+url+": "+e)
            }
        }
        else if (document.getDocumentType().isXML()) { 
            try {
                // Parse the document content as a JSOUP object and clone the metadata values indicated by the jsoup selectors.
                Document doc=Jsoup.parse(document.getContentAsString(), "", Parser.xmlParser())

                if (kgLabelXPaths != "") {
                    kgLabelXPaths.split('#,#').each { item ->
                        cloneXPaths(doc, item, KGLABEL, metadata)                       }
                }
                if (kgNamesXPaths != "") {
                    kgNamesXPaths.split('#,#').each { item ->
                        cloneXPaths(doc, item, KGNAMES, metadata)   
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Clone any filtered metadata fields as required - this is used to clone items in the document's metadata map (metadata added via filters)
        if (kgLabelFilterMeta != "") {     
            kgLabelFilterMeta.split('#,#').each{ item ->
                cloneFilteredMetaFields(item, KGLABEL, metadata)     
            }
        }
        if (kgNamesFilterMeta != "") {                 
            kgNamesFilterMeta.split('#,#').each{ item ->
                cloneFilteredMetaFields(item, KGNAMES, metadata)     
            }
        }

        return FilterResult.of(document.cloneWithMetadata(metadata));
    }


   // ==================================================================================================

    def cloneHtmlMetaFields(doc, item, metafield, metadata) { 
    // Clone and combine each metadata field indicated by the field names and emit metafield. Support both content and property attributes when extracting.
        log.debug("Processing as html metadata fields: "+item)
        def metafieldVal = ""
        item.split('#\\+#').each { metaName ->
        log.debug("- Processing: "+metaName)
            if (metaName =~ /^\"/) {
                log.debug("Add static value:"+metaName.replaceAll(/^\"|\"$/,""))
                metafieldVal += metaName.replaceAll(/^\"|\"$/,"")
            }
            else {
                doc.select("meta[name="+metaName+"]").each() { field -> 
                    log.debug("Extract HTML value from meta field "+field.outerHtml());
                    if (field.attr("content") != null) { metafieldVal += field.attr("content") } 
                }               
                doc.select("meta[property="+metaName+"]").each() { field -> 
                    log.debug("Extract HTML value from meta field "+field.outerHtml());
                    if (field.attr("content") != null) { metafieldVal += field.attr("content") } 
                }  
            }
        }
        if (metafieldVal != "") {
            log.info("Adding "+metafieldVal+" as "+metafield)
            metadata.put(metafield,metafieldVal)
        }
    }

    def cloneJsoupSelectors(doc, item, metafield, metadata) { 
    // Clone and combine each metadata field indicated by the selectors and emit metafield. 
        log.debug("Processing as Jsoup selectors: "+item)
        def selectorVal = ""
        item.split('#\\+#').each { sel ->
        log.debug("- Processing: "+sel)
            if (sel =~ /^\"/) {
                log.debug("Add static value:"+sel.replaceAll(/^\"|\"$/,""))
                selectorVal += sel.replaceAll(/^\"|\"$/,"")
            }
            else {
                doc.select(sel).each() { field ->
                    log.debug("Extract HTML value from jsoup selector "+field.text());
                    selectorVal += field.text()
                }
            }
        }
        if (selectorVal != "") {
            log.info("Adding "+selectorVal+" as "+metafield)
            metadata.put(metafield,selectorVal)
        }
    }

    def cloneXPaths(doc, item, metafield, metadata) { 
    // Clone and combine each metadata field indicated by the XPaths and emit metafield. 
        log.debug("Processing as XPaths: "+item)
        def xpathVal = ""
        item.split('#\\+#').each { path ->
        log.debug("- Processing: "+path)
            if (path =~ /^\"/) {
                log.debug("Add static value:"+sel.replaceAll(/^\"|\"$/,""))
                xpathVal += path.replaceAll(/^\"|\"$/,"")
            }
            else {
                Xsoup.compile(path).evaluate(doc).getElements().each { field ->
                    log.debug("Extract XML value "+field.text());
                    xpathVal += field.text()                        
                }
            }
        }
        if (xpathVal != "") {        
            log.info("Adding "+xpathVal+" as "+metafield)
            metadata.put(metafield,xpathVal)
        }
    }        

    def cloneFilteredMetaFields(item, metafield, metadata) { 
    // Clone and combine each filtered metadata field and emit metafield.
        log.debug("Processing as filtered metadata: "+item)
        def metaVal = ""
        item.split('#\\+#').each { meta ->
        log.debug("- Processing: "+meta)
            if (meta =~ /^\"/) {
                log.debug("Add static value:"+meta.replaceAll(/^\"|\"$/,""))
                metaVal += meta.replaceAll(/^\"|\"$/,"")
            }
            else {
                if (metadata.containsKey(meta)) {
                    def cloneMetaVal =  metadata.get(meta)
                    cloneMetaVal.each { String val ->    
                        log.debug("Extract filtered metadata field value "+val);                          
                        metaVal += val
                    }
                }
            }
        }    
        if (metaVal != "") {
            log.info("Adding "+metaVal+" as "+metafield);
            metadata.put(metafield, metaVal)
        }
    }           

    // ==================================================================================================
}

