<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout>
	<jsp:attribute name="title">
		Add User
	</jsp:attribute>
	<jsp:body>
		<form action="/coop/user/add" method="post" id="addUserForm" onsubmit="return checkDuplicate()">
			<div class="form-group" style="margin: 1%">
				<label for="firstName">First name</label> <input type="firstName" class="col-lg-12 form-control" name="firstName" id="firstName" placeholder="First Name">
			</div>
			<div class="form-group" style="margin: 1%">
				<label for="lastName">Last name</label> <input type="lastName" class="col-lg-12 form-control" name="lastName" id="lastName" placeholder="Last Name">
			</div>
			<div class="form-group" style="margin: 1%">
				<label for="username">Username</label> <input type="username" class="col-lg-12 form-control" name="username" id="username" placeholder="Username">
			</div>
			<div class="form-group" style="margin: 1%">
				<label for="password">Password</label> <input type="password" class="form-control" id="password" name="password" placeholder="Password">
			</div>
			<div class="form-group" style="margin: 1%">
				<label for="verifyPassword">Verify Password</label> <input type="password" class="form-control" id="verifyPassword" name="verifyPassword" placeholder="Re-Enter Password">
			</div>
			<div style="margin: 1%">
				<button class="btn submit-btn" type="submit">Add User</button>
				<button class="btn btn-secondary" type="button" onclick="window.location.href='<c:url value="/"/>'">Cancel</button>
			</div>
		</form>
		<script src="<c:url value='/resources/js/user.js'/>"></script>
	</jsp:body>
</t:layout>