
OpenGeoportal Ingest
Overview

OpenGeoportal Ingest (ogpIngest) is a set of tools designed to populate the Solr index that feeds search for the OpenGeoportal (OGP).  
Tools
Upload Metadata

One of the most common ingest actions is to ingest layer-level geospatial XML metadata documents.  ogpIngest currently supports ingest of FGDC and ISO 19115/19139 metadata, either as individual files or as zipped bundles.
 
Selecting an Institution on the form does several things.  Most obviously, it sets the value for the Institution field for the resulting documents in the OGP Solr index.  This value is not readily discoverable from metadata documents, so it must be set manually.  Setting this value also tells ogpIngest which values to read from the config file (ingest.properties), which may contain information such as the WorkspaceName, guidelines on how to parse layer names from the metadata, and default values for online Location.

If the selected Institution is ‘local’ (also set in ingest.properties), the user has the option to attempt to register the layer with a GeoServer instance.  To accomplish this, ogpIngest will first parse a layer name from the metadata document (or the document’s file name, which is a common convention), then prefix the name with a qualifier, if set (if your geospatial data is stored in a DBMS, layer names may look something like “GISPORTAL.GISOWNER01.YOUR_LAYER_NAME”).  Using the GeoServer REST API, ogpIngest will look for the layer in the GeoServer datastore specified in ingest.properties.  Separate GeoServer datastores, at separate urls if desired, may be registered for ‘public’ and ‘restricted’ layers.  If there is a match with your layer, a request is made to configure the layer in GeoServer if it has not already been configured.  Processing stops on failure of this process so that layers without working web services are not searchable in OGP.

The Required fields selection allows the user to require which fields must be present, and where possible, “valid” for the metadata to be ingested into the Solr index.  If a field is not “required” by the user, detected problems will be presented in the report as “Warnings”, rather than “Failures”, and the metadata will be ingested.

Ingest Records From Remote Solr
 
This tool retrieves records from a remote OGP Solr index and copies them into the local one (set in ingest.properties).  This allows an OGP implementer to populate a local instance with records from other institutions running OGP quickly and easily.  It is also a useful tool for populating development instances.

Selecting Institution is equivalent to querying the remote instance with “Institution:Your_Selection”.  The Remote Solr Url should be of the form: http://hostname/solr.

Delete Solr Layers
 
Enter a list of OGP layer ids to remove those layers from your local Solr instance.  Wild cards are in effect, so be careful! (ex. “Tufts.*” will delete all Tufts layers from your instance).


Preprocess Metadata

The Preprocess Metadata tool performs some rudimentary batch editing of FGDC XML metadata documents.

Access Constraints and Use Constraints allow you to upload a plain text file with the new values or simply type them into the text area.  		

FGDC fields modified:

The Access select box prepends the accconst field with some standard text that allows ogpIngest’s metadata parser to determine if the layer is ‘public’ or ‘restricted’.

Behind the scenes, the contact info under the distrib and metc fields is replaced with the contact info set in  ingest.properties.

Metadata files can be added one at a time, or as a zipped bundle.  Once complete, a zipped bundle of modified metadata is returned. 

 

While performing modifications, the metadata is also audited using the same error-checking logic from “Upload Metadata” so that issues with the metadata can be caught before ingest is attempted.


Configuration
ingest.properties

The value for local.institution should be the name of your institution as it appears in the solr index in the Institution field, lowercased.



local.solrUrl should be the url of the Solr index you are ingesting to.  If your solr index has multiple cores, you need to specify which to ingest to. local.ogpUrl is currently unused.

The following block of properties contains info to allow configuration of layers with a GeoServer instance (Local layers only.  See Metadata Upload.).  ogpIngest will attempt to configure public layers using the local.public properties and restricted layers with the local.restricted properties.  Note that the GeoServer datastore expected to contain the layers must be specified.

This block of properties contains contact info to insert into FGDC XML metadata via the Preprocess Metadata tool.


These properties are also for the Preprocess Metadata tool and determine how the layer name should be processed.  The FGDC tag ftname is altered depending on these values.


The following properties are necessary for each institution whose metadata you intend to ingest directly (via the Upload Metadata tool).  

•	*.layerPrefix prepends the layer name parsed from the metadata.  Often, metadata documents will not include the fully qualified name for the layer.  
•	*.GeoServerWorkspace is the GeoServer workspace the layer should be assigned to. This is the value that goes into the Solr field WorkspaceName.  Omitting this value results in a blank value for this field.
•	*.workspaceLogic allows for some very basic logic to be applied to determine the GeoServer workspace.  It’s recommended that you use the same workspace for all of your layers.
•	*.layerNameCase tells what case convention the layer name should adhere to.  This is frequently incorrect in the metadata.




These properties provide default location values for web services, in case they are not provided in the metadata.




security-app-context.xml

A complete examination of Spring Security configuration is beyond the scope of this document, but here are some basics. 

Default username/password: ogpAdmin/koala

By default, all pages require a user with role “ROLE_ADMIN”.  This can easily be changed through configuration.

Configuring ogpIngest to use LDAP is straightforward. See the Spring Security docs for other authentication types.

The included role mapper looks for the value of admins in ingest.properties.  The value should be a comma separated list of users with permission to access the application.  The role mapper can be easily modified to query a database of users or to use LDAP groups.


Technical Overview

OpenGeoportal Ingest is a Java web application that utilizes the Spring MVC framework and Spring Security. Currently, XML is parsed by walking the DOM via the W3C DOM API for speed and flexibility. Jackson is used for JSON parsing, SolrJ for interfacing with Solr and Apache HTTPComponents is used for HTTP connections. jQuery and Twitter Bootstap are used on the web client.

The general mechanism of ogpIngest is to gather layer-level geospatial metadata of disparate types (FGDC and ISO 19115/19139 XML documents, WMS GetCapabilities documents, remote OGP instances, library records, etc.), translate these to a common schema, then perform some validation on the resulting fields, which are finally ingested into a local Solr index. This allows an OGP instance to then search the records.  Issues and results are reported back to the user as they occur.

With form submission, an IngestStatus object with a jobId (UUID) is created and registered with the IngestStatusManager.  The jobId is returned to the web client so that it can poll the IngestStatusController for information about the ingest job.  At the same time, a new thread is spawned to handle the particular ingest job, which is passed a reference to the IngestStatus and arguments from the web form submitted by the user.  As messages (“Success”, “Warning”, “Fail”) are generated, they are added to the IngestStatus object.  Once ingest is complete, a summary of info messages can be viewed in a spreadsheet report.

Generally, the first stage of the process is to parse incoming metadata to populate an intermediary Metadata object.  The Metadata object contains fields relevant to the OGP Solr schema.  Typically it is populated with some combination of data from the metadata itself and information supplied in ingest.properties. The purpose of this intermediate object is twofold. An intermediate object provides some level of decoupling from the Solr schema, since it is likely to change over time.  Additionally fields can be strongly typed as deemed convenient, while Solr schemas are somewhat loosely typed.

If parsing is successful, the process continues with SolrIngest.  SolrIngest performs some validation on the Metadata fields (including enforcement of Required Fields) and converts the Metadata object into a SolrRecord object which can be directly ingested to a Solr instance via the SolrJ library.  Since all parsing processes result in a Metadata object, the same SolrIngest can be shared for each type of ingest process.


-------------------------------------------------------

ISO 19115/19139 parsing in ogpIngest:

Title: “gmd:title”,
Abstract: “gmd:abstract”,
LayerName: “gmd:fileIdentifier",
Bounds:
<gmd:extent>
        <gmd:EX_Extent>
          <gmd:geographicElement>
            <gmd:EX_GeographicBoundingBox>

MinX: “westBoundLongitude",
MaxX: “eastBoundLongitude",
MaxY: “northBoundLatitude",
MinY: "southBoundLatitude”,


Access: 
 <gmd:resourceConstraints>
        <gmd:MD_LegalConstraints>
          <gmd:accessConstraints>
            <gmd:MD_RestrictionCode codeListValue="" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode" />

          </gmd:accessConstraints>

If the codeListValue starts with “restricted”, the record is marked “Restricted”.  Otherwise it is marked “Public”.


Date:
comes from DateTime:
            <gmd:CI_Date>
              <gmd:date>
                <gco:DateTime>2005-03-03T09:00:00</gco:DateTime>

              </gmd:date>

Keywords:
1. look at the node: “descriptiveKeywords”
2. look for MD_Keywords —> keyword
3. look at the associated MD_KeywordTypeCode.  If it is “place” the keyword is added to PlaceKeywords. otherwise it is a ThemeKeyword


DataType:  
1: look at CI_PresentationFormCode, attribute: codeListValue
		switch (codeListValue) {
        	case imageDigital: 
        		dataType = GeometryType.Raster;
        		break;
        	case mapDigital: 
        		dataType = // we parse a different field to get specific vector values
        		break;
        	case imageHardcopy: 
        	case mapHardcopy:
        		dataType = GeometryType.PaperMap;
        		break;
        	case documentDigital:
        	case documentHardcopy:
        		dataType = GeometryType.LibraryRecord;
        	default: 
        		//we don't know what to do with all these dataTypes right now.
        		dataType = GeometryType.Undefined;	
        		break;

2: if DataType results in GeometryType.Undefined, 
				String rawDataType = getAttributeValue("MD_SpatialRepresentationTypeCode", "codeListValue");
				if (rawDataType.equalsIgnoreCase("vector")){
					geomType = GeometryType.Polygon;

								}

3: if DataType is “mapDigital”, we look first for: MD_SpatialRepresentationTypeCode
			if (dataType.equalsIgnoreCase("grid"))
				geomType = GeometryType.Raster;
			else if (dataType.equalsIgnoreCase("tin"))
				geomType = GeometryType.Polygon;
			else if (dataType.equalsIgnoreCase("vector")){
				geomType = resolveVectorToGeometryType();
			} else 
				geomType = GeometryType.Undefined;
	


	If we don’t find it, we look for “MD_TopologyLevelCode”:
		else if (xmlTag.equals("MD_TopologyLevelCode")){
			if (dataType.equalsIgnoreCase("geometryOnly")){
				geomType = resolveVectorToGeometryType();
			}

4: to further resolve the vector, we look first for MD_GeometricObjectTypeCode.  If we don’t find it: MI_GeometryTypeCode:
	
	protected GeometryType convertMD_GeometricObjectTypeCodeToGeometryType(ISO_MD_GeometricObjectTypeCode codeValue) throws Exception{
		GeometryType geomType = GeometryType.Undefined;
		switch (codeValue) {
        	case complex: 
        	case composite:
        	case surface:
        		geomType = GeometryType.Polygon;
        		break;
        	case curve: 
        		geomType = GeometryType.Line;
        		break;
        	case point: 
        		geomType = GeometryType.Point;
        		break;
        	default: 
        		geomType = GeometryType.Undefined;	
        		break;
		}

		return geomType;
	}		

	protected GeometryType convertMI_GeometryTypeCodeToGeometryType(ISO_MI_GeometryTypeCode codeValue) throws Exception{
		GeometryType geomType = GeometryType.Undefined;
		switch (codeValue) {
        	case areal:
        	case strip:
        		geomType = GeometryType.Polygon;
        		break;
        	case linear: 
        		geomType = GeometryType.Line;
        		break;
        	case point: 
        		geomType = GeometryType.Point;
        		break;
        	default: 
        		geomType = GeometryType.Undefined;	
        		break;
		}

		return geomType;

				}

For Originator and Publisher, we examine all of the CI_RoleCode values in CI_ResponsibleParty nodes and look at the role codes.
For Originator:
	we look first for “originator”, then “author”, then “principalInvestigator”, then “owner"
For Publisher:
	we look first for “publisher”, then “distributor”, then “resourceProvider”, then “custodian”, then “processor”

for the chosen CI_ResponsibleParty nodes, we take the first of “organisationName”, “individualName”, or “positionName"




