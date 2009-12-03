package jinngine.scientific;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigurationMetaInfo {
	
	private String title;          // for use in captions and text
	private String abbreviation;   // for use in plots
	private String description;    //for use in plot captions

	public ConfigurationMetaInfo(String filename) {
		//load configuration
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new File(filename), new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName,
						String name, Attributes attributes) throws SAXException {

					if (name.equals("configurationmeta")) {
						 title = attributes.getValue("title");
						 abbreviation = attributes.getValue("abbreviation");
						 description = attributes.getValue("description");
					}
				
				}
			} );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getTitle() {
		return title;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getDescription() {
		return description;
	}
	
	
	
}
