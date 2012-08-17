<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${!ajaxRequest}">
<html>
<head>
<title>OGP Ingest | Metadata Upload</title>
<link href="<c:url value="/resources/form.css" />" rel="stylesheet"
	type="text/css" />
<script type="text/javascript"
	src="<c:url value="/resources/jquery/1.6/jquery.js" />"></script>
<script type="text/javascript"
	src="<c:url value="/resources/jqueryform/2.8/jquery.form.js" />"></script>
</head>
<body>
<img src="<c:url value="/resources/media/mitlogo.png"/>" alt="MIT GeoWeb"/>
<div id="leftColumn">
<span>Options</span><br/>
<a href="ingestBarton">Ingest Map Records</a><br/>
<a href="ingestExternalSolr">Ingest Records from Remote Solr Instance</a><br/>
<a href="solrDelete">Delete Solr Layers</a><br/>
<a href="adjustMetadata">Preprocess Metadata</a><br/>
</div>

<div id="rightColumn">

</c:if>
	<div id="fileuploadContent">
<h3>Metadata Ingest</h3>
<p>
Use this page to ingest FGDC metadata files into GeoServer (Local layers only) and Solr.
</p>

		<form id="fileuploadForm" action="metadataUpload" method="POST" enctype="multipart/form-data" class="cleanform">
			<div class="header">
		  		<h2>Select XML metadata file(s) or zipped directory of XML metadata files.</h2>
			</div>
				<label for="institution">
					Institution (select one)
				</label>
				<select id="institution" name="institution">
					<option value="MIT">MIT</option>
					<option value="Harvard">Harvard</option>
					<option value="Tufts">Tufts</option>
					<option value="MassGIS">MassGIS</option>
					<option value="Berkeley">Berkeley</option>
				</select>
				
			<fieldset class="radio">
				<label><input type="radio" name="ingestOption" value="both" CHECKED />Ingest to GeoServer and Solr</label>
				<label><input type="radio" name="ingestOption" value="geoServerOnly" />Ingest to GeoServer only</label>
				<label><input type="radio" name="ingestOption" value="solrOnly" />Ingest to Solr only</label>
			</fieldset>
			
			<label for="file">Metadata File(s)</label>
			<input id="file" type="file" multiple="" name="fgdcFile[]" />
			<button id="uploadButton" type="submit">Upload</button>
			<div id="loader"></div>
			<c:if test="${not empty success}">
				<div id="message" class="success">
					<h4>Ingest Succeeded:</h4>
					<c:forEach var="successMessage" items="${success}">
							<span>${successMessage.layer}</span><br/>		  		
      				</c:forEach>
      			</div>
		  	</c:if>
		  	<c:if test="${not empty warning}">
				<div id="warnings" class="warning">
					<h4>Ingest Warnings:</h4>
					<table>
						<c:forEach var="warningMessage" items="${warning}">
							<tr><td><span>${warningMessage.layer}</span></td><td><span>${warningMessage.status}</span></td></tr>		  		
      					</c:forEach>
      				</table>
				</div>	  		
		  	</c:if>
		  	<c:if test="${not empty error}">
				<div id="errors" class="error">
					<h4>Ingest Failed:</h4>
					<table>
					<c:forEach var="errorMessage" items="${error}">
							<tr><td><span>${errorMessage.layer}</span></td><td><span>${errorMessage.status}</span></td></tr>		  		
      				</c:forEach>
      				</table>
				</div>	  		
		  	</c:if>
		</form>
		
		
		<script type="text/javascript">
			$(document).ready(function() {
				$('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#file"));
				$("#uploadButton").click(function(){
					var loaderUrl = "<c:url value="/resources/media/loader.gif"/>";
					$("#loader").html('<img src="' + loaderUrl + '" alt="loading..."/>');
					});
				$("#fileuploadForm").ajaxForm({ success:function(html) {
						$("#fileuploadContent").replaceWith(html);
					},
					complete:function() {$("#loader").html("");}
				});
			});
		
	</script>
	</div>
	<c:if test="${!ajaxRequest}">
</div>

</body>
	</html>
</c:if>
