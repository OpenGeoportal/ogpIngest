<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:if test="${!ajaxRequest}">
<%@include file="jspf/header.jspf"%>
<title>OGP Ingest | Delete From Solr</title>
</head>
<body>

<%@include file="jspf/navbar.jspf"%>

</c:if>
	<div id="solrDeleteContent">

<h3>Solr Delete</h3>
<p>
Use this page to delete layers from the Solr index.
</p>

		<form id="solrDeleteForm" action="solrDelete" method="POST" class="cleanform">
			<div class="header">
		  		<h2>Enter Layer IDs</h2>
			</div>
				<textarea id="layerIds" name="layerIds" rows="5" cols="40"></textarea>
							 <button type="submit" id="uploadButton" class="btn btn-danger">
                <i class="icon-trash icon-white"></i>
                    <span>Delete</span>
             </button>

			<c:if test="${not empty message}">
				<div id="message" class="success">
					<h4>Delete Succeeded:</h4>
					<c:forEach var="successMessage" items="${message}">
							<span>${successMessage}</span><br/>		  		
      				</c:forEach>
      			</div>
		  	</c:if>
		  	<c:if test="${not empty error}">
				<div id="errors" class="error">
					<h4>Delete Failed:</h4>
					<c:forEach var="errorMessage" items="${error}">
							<span>${errorMessage}</span><br/>		  		
      				</c:forEach>
				</div>	  		
		  	</c:if>
		</form>
		
		
		<script type="text/javascript">
			$(document).ready(function() {
				$("#deleteNav").addClass("active");
				$('<input type="hidden" name="ajaxUpload" value="true" />').insertAfter($("#layerIds"));
				$("#solrDeleteForm").ajaxForm({ success:function(html) {
						$("#solrDeleteContent").replaceWith(html);
					}
				});
			});
		
	</script>
	</div>
	<c:if test="${!ajaxRequest}">
<%@include file="jspf/footer.jspf"%>

</c:if>
