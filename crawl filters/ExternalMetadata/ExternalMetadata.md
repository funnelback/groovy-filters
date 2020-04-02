# Filter: External metadata

NOTE: This filter is a beta state - use at your own risk as it is not fully tested.

## Purpose

This filter can be used to add external metadata to documents while you are filtering. This filter is more flexible than the existing external metadata system and supports matching of URLs via several methods and supports application of multiple external metadata rules to a single document.

## Installation

The filter should be copied to the collection's `@groovy/com/funnelback/filter` folder

## Usage

To use the filter as part of the collection's update add `com.funnelback.filter.ExternalMetadata` to the `filter.classes` in `collection.cfg`.  

Additional configuration is required to define the external metadata. 

## Configuration file

### external-metadata.json

External metadata rules are defined in the `external-metadata.json` file that should be created in the collection's configuration folder.

The format of the file matches the example below (also included in the git repository as `external-metadata.json`, which shows three external metadata rules defined using the three different match types.

```
[
  {
    "name":"All docs",
    "description":"Add this metadata to all documents",
    "patternType":"REGEX_PATTERN",
    "pattern":".*",
    "metadata":{
        "author":"John Smith",
        "date":"2019"
    }
  },
  {
    "name":"Publications",
    "description":"Add this metadata to urls beginning with http://example.com/publications",
    "patternType":"LEFT_MATCH",
    "pattern":"http://example.com/publications",
    "metadata":{
        "type":"publications",
        "department":"Example department"
    }
  },
  {
    "name":"Media pages",
    "description":"Add this metadata to urls containing /media/",
    "patternType":"SUBSTRING",
    "pattern":"/media/",
    "metadata":{
        "type":"media"
    }
  }
]
```

The JSON fields are:

* **name**: This is a name to assign to the rule.
* **description**: This is a description to assign to the rule (optional).
* **patternType**: This defines the type of match that will be applied to the document's URL. Acceptable values are: 
  * `REGEX_PATTERN`: The URL must match (case insensitively) the `pattern`, expressed as a Java regular expression.
  * `LEFT_MATCH`: The URL must start with the `pattern`.
  * `SUBSTRING`: The URL must include the `pattern` somewhere in the URL.
* **pattern**: The URL is compared with this value using the method defined by **patternType**.
* **metadata**: If the URL matches the pattern then the metadata listed as a set of key value pairs is attached to the document.

## Debugging

The configuration file must contain valid JSON and values (such as the regex patterns) must be appropriate Java regex patterns that are then JSON escaped. e.g. a Java regex pattern of `.+\.pdf` must be added as `.+\\.pdf`

The filter will output information to the collection's filter logs (`crawler.central.log` for a web collection) detailing what metadata has been added for a URL and which rule was applied.

