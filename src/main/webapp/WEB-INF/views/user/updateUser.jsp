<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout>
	<jsp:attribute name="title">
		Update User
	</jsp:attribute>
	<jsp:body>
		<form id="updateUserForm" action="/coop/user/update/${user.id}" method="post" onsubmit="return checkDuplicate()">			
			<div class="form-group" style="margin: 1%">
				<label for="firstName">First name</label>
				<input type="firstName" class="form-control" value="${user.getFirstName()}" name="firstName" id="firstName" placeholder="First Name">
			</div>
			<div class="form-group" style="margin: 1%">
				<label for="lastName">Last name</label>
				<input type="lastName" class="form-control" value="${user.getLastName()}" name="lastName" id="lastName" placeholder="Last Name">
			</div>
			<div class="form-group" style="margin:1%">
				<label for="username">Username</label>
				<input type="username" class="form-control" value="${user.getUsername()}" name="username" id="username" placeholder="Username">
			</div>
			<div class="form-group" style="margin:1%">
				<label for="password">Password</label>
				<input type="password" class="form-control" id="password" name="password" placeholder="Password">
			</div>
			<div class="form-group" style="margin: 1%">
				<label for="verifyPassword">Verify Password</label> <input type="password" class="form-control" id="verifyPassword" name="verifyPassword" placeholder="Re-Enter Password">
			</div>
			<div style="margin:1%">
				<button class="btn submit-btn" type="submit">Update User</button>
				<button class="btn btn-secondary" type="button" onclick="window.location.href='<c:url value="/user/${user.getId()}"/>'">Cancel</button>
			</div>
		</form>
		<script src="<c:url value='/resources/js/user.js'/>"></script>
	</jsp:body>
</t:layout>