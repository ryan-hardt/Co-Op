<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="title" %>
<%@ attribute name="pageButtons" %>
<%@ attribute name="success" %>
<%@ attribute name="error" %>

<!DOCTYPE html>
<html>
<head>
	<title>Co-Op | ${title}</title>

	<meta name="viewport" content="width=device-width, initial-scale=1">
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js" integrity="sha512-894YE6QWD5I59HgZOGReFYm4dnWc1Qt5NtvYSaNcOP+u1T9qYdvdihz0PPSiiqn/+/3e7Jo4EaG7TubfWGUrMQ==" crossorigin="anonymous"></script>

	<!-- Bootstrap core CSS-->
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" integrity="sha384-B0vP5xmATw1+K9KRQjQERJvTumQW0nPEzvF6L/Z6nronJ3oUOFUFpCjEUQouq2+l" crossorigin="anonymous">

	<!-- Custom fonts for this template-->
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" integrity="sha512-SfTiTlX6kk+qitfevl/7LibUOeJWlt9rbyDn92a1DqWOw9vWG2MFoays0sgObmWazO5BQPiFucnnEAjpAB+/Sw==" crossorigin="anonymous" />

	<!-- Custom styles for this template-->
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/startbootstrap-sb-admin/5.1.1/css/sb-admin.min.css" integrity="sha512-MMPoraQHjUkh3iTYGMIvYskg+UiP4MqZ9vu3Ui/u8OT8qn43cSR5J8lOb5Rr61ede0tEZLt/+JkMohB0JQswfQ==" crossorigin="anonymous" />

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
        <div class="page-title">
        	${title}
        	<span class="page-btns">${pageButtons}</span>
        </div>
        <div id="body">
            <jsp:doBody />
        </div>
	</div>

	<!-- Bootstrap core JavaScript-->
	<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js" integrity="sha384-Piv4xVNRyMGpqkS2by6br4gNJ7DXjqk09RmUpJ8jgGtD7zP9yug3goQfGII0yAns" crossorigin="anonymous"></script>

	<!-- Core plugin JavaScript-->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-easing/1.4.1/jquery.easing.min.js" integrity="sha512-0QbL0ph8Tc8g5bLhfVzSqxe9GERORsKhIn1IrpxDAgUsbBGz/V7iSav2zzW325XGd1OMLdL4UiqRJj702IeqnQ==" crossorigin="anonymous"></script>

	<!-- Custom scripts for all pages-->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/startbootstrap-sb-admin/5.1.1/js/sb-admin.min.js" integrity="sha512-RlJ1J/p+lyZbn+ABpSvCerrec/S3iaYPVdd6Lvnn9smqM2G0iEJuuTLuTORk1WriCdnnHADYuqw2b181ncN3sQ==" crossorigin="anonymous"></script></body>
</body>
</html>