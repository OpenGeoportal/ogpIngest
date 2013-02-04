<%@include file="jspf/header.jspf"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<title>OGP Ingest | Direct To Solr</title>
</head>
<body>

<%@include file="jspf/navbar.jspf"%>

	<div id="fileuploadContent">
<h3>Direct To Solr</h3>
<p>
Use this page to upload a record directly to Solr from the form.
</p>

		<form:form id="solrform" action="directToSolr" modelAttribute="metadata" method="POST" class="cleanform">
			<div class="header">
			<!-- 
	BoundingBox bounds;
	List<ThemeKeywords> themeKeywords;
	List<PlaceKeywords> placeKeywords;
	String fullText;
	private String contentDate;
			 -->
		  		<h4></h4>
			</div>
		 		<form:label path="title">
		  			Title: <form:errors path="title" cssClass="error" />
		 		</form:label>
		  		<form:input path="title" />
		  		
		  		<form:label path="description">
		  			Abstract: <form:errors path="description" cssClass="error" />
		 		</form:label>
		  		<form:input path="description" />
		  		
		  		<form:label path="owsName">
		  			Layer Name: <form:errors path="owsName" cssClass="error" />
		 		</form:label>
		 	 	<form:input path="owsName" />
		 	 	
		  		<form:label path="workspaceName">
		  			Workspace Name: <form:errors path="workspaceName" cssClass="error" />
		 		</form:label>
		  		<form:input path="workspaceName" />

		  		<form:label path="originator">
		  			Originator: <form:errors path="originator" cssClass="error" />
		 		</form:label>
		  		<form:input path="originator" />
		  		
		  		<form:label path="publisher">
		  			Publisher: <form:errors path="publisher" cssClass="error" />
		 		</form:label>
		  		<form:input path="publisher" />
		  		
		  		<form:label path="location">
		  			Location: <form:errors path="location" cssClass="error" />
		 		</form:label>
		  		<form:input path="location" />
		  		
		  		<form:label path="themeKeywords[0]">
		  			Theme Keywords: <form:errors path="themeKeywords[0]" cssClass="error" />
		 		</form:label>
		  		<form:input path="themeKeywords[0]" />
		  		
		  		<form:label path="placeKeywords[0]">
		  			Place Keywords: <form:errors path="placeKeywords[0]" cssClass="error" />
		 		</form:label>
		  		<form:input path="placeKeywords[0]" />
		  		
		  		<form:label path="bounds">
		  			Bounds: <form:errors path="bounds" cssClass="error" />
		 		</form:label>
		  		<form:input path="bounds" />
		  		
		  		<form:label path="contentDate">
		  			Content Date: <form:errors path="contentDate" cssClass="error" />
		 		</form:label>
		  		<form:input path="contentDate" />
		  		
		  		<form:label path="fullText">
		  			Full Text: <form:errors path="fullText" cssClass="error" />
		 		</form:label>
		  		<form:input path="fullText" />
		  		
				<form:label path="institution">
					Institution (select one)
				</form:label>
				<form:select path="institution">
					<form:option value="Tufts">Tufts</form:option>
					<form:option value="Harvard">Harvard</form:option>
					<form:option value="MIT">MIT</form:option>
					<form:option value="MassGIS">MassGIS</form:option>
					<form:option value="Berkeley">Berkeley</form:option>
				</form:select>
				<form:label path="access">
					Access Level (select one)
				</form:label>
				<form:select path="access">
					<form:option value="public">public</form:option>
					<form:option value="restricted">restricted</form:option>
				</form:select>
				
				<form:label path="geometryType">
					Geometry Type
				</form:label>
				<form:select path="geometryType">
					<form:option value="point">point</form:option>
					<form:option value="line">line</form:option>
					<form:option value="polygon">polygon</form:option>
					<form:option value="raster">raster</form:option>
					<form:option value="papermap">papermap</form:option>
				</form:select>
				
				<label><form:checkbox path="georeferenced" value="true" />Georeferenced</label>
		  		
			<!-- <div class="control-group">
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
			-->
        <!-- The loading indicator is shown during file processing -->
        <div class="fileupload-loading"></div>
        <br>
        <!-- The table listing the files available for upload/download -->
        <table role="presentation" class="table table-striped"><tbody class="files" data-toggle="modal-gallery" data-target="#modal-gallery"></tbody></table>
			<div id="status"></div>
						<p><button type="submit">Submit</button></p>
		</form:form>
		
		
		<script type="text/javascript">
		//var jobId;
		var loaderUrl = '<c:url value="/resources/media/loader.gif"/>';
			$(document).ready(function() {
				$('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#file"));
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

