package org.OpenGeoPortal.Ingest.Metadata;

import java.util.Hashtable;

import org.OpenGeoPortal.Layer.Metadata;
import org.w3c.dom.Document;


/**
 * parser for ISO 19139/19115
 * reads ISO metadata file to internal hashtable representation
 * 
 * this code isn't complete but works well enough to run searches for ISO layers
 * 
 * @author smcdon08
 *
 */
public class IsoParseMethod extends AbstractXmlMetadataParseMethod implements MetadataParseMethod
{	
	/**
	 * from the gist data, I've only seen vector and grid values for MD_SpatialRepresentationTypeCode
	 * this function isn't complete 
	 * @throws Exception 
	 */
	protected void handleDataType(Document document)
	{
		String solrType = "Raster";   // default value
		String dataType = null;

			try {
				dataType = getDocumentValue("MD_SpatialRepresentationTypeCode");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		if (dataType != null)
		{
			if (dataType.equalsIgnoreCase("grid"))
				dataType = "Raster";
			else if (dataType.equalsIgnoreCase("tin"))
				dataType = "Polygon";
			else if (dataType.equalsIgnoreCase("vector"))
				dataType = "Line";
			if (dataType.length() > 0)
				solrType = dataType;
		}
		//layerValues.put(Key.DataType, solrType);		
	}
	
	/*public String keyToTag(Key key)
	{
		if (key == Key.Title)
			return "title";
		else if (key == Key.Abstract)
			return "abstract";
		else if (key == Key.WestBc)
			return "westBoundLongitude";
		else if (key == Key.EastBc)
			return "eastBoundLongitude";
		else if (key == Key.NorthBc)
			return "northBoundLatitude";
		else if (key == Key.SouthBc)
			return "southBoundLatitude";
		else if (key == Key.EsriName)
			return "fileIdentifier";
		else if (key == Key.Publisher)
			return "publish";
		else if (key == Key.Originator)
			return "origin";
		else if (key == Key.ThemeKeywords)
			return "themekey";
		else if (key == Key.PlaceKeywords)
			return "placekey";
		else 
		{
			System.out.println("error in FgdcToSolr.keyToFgdcTag, unexpected key = " + key);
			return "error";
		}
		
	}*/

	@Override
	void handleOriginator() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handlePublisher() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleLayerName() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleAbstract() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleTitle() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleDate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleDataType() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleAccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleKeywords() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleBounds() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void handleFullText() {
		// TODO Auto-generated method stub
		
	}

}
