<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout>
	<jsp:attribute name="title">
		Login
	</jsp:attribute>
	<jsp:body>
		<div>
			<form action="/coop/login" method ="post">
				<div class="form-group">
					<label for="username">Username</label>
					<input autofocus type="username" class="form-control" name="username" id="username" placeholder="Username" required>
				</div>
				<div class="form-group">
					<label for="password">Password</label> 
					<input type="password" class="form-control" id="password" name="password" placeholder="Password" required>
				</div>
				<input type="submit" value="Login" class="btn submit-btn">
				<a href="/coop/user/add" class="btn other-btn">Register</a>	
			</form>
		</div>
	</jsp:body>
</t:layout>
