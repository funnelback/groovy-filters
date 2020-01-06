package filter.jsoup;

/*
	This filter will convert metadata fields defined in the collection.cfg and convert its delimeter character to what is defined in the filter configuration. In this case it is the | (pipe character).

	This will ensure no conflicts happen when using facet_sep_chars and baselines all character delimiters used.

	#collection.cfg settings

	#Minimum metadata configuration
	# Assumes <meta name="keywords" content="pan-look,padre" />
	#filter.metadataDelimiters.<METADATA_FIELD_NAME>=<CHARACTER_TO_REPLACE>
	filter.metadataDelimiters.metadata.keywords=,
	
	#Configurable Settings
	#filter.metadataDelimiters.separator=<DELIMINTER_TO_USE> (defaults to pipe `|` )
	filter.metadataDelimiters.separator=;

	#Advanced metadata configuration 
	# E.g <meta property="og:subjects" content="pan-look;padre;" />
	# Notice Open Graph schema uses 'property' and not 'name' as the id.
	filter.metadataDelimiters.metadata.article:tag=,
	#filter.metadataDelimiters.setting.<METADATA_FIELD_NAME>=<NAME_OF_META_ATTRIBUTE_USED_FOR_NAME>
	filter.metadataDelimiters.setting.article:tag.prop=property


	@author Robert Prib (rprib@funnelback.com)

*/

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.funnelback.common.config.Keys;
import com.funnelback.common.filter.jsoup.IJSoupFilter;
import com.funnelback.common.filter.jsoup.FilterContext;
import com.funnelback.common.filter.jsoup.SetupContext;

// Imports required for logging
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * replaces metadata delimiters as specified in collection.cfg with |
 */
public class MetadataDelimiters implements IJSoupFilter {
	private SetupContext setup;
	private static final Logger logger = LogManager.getLogger(MetadataDelimiters.class);
	static String CONFIG_SEPARATOR_PREFIX = "filter.metadataDelimiters.separator";
	static String CONFIG_PREFIX = "filter.metadataDelimiters.metadata";
	static String CONFIG_SETTING_PREFIX = "filter.metadataDelimiters.setting";
	static String CONFIG_PROP_PREFIX = "prop";
	private String separator = "|";

	public void setup(SetupContext setup) {
		this.setup = setup;
		this.separator = this.setup.getConfigSetting("key.${CONFIG_SEPARATOR_PREFIX}") ?: this.separator;
 	}
	
	public void processDocument(FilterContext context) {
		// get the document as a Jsoup object
		def doc = context.getDocument();
		// logger.error("MetadataDelimiters has begun processing document.")

		// for each collection.cfg setting
		for (String key : this.setup.getConfigKeysWithPrefix( CONFIG_PREFIX ) ) {
			
			
			String metaName = key.replace( "$CONFIG_PREFIX.", "" );
			String metaProp = this.setup.getConfigSetting("$CONFIG_SETTING_PREFIX.$metaName.$CONFIG_PROP_PREFIX") ?: "name";
			String replaceChar =  this.setup.getConfigSetting(key);

			// logger.error("Processing metadata field: ${metaName} prop: ${metaProp} replacechar: ${replaceChar}")
			// println("Processing metadata field: ${metaName} prop: ${metaProp} replacechar: ${replaceChar}");
			processMetadata(doc, metaName, metaProp, replaceChar);
		}
		
	}

	public void processMetadata(def doc,String metaName, String metaProp, String replaceChar){
		String c = doc.select("meta[${metaProp}=${metaName}]").attr("content");
		c = c.replace( replaceChar, this.separator );
		doc.select("meta[${metaProp}=${metaName}]").attr("content", c);
	}
}
