<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${!ajaxRequest}">
<%@include file="jspf/header.jspf"%>
<title>OGP Ingest | Form to Solr</title>
</head>
<body>

<%@include file="jspf/navbar.jspf"%>

</c:if>
	<div id="fileuploadContent">
<h3>Form to Solr</h3>
<p>
Use this page to add a record to your solr index directly from a form.
</p>


			<!-- 

			@RequestParam("themeKeywords") List<String> themeKeywords, @RequestParam("placeKeywords") List<String> placeKeywords, 
			@RequestParam("location") Set<String> location)
				BoundingBox bounds;
	String id;
	String title;
	String description;
	String owsName;
	String workspaceName;
	Set<LocationLink> location;
	String originator;
	List<ThemeKeywords> themeKeywords;
	List<PlaceKeywords> placeKeywords;
	String institution;
	String fullText;
	AccessLevel access;
	GeometryType geometryType;
	private String publisher;
	private Boolean georeferenced;
	private String contentDate;
			 -->
		<form id="fileupload" action="formToSolr" method="POST" enctype="multipart/form-data" class="cleanform">
			 
			 <label for="layerId">
				LayerId
			</label>
			<input type="text" id="layerId" name="layerId" value="${parsedValues.id}"/>

			<label for="workspaceName">
				Workspace Name
			</label>
			<input type="text" id="workspaceName" name="workspaceName" value="${parsedValues.workspaceName}" />
			
			<label for="owsName">
				OWS Name
			</label>
			<input type="text" id="owsName" name="owsName" value="${parsedValues.owsName}"/>
			
						
			<label for="title">
				Title
			</label>
			<input type="text" id="title" name="title" value="${parsedValues.title}" />
			
			<label for="institution">
				Institution
			</label>
			<input type="text" id="institution" name="institution" value="${parsedValues.institution}" />
			
			<label for="originator">
				Originator
			</label>
			<input type="text" id="originator" name="originator" value="${parsedValues.originator}" />
			
			<label for="publisher">
				Publisher
			</label>
			<input type="text" id="publisher" name="publisher" value="${parsedValues.publisher}" />
			
			<label for="abstract">
				Abstract
			</label>
			<div>
			    <textarea name="abstract" id="abstract">
			    ${parsedValues.description}
				</textarea>
			</div>
			
			<label for="location">
				Online Location
			</label>
			<input type="text" id="location" name="location" value="${parsedValues.location}" /> 
			
			<label for="bounds">
				Bounds
			</label>
			<input type="text" id="bounds" name="bounds" value="${parsedValues.bounds}" />
			
			
			<label for="date">
				Content Date
			</label>
			<input type="text" id="date" name="date" value="${parsedValues.contentDate}" />

			<label for="geometryType">
				Geometry Type
			</label>
			<select id="geometryType" name="geometryType">
					<option value="Point" ${parsedValues.geometryType == 'Point' ? 'selected' : ''}>Point</option>
					<option value="Line" ${parsedValues.geometryType == 'Line' ? 'selected' : ''}>Line</option>
					<option value="Polygon" ${parsedValues.geometryType == 'Polygon' ? 'selected' : ''}>Polygon</option>
					<option value="Raster" ${parsedValues.geometryType == 'Raster' ? 'selected' : ''}>Raster</option>
					<option value="ScannedMap" ${parsedValues.geometryType == 'ScannedMap' ? 'selected' : ''}>Scanned Map</option>
					<option value="Undefined" ${parsedValues.geometryType == 'Undefined' ? 'selected' : ''}>Unspecified Vector</option>
					<option value="PaperMap" ${parsedValues.geometryType == 'PaperMap' ? 'selected' : ''}>Paper Map</option>
					<option value="LibraryRecord" ${parsedValues.geometryType == 'LibraryRecord' ? 'selected' : ''}>Library Record</option>
			</select>			
			
			 <label for="access">
				Access
			</label>
			<select id="access" name="access">
					<option value="Public" ${parsedValues.access == 'Public' ? 'selected' : ''}>public</option>
					<option value="Restricted" ${parsedValues.access == 'Restricted' ? 'selected' : ''}>restricted</option>
			</select>

			<label for="georeferenced">
				Georeferenced
			</label>
			<select id="georeferenced" name="georeferenced" >
					<option value="true" ${parsedValues.georeferenced == 'true' ? 'selected' : ''}>true</option>
					<option value="false" ${parsedValues.georeferenced == 'false' ? 'selected' : ''}>false</option>
			</select>
			
			<label for="toSolr">
				to Solr?
			</label>
			<input type="checkbox" id="toSolr" name="toSolr" value="true" />
		
		<label for="file">Metadata File</label>
        <div class="row fileupload-buttonbar">
            <div class="span4">
                <!-- The fileinput-button span is used to style the file input field as button -->
                <span class="btn btn-success fileinput-button">
                    <i class="icon-plus icon-white"></i>
                    <span>Add file...</span>
                    <input type="file" id="file" name="metadataFile">
                </span>
                <input type="submit" id="submitButton" class="btn btn-primary start" />
                   <!--  <i class="icon-upload icon-white"></i>
                    <span>Process Metadata</span>
                </input>-->
            </div>
            <!-- The global progress information -->
            <div class="span4 fileupload-progress fade">
                <!-- The global progress bar -->
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                    <div class="bar" style="width:0%;"></div>
                </div>
                <!-- The extended global progress information -->
                <!-- <div class="progress-extended">&nbsp;</div>-->
            </div>
        </div>
        <!-- The loading indicator is shown during file processing -->
        <div class="fileupload-loading"></div>
        <br>
        <!-- The table listing the files available for upload/download -->
        <table role="presentation" class="table table-striped"><tbody class="files" data-toggle="modal-gallery" data-target="#modal-gallery"></tbody></table>
			<div id="status"></div>
		</form>
		
		
		<script type="text/javascript">
		var loaderUrl = '<c:url value="/resources/media/loader.gif"/>';
		
		
			$(document).ready(function() {
				$("#preprocessNav").addClass("active");
				/*$('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#file"));
				
			    // Initialize the jQuery File Upload widget:
			    $('#fileupload').fileupload();

			    // Enable iframe cross-domain access via redirect option:
			    $('#fileupload').fileupload(
			        'option',
			        'redirect',
			        window.location.href.replace(
			            /\/[^\/]*$/,
			            '/cors/result.html?%s'
			        )
			    );

		        $('#fileupload').fileupload('option', {
		            singleFileUploads: true,
		            url: 'formToSolr',
		            acceptFileTypes: /(\.|\/)(xml)$/i,
		            dataFilter: function(data, type){
		            	//extract jobId info and return the object fileupload plugin accepts
		            	data = jQuery.parseJSON(data);
		            	jQuery("#status").empty();
						//now, return should be jobid
						//use the jobid to make a second request to the IngestStatusManager
						//poll every ? seconds until "succeeded" or "failed"
							//we can get status messages and percent progress from IngestStatusManager
		            		//pollIngestStatus(data.jobId);
						console.log(data);
						//populate boxes with data
		            	return JSON.stringify(data);
		            }
		        });*/
			});
		
	</script>
	</div>
	<!-- The template to display files available for upload -->
<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-upload fade">
        <td class="preview"><span class="fade"></span></td>
        <td class="name"><span>{%=file.name%}</span></td>
        <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
        {% if (file.error) { %}
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else if (o.files.valid && !i) { %}
            <td>
                <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
            </td>
            <td class="start">{% if (!o.options.autoUpload) { %}
                <button class="btn btn-primary">
                    <i class="icon-upload icon-white"></i>
                    <span>{%=locale.fileupload.start%}</span>
                </button>
            {% } %}</td>
        {% } else { %}
            <td colspan="2"></td>
        {% } %}
        <td class="cancel">{% if (!i) { %}
            <button class="btn btn-warning">
                <i class="icon-ban-circle icon-white"></i>
                <span>{%=locale.fileupload.cancel%}</span>
            </button>
        {% } %}</td>
    </tr>
{% } %}
</script>
<!-- The template to display files available for download -->
<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-download fade processing">
        {% if (file.error) { %}
            <td></td>
            <td class="name"><span>{%=file.name%}</span></td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
        {% } else { %}
            <td class="preview">{% if (file.type) { %}
                <span>{%=file.type%}</span>
            {% } %}</td>
            <td class="name">
                <span>{%=file.name%}</span>
            </td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td></td>
			<td class="loader"><img src="{%=loaderUrl%}" alt="loading..."/></td>
        {% } %}
    </tr>
{% } %}
</script>
	<c:if test="${!ajaxRequest}">
<%@include file="jspf/footer.jspf"%>

</c:if>
