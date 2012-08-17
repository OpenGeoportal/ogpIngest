<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${!ajaxRequest}">
<%@include file="jspf/header.jspf"%>
<title>OGP Ingest | Barton Records Ingest</title>
</head>
<body>

<%@include file="jspf/navbar.jspf"%>
</c:if>
	<div id="fileuploadContent">
<h3>Barton Records Ingest</h3>
<p>
Use this page to ingest Barton Records into Solr.
</p>

		<form id="fileuploadForm" action="ingestBarton" method="POST" class="cleanform">
			<div class="header">
		  		<h2>Ingest Barton Records.</h2>
			</div>
			 <button type="submit" id="uploadButton" class="btn btn-primary start">
                <i class="icon-upload icon-white"></i>
                    <span>Start ingest</span>
             </button>
			<div id="loader"></div>
			<div id="status"></div>
		</form>
		
		
		<script type="text/javascript">
			$(document).ready(function() {
				$("#bartonNav").addClass("active");
				$('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#file"));
				pollIngestStatus = function(jobId){
					jQuery.getJSON("ingestStatus?jobId=" + jobId, function(data){
						var success = data.successes;
						if (success.length > 0){
							//see if the success div exists
							if (jQuery("#message").length == 0){
								//create the error div
								jQuery("#status").append('<div id="message" class="success"><h4>Ingest Succeeded:</h4></div>');
							}
							jQuery("#message").html("<h4>Ingest Succeeded:</h4>");
							for (var i in success){
								jQuery("#message").append('<span>' + success[i].layer + '</span><br/>');
							}
						}
						var warnings = data.warnings;
						if (warnings.length > 0){
							//see if the warning div exists
							if (jQuery("#warnings").length == 0){
								//create the error div
								jQuery("#status").append('<div id="warnings" class="warning"><h4>Ingest Warnings:</h4><table></table></div>');
							}
							jQuery("#warnings table").html("");
							for (var i in warnings){
								jQuery("#warnings table").append('<tr><td><span>' + warnings[i].layer + '</span></td><td><span>' + warnings[i].status + '</span></td></tr>');
							}
						}
						var errors = data.errors;
						if (errors.length > 0){
							//see if the error div exists
							if (jQuery("#errors").length == 0){
								//create the error div
								jQuery("#status").append('<div id="errors" class="error"><h4>Ingest Failed:</h4><table></table></div>');
							}
							jQuery("#errors table").html("");
							for (var i in errors){
								jQuery("#errors table").append('<tr><td><span>' + errors[i].layer + '</span></td><td><span>' + errors[i].status + '</span></td></tr>');
							}
						}
						currentStatus = data.jobStatus;
						if (currentStatus == "Processing"){
							var t=setTimeout(function(){pollIngestStatus(jobId);},10000);

						} else {
							$("#loader").html("");
							jQuery("#status").prepend("<span>Finished!</span>");
						}

					});
				};
				$("#uploadButton").click(function(event){
					event.preventDefault();
					var loaderUrl = "<c:url value="/resources/media/loader.gif"/>";
					$("#loader").html('<img src="' + loaderUrl + '" alt="loading..."/>');
					//console.log($('#fileuploadForm').serialize());
					jQuery.ajax(
						{
						type: 'POST',
						url:"ingestBarton",
						dataType: 'json',
						//data: jQuery('#fileuploadForm').serialize(),
						success : function(data){
							pollIngestStatus(data.jobId);
						}
					});
					return false;
				});
			});
		
	</script>
	</div>
	<c:if test="${!ajaxRequest}">
<%@include file="jspf/footer.jspf"%>

</c:if>
