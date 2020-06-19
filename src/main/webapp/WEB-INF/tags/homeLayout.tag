<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="title" %>
<%@ attribute name="success" %>
<%@ attribute name="error" %>

<!DOCTYPE html>
<html>
<head>
	<title>Co-Op | ${title}</title>

	<meta name="viewport" content="width=device-width, initial-scale=1">
	<script src="<c:url value='/resources/sb-admin/vendor/jquery/jquery.min.js'/>"></script>

	<!-- Bootstrap core CSS-->
	<link rel="stylesheet" href='<c:url value="/resources/sb-admin/vendor/bootstrap/css/bootstrap.min.css" />' type="text/css" />
	
	<!-- Custom fonts for this template-->
	<link rel="stylesheet" href='<c:url value="/resources/sb-admin/vendor/font-awesome/css/font-awesome.min.css" />' type="text/css" />
	
	<!-- Custom styles for this template-->
	<link rel="stylesheet" href='<c:url value="/resources/sb-admin/css/sb-admin.css"/>' type="text/css" />
	
	<!--  Custom styles for this site -->
	<link rel="stylesheet" href='<c:url value="/resources/css/coop.css"/>' type="text/css" />
	
</head>
<body class="fixed-nav bg-dark" id="page-top">
	<c:import url="/WEB-INF/views/template/navigationFull.jsp" />
	<div class="content-wrapper tile-background">
        <div id="success" class="alert alert-success" <c:if test="${empty success}">style="display:none"</c:if>>
            ${success}
        </div>
        <div id="errors" class="alert alert-danger" <c:if test="${empty error}">style="display:none"</c:if>>
            ${error}
        </div>
        <div id="body">
            <jsp:doBody />
        </div>
	</div>
	
	<!-- Bootstrap core JavaScript-->
	<script src="<c:url value='/resources/sb-admin/vendor/bootstrap/js/bootstrap.bundle.min.js'/>"></script>
	
	<!-- Core plugin JavaScript-->
	<script src="<c:url value='/resources/sb-admin/vendor/jquery-easing/jquery.easing.min.js'/>"></script>
	
	<!-- Custom scripts for all pages-->
	<script src="<c:url value='/resources/sb-admin/js/sb-admin.min.js'/>"></script>
</body>
</html>