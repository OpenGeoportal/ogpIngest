<%@include file="jspf/header.jspf"%>
<title>OGP Ingest | Metadata Upload</title>
</head>
<body>

<%@include file="jspf/navbar.jspf"%>

	<div id="fileuploadContent">
<h3>Metadata Ingest</h3>
<p>
Use this page to ingest FGDC metadata files into GeoServer (Local layers only) and Solr.
</p>

		<form id="fileupload" action="metadataUpload" method="POST" enctype="multipart/form-data" class="cleanform">
			<div class="header">
		  		<h4></h4>
			</div>

				<%@include file="jspf/institutions.jspf"%>
				
			<div class="control-group">
				<label class="radio inline"><input type="radio" name="ingestOption" value="both" CHECKED />Ingest to GeoServer and Solr</label>
				<label class="radio inline"><input type="radio" name="ingestOption" value="geoServerOnly" />Ingest to GeoServer only</label>
				<label class="radio inline"><input type="radio" name="ingestOption" value="solrOnly" />Ingest to Solr only</label>
				<p class="help-block span10">
					<strong>Note:</strong>
					This option only applies to local layers.  Ingest will not attempt to configure GeoServer for remote layers.
				</p>
			</div>
			<br/>
			<br/>
			<div class="control-group">
			<label class="control-label" for="optionsCheckboxList"><h4>Required fields</h4></label>
				<div class="controls">
					<label class="checkbox inline">
						<input type="checkbox" value="title" name="requiredFields" checked>
						Title
					</label>
					<label class="checkbox inline">
						<input type="checkbox" value="publisher" name="requiredFields" checked>
						Publisher
					</label>
					<label class="checkbox inline">
						<input type="checkbox" value="originator" name="requiredFields" checked>
						Originator
					</label>
					<label class="checkbox inline">
						<input type="checkbox" value="abstract" name="requiredFields" checked>
						Abstract
					</label>
					<label class="checkbox inline">
						<input type="checkbox" value="dataType" name="requiredFields" checked>
						Data Type
					</label>
					<br/>
					<label class="checkbox inline">
						<input type="checkbox" value="themekey" name="requiredFields" checked>
						Theme Keywords
					</label>
					<label class="checkbox inline">
						<input type="checkbox" value="placekey" name="requiredFields" checked>
						Place Keywords
					</label>
					<label class="checkbox inline">
						<input type="checkbox" value="contentdate" name="requiredFields" checked>
						Content Date
					</label>
					<label class="checkbox inline">
						<input type="checkbox" value="bounds" name="requiredFields" checked>
						Bounds
					</label>
					<label class="checkbox inline">
						<input type="checkbox" value="access" name="requiredFields" checked>
						Access
					</label>
					<p class="help-block span10">
						<strong>Note:</strong>
						If you require a field, Solr ingest will fail for a data layer if the metadata does not contain a valid value.  If you <br/>do not require
						the field, a warning will still be produced for invalid values.
					</p>
				</div>
			</div>
			<br/>
			<br/>
			<br/>
			<label for="file"><h4>Select XML metadata file(s) or zipped directory of XML metadata files</h4></label>
			 <!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
        <div class="row fileupload-buttonbar">
            <div class="span4">
                <!-- The fileinput-button span is used to style the file input field as button -->
                <span class="btn btn-success fileinput-button">
                    <i class="icon-plus icon-white"></i>
                    <span>Add files...</span>
                    <input type="file" id="file" name="fgdcFile[]" multiple>
                </span>
                <button type="submit" id="submitButton" class="btn btn-primary start">
                    <i class="icon-upload icon-white"></i>
                    <span>Start ingest</span>
                </button>
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
		//var jobId;
		var loaderUrl = '<c:url value="/resources/media/loader.gif"/>';
			$(document).ready(function() {
				$("#xmluploadNav").addClass("active");
				$('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#file"));
			    'use strict';

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
		            singleFileUploads: false,
		            limitMultiFileUploads: 50,
		            url: 'metadataUpload',
		            acceptFileTypes: /(\.|\/)(xml|zip)$/i,
		            dataFilter: function(data, type){
		            	//extract jobId info and return the object fileupload plugin accepts
		            	data = jQuery.parseJSON(data);
		            	jQuery("#status").empty();
						//now, return should be jobid
						//use the jobid to make a second request to the IngestStatusManager
						//poll every ? seconds until "succeeded" or "failed"
							//we can get status messages and percent progress from IngestStatusManager
		            		pollIngestStatus(data.jobId);
						
		            	return JSON.stringify(data.fileInfo);
		            }
		        });
			});			
	</script>
	</div>
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
<%@include file="jspf/footer.jspf"%>

