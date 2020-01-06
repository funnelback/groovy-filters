# Groovy filters

This repository contains reusable filters that can be added to a Funnelback collection to extend the filtering.

# Crawl filters

Crawl filters can be added to the main filter chain (filter.classes) and operate on whole documents as they are filtered during the gather phase.

See: [Developing custom filters](https://docs.funnelback.com/develop/programming-options/document-filtering/index.html)

## Included crawl filters

* **SplitXml:** Splits XML files into separate documents based on xPath(s)
* **KGMetadata:** Configure the metadata fields / XML elements to use for the knowledge graph (KG) node metadata via collection.cfg.  The filter clones the values to the KG metadata fields leaving the original fields free to be mapped as normal.

# Jsoup filters

Jsoup filters can be added to the Jsoup filter chain (filter.jsoup.classes).

Jsoup filters are used to transform HTML documents by operating on a Jsoup object representing the HTML structure.

See: [Jsoup filters](https://docs.funnelback.com/develop/programming-options/document-filtering/jsoup-filters.html)

## Included Jsoup filters

* **Metadata delimiters:** Replace delimiters in specified metadata fields.

### Content auditing filters

* **Check content:** Rule-based content checking filter.
* **Detect language:** Detect the language by analyzing the content.
* **Social tags:** Detect and extra Twitter hash tags and user mentions in content.