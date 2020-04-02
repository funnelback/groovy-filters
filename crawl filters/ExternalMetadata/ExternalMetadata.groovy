package com.funnelback.filter;

import java.util.*;
import org.junit.*;
import org.junit.Test;
import com.funnelback.filter.api.*;
import com.funnelback.filter.api.documents.*;
import com.funnelback.filter.api.filters.*;
import com.funnelback.filter.api.mock.*;
import com.google.common.collect.ListMultimap;
import groovy.json.JsonSlurper
import java.util.regex.Pattern

/**
 * Adds external metadata defined in external-metadata.json.
 * 
 * Metadata is added based on a series of rules that are matched against the document's URL. Match can be of three different types: REGEX_PATTERN, LEFT_MATCH or SUBSTRING.
 *
 * Example external-metadata.json showing the three different match types. Name and description fields are optional and don't currently do anything apart
 * from provide comments in the config file.
 * 
 * The REGEX_PATTERN mode support Java regular expressions. The CASE_INSENTIVE flag is set when performing the match.
 * 
 * [ 
 *   {
 *     "name":"All docs",
 *     "description":"Add this metadata to all documents",
 *     "patternType":"REGEX_PATTERN",
 *     "pattern":".*",
 *     "metadata":{
 *         "author":"John Smith",
 *         "date":"2019"
 *     }
 *   },
 *   {
 *     "name":"Publications",
 *     "description":"Add this metadata to all documents",
 *     "patternType":"LEFT_MATCH",
 *     "pattern":"http://example.com/publications",
 *     "metadata":{
 *         "type":"publications",
 *         "department":"Example department"
 *     }
 *   },
 *   {
 *     "name":"Media pages",
 *     "description":"Add this metadata to all documents",
 *     "patternType":"SUBSTRING",
 *     "pattern":"/media/",
 *     "metadata":{
 *         "type":"media"
 *     }
 *   }
 * ]
 *
 */
@groovy.util.logging.Log4j2
public class ExternalMetadata implements Filter {

  // External metadata rules, read in from configuration in the constructor
  def rules

  // Constructor, used to hold things that will be initialised once, but reused for each filtered document
  public ExternalMetadata(File searchHome, String collectionName) {

      // Read the external metadata rules from external-metadata.json
      def rulesFile = new File(searchHome.getAbsolutePath()+"/conf/"+collectionName+"/external-metadata.json")
      rules = new JsonSlurper().parse(rulesFile, 'UTF-8')
  }

  @Override
  public FilterResult filter(FilterableDocument document, FilterContext context) throws RuntimeException,
    FilterException {

    // Get a copy of the existing metadata, 
    // so that we preserve the existing metadata
    ListMultimap<String, String> metadata = document.getCopyOfMetadata();

    // The current document URL
    def url = document.getURI().toString()

    try {
      rules.each() { rule ->
        // Check the URL against each rule
        log.debug("DEBUG: {}",rule.toString())
        if (rule.patternType == "REGEX_PATTERN") {
          // REGEX URL match
          Pattern p = Pattern.compile(rule.pattern.trim(),CASE_INSENSITIVE)
          if (url.matches(p)) {
            log.debug("REGEX matches - '{}':'{}'",rule.pattern.trim(),url);
            rule.metadata.each() { mf,v ->
              metadata.put(mf, v);
              log.info("Adding external metadata - '{}':'{}' rule: '{}'",mf,v,rule.name);
            }
          }
        }
        else if (rule.patternType == "LEFT_MATCH") {
          // Left anchored URL match
          if (url.startsWith(rule.pattern.trim())){
            log.debug("LEFT_MATCH matches - '{}':'{}'",rule.pattern.trim(),url);
            rule.metadata.each() { mf,v ->
              metadata.put(mf, v);
              log.info("Adding external metadata - '{}':'{}' rule: '{}'",mf,v,rule.name);
            }
          }
        }
        else if (rule.patternType == "SUBSTRING") {
          // URL substring match
          if (url.contains(rule.pattern.trim())){
            log.debug("SUBSTRING matches - '{}':'{}'",rule.pattern.trim(),url);
            rule.metadata.each() { mf,v ->
              metadata.put(mf, v);
              log.info("Adding external metadata - '{}':'{}' rule: '{}'",mf,v,rule.name);
            }
          }
        }
      }
    } catch (e) {
        log.error("Error adding external metadata to '{}'", url, e)
    }
   
    // Create a document with the new metadata 
    FilterableDocument filteredDocument = document.cloneWithMetadata(metadata);
    
    return FilterResult.of(filteredDocument);
  }
  
}