# Filter: Split XML

NOTE: This filter is a beta state - use at your own risk as it is not fully tested.

## Purpose

This filter can be used to split XMLs file into separate records.

## Installation

The filter should be copied to the collection's `@groovy/com/funnelback/filter` folder

## Usage

To use the filter as part of the collection's update add `com.funnelback.filter.SplitXml` to the `filter.classes` in `collection.cfg`.  The filter should appear close to the start of the filter chain as the split documents will proceed through the rest of the filter chain.

Additional configuration is required that configures the path(s) used for splitting the XML. 

Ensure that `crawler.max_download_size` and `crawler.max_parse_size` are both set to the same value (10 MB is the default) and increased to be larger than the largest XML file being downloaded.

If no matching split pattern is found the XML file is not modified.

## Split patterns

XML documents are split using a split path defined as a xPath pattern.

for the XML below:

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<libraries>
    <library>
      <books>
        <book></book>
        <book></book>
       </books>
    </library>
    <library>
      <books>
        <book></book>
        <book></book>
       </books>
    </library>
</libraries>
```

A split path of `/libraries/library` would index each `<library>` element as an individual item.  A path of `/libraries/library/books/book` could be used to index the individual books.

## Configuration options

### filter.splitxml.cfg

Split patterns are defined on a per-url basis in the `filter.splitxml.cfg` file that should be created in the collection's configuration folder.

The format of the file is a tab-delimited text file with one split rule per line.

Each line should contain the URL then split pattern separated with a space

e.g. 

```
http://example.com/bookstore.xml    /books/book
http://example.com/staffdirectory.xml   /directory/staff/person
``` 

### Default split pattern

A default split pattern can be defined in the `collection.cfg` by setting the `filter.splitXml.defaultSplitPath` value

e.g.

```
filter.splitXml.defaultSplitPath=/libraries/library/books/book
```

