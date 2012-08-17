<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${!ajaxRequest}">
<%@include file="jspf/header.jspf"%>
<title>OGP Ingest | Preprocess Metadata</title>
</head>
<body>

<%@include file="jspf/navbar.jspf"%>

</c:if>
	<div id="fileuploadContent">
<h3>Preprocess Metadata</h3>
<p>
Use this page to preprocess FGDC metadata files.  The returned zip file will contain metadata with updated contact info, cleaned up onlink tags,
updated use constaints (if provided), and access constraints.  Additionally, it will perform a metadata audit to alert you to potential problems 
in the ingest process.
</p>
		<form id="fileupload1" action="getText" method="POST" enctype="multipart/form-data" class="getText cleanform">

				<div class="control-group">
			<label for="accessConstraintsFile">Access Constraints File(.txt)</label>
				<div>
			    <textarea>
				</textarea>
				</div> 
				<span class="btn btn-success fileinput-button">
                    <i class="icon-plus icon-white"></i>
                    <span>Add file...</span>
                    <input type="file" id="accessConstraintsFile" name="accessConstraintsFile">
                </span>

                </div>
                </form>
                 <br/>
                <br/>
                <br/>
             <form id="fileupload2" action="getText" method="POST" enctype="multipart/form-data" class="getText cleanform">
                
                <div class="control-group">
			<label for="useConstraintsFile">Use Constraints File(.txt)</label>
				<div>
			    <textarea>
				</textarea>
				</div> 
			    <span class="btn btn-success fileinput-button">
                    <i class="icon-plus icon-white"></i>
                    <span>Add file...</span>
                    <input type="file" id="useConstraintsFile" name="useConstraintsFile">
                </span>
 
                </div>
                </form>
                <br/>
                <br/>
                <br/>
		<form id="fileupload" action="adjustMetadata" method="POST" enctype="multipart/form-data" class="cleanform">
			<div class="header">
		  		<h2>Select XML metadata file(s) or zipped directory of XML metadata files.</h2>
			</div>
			
			<label for="access">
				Access
			</label>
			<select id="access" name="access">
					<option value="public">public</option>
					<option value="restricted">restricted</option>
				</select>

			<label for="file">Metadata File(s)</label>
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
                    <span>Process Metadata</span>
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
		var loaderUrl = '<c:url value="/resources/media/loader.gif"/>';
			$(document).ready(function() {
				$("#preprocessNav").addClass("active");
				$('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#file"));
			    //'use strict';
			    $(".getText").each(function(){
			    	$(this).fileupload();
				    $(this).fileupload(
					        'option',
					        'redirect',
					        window.location.href.replace(
					            /\/[^\/]*$/,
					            '/cors/result.html?%s'
					        )
					    );
				     var that$ = jQuery(this);
				     $(this).fileupload('option', {
				    	 	paramName: "textFile",
				            url: 'getText',
				            acceptFileTypes: /(\.|\/)(txt)$/i,
				            dataFilter: function(data, type){
				            	//extract jobId info and return the object fileupload plugin accepts
				            	data = jQuery.parseJSON(data);
				            	var textArea$ = that$.find("textarea").first();
				            	textArea$.text(data.fileText);
				            	return JSON.stringify(data.fileInfo);
				            }
				     });
				     $(this).find("input").first().bind('change', function (e) {
				    	    that$.fileupload('send', {
				    	        fileInput: $(this)
				    	    });
				     });
				});
				
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
			    var getParams = function(){
			    	var postParams = [];
			    	var accessConstObj = {};
			    	accessConstObj.name = "accessConstraints";
			    	accessConstObj.value = jQuery("#fileupload1").find("textarea").first().val().trim();
			    	postParams.push(accessConstObj);
			    	var useObj = {};
			    	useObj.name = "useConstraints";
			    	useObj.value = jQuery("#fileupload2").find("textarea").first().val().trim();
			    	postParams.push(useObj);
			    	var accessObj = {};
			    	accessObj.name = "access";
			    	accessObj.value = jQuery("#access").val();
			    	postParams.push(accessObj);
			    	return postParams;
			    };
		        $('#fileupload').fileupload('option', {
		            singleFileUploads: false,
		            limitMultiFileUploads: 50,
		            url: 'adjustMetadata',
		            formData: getParams,
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
