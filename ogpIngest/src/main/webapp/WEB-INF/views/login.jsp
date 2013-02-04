<%@include file="jspf/simpleheader.jspf"%>
<title>OGP Ingest | Login</title>
</head>
<body>
    <div class="navbar">
    <div class="navbar-inner">
    <div class="container">
	<%@include file="jspf/logo.jspf"%><span class="headerText">Ingest</span>
    </div>
    </div>
    </div>


<body onload='document.f.j_username.focus();'>
<div class="container">
	<h3>Login</h3>
 
	<c:if test="${not empty error}">
		<div class="error">
			Your login attempt was not successful, try again.<br /> Cause :
			${sessionScope["SPRING_SECURITY_LAST_EXCEPTION"].message}
		</div>
	</c:if>
 
	<form name='f' action="<c:url value='j_spring_security_check' />" method='POST'>
		
    <div class="row">
    <div class="span4"><label>User: </label><input type='text' name='j_username' <c:if test="${not empty error}">value='${sessionScope["SPRING_SECURITY_LAST_USERNAME_KEY"]}'</c:if> /></div>
    </div>
    <div class="row">
    <div class="span4"><label>Password: </label><input type='password' name='j_password' /></div>
    </div>    
    <div class="row">
    <div class="span4">				
	<button class="btn btn-primary" type="submit">
        <i class="icon-user icon-white"></i>
		login
	</button>
	<button class="btn btn-warning" type="reset">
        <i class="icon-thumbs-down icon-white"></i>
		reset
	</button>
</div>
    </div>
		



 
	</form>
	</div>
<%@include file="jspf/footer.jspf"%>