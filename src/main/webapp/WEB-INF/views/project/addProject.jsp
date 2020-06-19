<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Add New Project
	</jsp:attribute>
	<jsp:body>
		<link rel="stylesheet" href='<c:url value="/resources/css/multiSelectBox.css"/>' type="text/css"/>
		<form name="addProjectForm"  method="post" onSubmit="return addProjectValidate();">
			<table>
				<tr>
					<td>
						<div class="form-group">
							<label>Project Name:</label>
							<input type="text" class="form-control" name="projName" maxlength="30">
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div class="form-group">
							<label>Repository Host:</label>
							<select id="repositoryHostId" name="repositoryHostId" class="form-control">
								<option value="">Choose a repository host...</option>
								<c:forEach var="repositoryHost" items="${repositories}">
									<option value="${repositoryHost.id}">${repositoryHost.name}</option>
								</c:forEach>
							</select>
							<a href="/coop/repository/add">Setup new repository host</a>
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div class="form-goup">
							<label>Repository Project:</label>
							<select id="repositoryProjectUrl" name="repositoryProjectUrl" class="form-control">
								<option value="">Choose a repository project...</option>
							</select>

						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div class="form-group">
							<label>Users</label>
							<input type="hidden" name="users" value=""/>
							<table class="form-control multiSelectBox">
								<tr>
									<th>Username</th>
									<th>Owner</th>
						          </tr>
					          	<tr id="${user.getId()}" class="multiSelectBoxItem selectedItem">
					          		<td>${user.getUsername()}</td>
					          		<td><input type="checkbox" name="owners" value="${user.getId()}" onclick="return false;" checked/></td>
					          	</tr>
								<c:set var="userNum" value="2"/>
					          	<c:forEach var="otherUser" items="${users}">
								<tr id="${otherUser.getId()}" class="multiSelectBoxItem <c:if test="${userNum ge 4}">hiddenItem</c:if>">
					          		<td>${otherUser.getUsername()}</td>
					          		<td><input type="checkbox" name="owners" value="${otherUser.getId()}"/></td>
					          	</tr>
								<c:set var="userNum" value="${userNum+1}"/>
								</c:forEach>
								<c:if test="${userNum ge 4}">
									<tr>
										<td colspan="2" id="exp-col-btn">&#x25BC;</td>
									</tr>
								</c:if>
							</table>
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<input type="submit" class="btn submit-btn" value="Create">
						<button class="btn btn-secondary" type="button" onclick="window.location.href='<c:url value="/user/${user.getId()}"/>'">Cancel</button>
					</td>
				</tr>
			</table>
		</form>
		<!-- multiSelectBox.js should be referenced before page js -->
		<script src="<c:url value='/resources/js/multiSelectBox.js'/>"></script>
		<script src="<c:url value='/resources/js/project.js'/>"></script>
	</jsp:body>
</t:layout>