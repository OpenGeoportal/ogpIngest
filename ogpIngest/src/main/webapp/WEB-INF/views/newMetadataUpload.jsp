<%@include file="jspf/header.jspf"%>
<title>OGP Ingest | Metadata Upload</title>
<head></head>
<body>

	<%@include file="jspf/navbar.jspf"%>

	<div id="fileuploadContent">
		<h3>Metadata Ingest</h3>
		<p>Use this page to ingest FGDC metadata files into GeoServer
			(Local layers only) and Solr.</p>

		<form:form id="fileupload" action="newMetadataUpload"
			commandName="defaultForm" method="POST" enctype="multipart/form-data"
			class="cleanform">
			<div class="header">
				<h4></h4>
			</div>
			<label for="institution">
				<h4>Institution</h4>
			</label>
			<form:select id="institution" path="institution"
				items="${institutionList}" />

			<div class="control-group">
				<label class="radio inline"><input type="radio"
					name="ingestOption" value="both" CHECKED />Ingest to GeoServer and
					Solr</label> <label class="radio inline"><input type="radio"
					name="ingestOption" value="geoServerOnly" />Ingest to GeoServer
					only</label> <label class="radio inline"><input type="radio"
					name="ingestOption" value="solrOnly" />Ingest to Solr only</label>
				<p class="help-block span10">
					<strong>Note:</strong> This option only applies to local layers.
					Ingest will not attempt to configure GeoServer for remote layers.
				</p>
			</div>
			<br />
			<br />
			<div class="control-group">
				<label class="control-label" for="optionsCheckboxList"><h4>Required
						fields</h4></label>
				<div class="controls">
					<form:checkboxes path="requiredFields"
						items="${metadataElementList}" class="checkbox inline" />
					<br />
					<p class="help-block span10">
						<strong>Note:</strong> If you require a field, Solr ingest will
						fail for a data layer if the metadata does not contain a valid
						value. If you <br />do not require the field, a warning will still
						be produced for invalid values.
					</p>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label"><h4 id="placeKeywordHeader">Place
						Keyword Thesauri Requirements</h4></label>
				<div class="controls placeKeywordPane">
					<div class="requiredKeywords keywordContainer well">
						<div class="hiddenCheckboxes">
							<form:checkboxes path="placeKeywordRequirement.required"
								items="${placeKeywordConstraintList}" />
						</div>
						<h5>Required Thesauri</h5>
						<ul id="placeKeywordRequirements"
							class="sortableRequirements placeKeywordRequirements placeKeywordDrop">
						</ul>
					</div>
					<div class="optionalKeywords keywordContainer well">
						<div class="hiddenCheckboxes">
							<form:checkboxes path="placeKeywordRequirement.optionList"
								items="${placeKeywordConstraintList}" />
						</div>
						<h5>Optional Thesauri</h5>
						<span>required number: </span>
						<form:input path="placeKeywordRequirement.numberOfOptions" />


						<ul id="placeKeywordOptions"
							class="sortableRequirements placeKeywordRequirements placeKeywordDrop">
							<c:forEach var="placeKeyword"
								items="${placeKeywordConstraintList}">

								<li><div class="label keywordItem">
										<span class="icon-th"></span>
										<c:out value="${placeKeyword.value}" />
										<span class="value"><c:out value="${placeKeyword.key}" /></span>
									</div></li>

							</c:forEach>
						</ul>

					</div>
					<!-- <p class="help-block span10">
						<strong>Note:</strong>
						Required Place
					</p>-->
				</div>
			</div>
			<div class="control-group">
				<label class="control-label"><h4 id="themeKeywordHeader">Theme
						Keyword Thesauri Requirements</h4></label>
				<div class="controls themeKeywordPane">
					<div class="requiredKeywords keywordContainer well">
						<h5>Required Thesauri</h5>
						<div class="hiddenCheckboxes">

							<form:checkboxes path="themeKeywordRequirement.required"
								items="${themeKeywordConstraintList}" />
						</div>
						<ul id="themeKeywordRequirements"
							class="sortableRequirements themeKeywordRequirements">
						</ul>
					</div>
					<div class="optionalKeywords keywordContainer well">
						<div class="hiddenCheckboxes">

							<form:checkboxes path="themeKeywordRequirement.optionList"
								items="${themeKeywordConstraintList}" />
						</div>
						<h5>Optional Thesauri</h5>
						<span>required number: </span>
						<form:input path="placeKeywordRequirement.numberOfOptions" />
						<ul id="themeKeywordOptions"
							class="sortableRequirements themeKeywordRequirements">
							<c:forEach var="themeKeyword"
								items="${themeKeywordConstraintList}">

								<li><div class="label keywordItem">
										<span class="icon-th"></span>
										<c:out value="${themeKeyword.value}" />
										<span class="value"><c:out value="${themeKeyword.key}" /></span>
									</div></li>

							</c:forEach>
						</ul>
					</div>
					<!-- <p class="help-block span10">
						<strong>Note:</strong>
						Required Place
					</p>-->
				</div>
			</div>
			<br />
			<br />
			<br />
			<label for="file"><h4>Select XML metadata file(s) or
					zipped directory of XML metadata files</h4></label>
			<!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
			<div class="row fileupload-buttonbar">
				<div class="span4">
					<!-- The fileinput-button span is used to style the file input field as button -->
					<span class="btn btn-success fileinput-button"> <i
						class="icon-plus icon-white"></i> <span>Add files...</span> <input
						type="file" id="file" name="fgdcFile[]" multiple>
					</span>
					<button type="submit" id="submitButton"
						class="btn btn-primary start">
						<i class="icon-upload icon-white"></i> <span>Start ingest</span>
					</button>
				</div>
				<!-- The global progress information -->
				<div class="span4 fileupload-progress fade">
					<!-- The global progress bar -->
					<div class="progress progress-success progress-striped active"
						role="progressbar" aria-valuemin="0" aria-valuemax="100">
						<div class="bar" style="width: 0%;"></div>
					</div>
					<!-- The extended global progress information -->
					<!-- <div class="progress-extended">&nbsp;</div>-->
				</div>
			</div>
			<!-- The loading indicator is shown during file processing -->
			<div class="fileupload-loading"></div>
			<br>
			<!-- The table listing the files available for upload/download -->
			<table role="presentation" class="table table-striped">
				<tbody class="files" data-toggle="modal-gallery"
					data-target="#modal-gallery"></tbody>
			</table>
			<div id="status"></div>
		</form:form>


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

		       jQuery("[name=placeKeywordRequirement\\.required]:checked").each(function(){
					var value = jQuery(this).val();
					jQuery("#placeKeywordOptions li").each(function(){
						if (value == jQuery(this).find(".value").text()){
				    	   	jQuery(this).appendTo("ul#placeKeywordRequirements");
							return;
						}
					});
		    	   });
		       
	    	   //$("#source").appendTo("#destination");
	    	   //find the li element in the options list and append
		       jQuery("[name=themeKeywordRequirement\\.required]:checked").each(function(){
					var value = jQuery(this).val();
					jQuery("#themeKeywordOptions li").each(function(){
						if (value == jQuery(this).find(".value").text()){
				    	   	jQuery(this).appendTo("ul#themeKeywordRequirements");
							return;
						}
					});
		    	});
		       
		       jQuery("#placeKeywordRequirements, #placeKeywordOptions").sortable({
		    	   connectWith: ".placeKeywordRequirements",
		    	   update: function( event, ui ) {
		    		   //if the item has been moved to the other list, update hiddencheckboxes
		    		   if (ui.sender != null){
		    			   //console.log("item moved");
		    			   //console.log(event);
		    			   //console.log(ui);
		    			   var value = ui.item.find(".value").text();
		    			   //console.log(value);
		    			   //console.log(jQuery(event.target).siblings(".hiddenCheckboxes").find("input[value=" + value + "]").first());
		    			   jQuery(event.target).siblings(".hiddenCheckboxes").find("input[value=" + value + "]").attr("checked", "checked");
		    			   ui.sender.siblings(".hiddenCheckboxes").find("input[value=" + value + "]").removeAttr("checked");

		    		   }
		    	   }
		    	   }).disableSelection();
		       jQuery("#themeKeywordRequirements, #themeKeywordOptions").sortable({
		    	   connectWith: ".themeKeywordRequirements",
		    	   update: function( event, ui ) {
		    		   //if the item has been moved to the other list, update hiddencheckboxes
		    		   if (ui.sender != null){
		    			   //console.log("item moved");
		    			   //console.log(event);
		    			   //console.log(ui);
		    			   var value = ui.item.find(".value").text();
		    			   //console.log(value);
		    			   //console.log(jQuery(event.target).siblings(".hiddenCheckboxes").find("input[value=" + value + "]"));
		    			   jQuery(event.target).siblings(".hiddenCheckboxes").find("input[value=" + value + "]").attr("checked", "checked");
		    			   ui.sender.siblings(".hiddenCheckboxes").find("input[value=" + value + "]").removeAttr("checked");
		    		   }
		    	   }
		    	   }).disableSelection();
		       
		       jQuery(".placeKeywordPane,.themeKeywordPane").hide();
		       jQuery("#placeKeywordHeader").on("click", function(){jQuery(".placeKeywordPane").toggle("blind");});
		       jQuery("#themeKeywordHeader").on("click", function(){jQuery(".themeKeywordPane").toggle("blind");})
			});			
	</script>
	</div>
	<div></div>

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

</body>