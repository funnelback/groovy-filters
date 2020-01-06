# Jsoup filter: Metadata delimiters

## Purpose

This filter can be used to replace the delimiters used in metadata fields on a per-field basis.

The delimiters are replaced with the Funnelback standard delimiter (a vertical bar character).

## Installation

The filter should be copied to the collection's @groovy/jsoup/filter folder

## Usage

To use the filter as part of the collection's update add ```filter.jsoup.MetadataDelimiters``` to the jsoup.filter.classes in collection.cfg

e.g. adding to the default set of Jsoup filters:

```
filter.jsoup.classes=ContentGeneratorUrlDetection,FleschKincaidGradeLevel,UndesirableText,filter.jsoup.MetadataDelimiters
```

## Configuration options

The following can be set in collection.cfg

### filter.metadataDelimiters.&lt;METADATA_FIELD_NAME&gt;=&lt;CHARACTER_TO_REPLACE&gt;

**Description:** Defines the metadata field and delimiter to replace

**Example:** 

```
# E.g <meta name="keywords" content="spain,france,portugal" />
filter.metadataDelimiters.metadata.keywords=,
# E.g <meta name="article:tag" content="flamingo;africa" />
filter.metadataDelimiters.metadata.article:tag=;
```

### filter.metadataDelimiters.separator=&lt;DELIMINTER_TO_USE&gt;

**Description:** Defines the character to use when replacing the delimiter.  This should generally not be changed.

**Default value:**
```
filter.metadataDelimiters.separator=|
```

**Example:**

Replace delimiters with a semicolon.

```
filter.metadataDelimiters.separator=;
```

### filter.metadataDelimiters.setting.&lt;METADATA_FIELD_NAME&gt;=&lt;NAME_OF_META_ATTRIBUTE_USED_FOR_NAME&gt;

**Description:** Defines the attribute to use for identifying the field.  This is required when the attribute used for the metadata field is not name.

**Example:** 

```
# E.g <meta property="og:subjects" content="pan-look;padre;" />
# Notice Open Graph schema uses 'property' and not 'name' as the identifier.
filter.metadataDelimiters.metadata.og:subjects=;
filter.metadataDelimiters.setting.og:subjects.prop=property
```