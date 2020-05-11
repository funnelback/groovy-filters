## KGMetadata filter

## Usage

Install `KGMetadata.groovy` to `@groovy/com/funnelback/filter/`.

Add the following to the filter classes:

```
com.funnelback.filter.KGMetadata
```

### collection.cfg keys

Only run the filter if the URL matches the REGEX pattern. If empty or not set then the filter will apply to all URLs.

```
kg.urlfilter=<REGEX>
```

To extract from HTML `<meta>` tags:

```
kg.labels.meta=<delimited list of metadata field names>
kg.names.meta=<delimited list of metadata field names>
```

To extract the text from HTML based on Jsoup selector:

```
kg.labels.jsoup=<delimited list of jsoup selectors>
kg.names.jsoup=<delimited list of jsoup selectors>
```

To clone a html `<meta>` tag for use with KG (note: unlike the other config keys the metadata field name only supports a single metadata field):

This will clone the `<metadata field name>` to a metadata field `kg.property.<property name>`. You then need to map these fields using the metadata mapping interface.

This is useful for cases where you need a specific field for use in the KG widget but it is already mapped to a metadata class with other fields (like the titles mapped to the t metadata class).

```
kg.property.<property name>=<metadata field name>
```

To extract the text from XML based on X-Path:

```
kg.labels.xml=<delimited list of X-Paths>
kg.names.xml=<delimited list of X-Paths>
```

To clone a value from the filtered metadata multimap:

```
kg.labels.filtermeta=<delimited list of metadata names>
kg.names.filtermeta=<delimited list of metadata names>
```

Each item in the delimited list of items can be combined from a combination of values of the item type (e.g. metadata field values, Jsoup selectors, XPath values etc) and static values.

The delimeter format is as follows:

`#,#` separates the different items to add a metadata values.
`#+#` separates the components of each item.  

The elements that are combined with the `#+#` delimiter are either field paths (metadata field values/Jsoup selectors/XPaths) or static values which are indicated as a double quoted string. Field paths must all be of the same type and match the type indicated by the collection config key.

e.g.

```
kg.names.meta=dc.subject#,#dcterms.subject#,#keyword
kg.names.xml=//fn#+#" "#+#//ln#,#//ln#+#", "#+#//fn
```

Extracted `kg.labels.*` values will appear in the `FUNkgNodeLabel` metadata field. 

Extracted `kg.names.*` values will appear in the `FUNkgNodeNames` metadata field.

Metadata mappings need to be created for `FUNkgNodeLabel`, `FUNkgNodeNames` and any cloned metadata fields as HTTP Header metadata.

Apply a node label (type) to all URLs.  This can be used to work around the URL limitation in external metadata when you wish to apply a value to all records and the URL is an unsupported format (e.g. because it's a fake URL for a non web collection type)

```
kg.labels.allValue=<value to be applied to every item as FUNkgNodeLabel>
```

Metadata mappings must be created in the administration interface to map the cloned metadata so that it is available for Funnelback knowledge graph configuration.

Create the following mappings:

Funnelback 15.20 and newer:

* Metadata class: `FUNkgNodeLabel` mapped from HTML metadata field `FUNkgNodeLabel`
* Metadata class: `FUNkgNodeNames` mapped from HTML metadata field `FUNkgNodeNames`

Funnelback 15.18:

* Metadata class: `FUNfkgNodeLabel` mapped from HTML metadata field `FUNkgNodeLabel`
* Metadata class: `FUNfkgNodeNames` mapped from HTML metadata field `FUNkgNodeNames`

