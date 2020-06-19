<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Update Project
	</jsp:attribute>
	<jsp:body>
		<link rel="stylesheet" href='<c:url value="/resources/css/multiSelectBox.css"/>' type="text/css"/>
		<form name="addProjectForm" method="post" onsubmit="return addProjectValidate();">
			<table>
				<tr>
					<td>
						<div class="form-goup">
							<label>Project Name:</label>
							<input type="text" name="projName" maxlength="30" class="form-control" value="${project.getName()}">
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
									<option value="${repositoryHost.id}" <c:if test="${repositoryHost.id eq project.getRepositoryProject().getRepositoryHost().getId()}">selected</c:if>>${repositoryHost.name}</option>
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
								<option value="">Choose a repository host project...</option>
								<c:forEach var="repositoryProject" items="${repositoryProjects}">
									<option value="${repositoryProject.repositoryProjectUrl}" <c:if test="${repositoryProject.repositoryProjectUrl eq project.getRepositoryProject().getRepositoryProjectUrl()}">selected</c:if>>${repositoryProject.name}</option>
								</c:forEach>
							</select>
						</div>
					</td>
				</tr>
				<c:if test="${not empty project.slackWorkspace}">
				<tr id="slackWorkspaceRow">
					<td>
						<div class="form-group">
							<label>Slack Workspace:</label><br/>
							<button class="btn update-btn" type="button" onclick="window.location.href='${slackRegistrationLink}'">Update</button>
							<button id="removeSlackWorkspaceBtn" class="btn btn-danger" type="button" onclick="removeSlackWorkspace(${project.getId()})">Remove</button>
						</div>
					</td>
				</tr>
				</c:if>
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
						          <!--  DISPLAY LOGGED IN USER AS OWNER -->
					          	<tr id="${user.getId()}" class="multiSelectBoxItem selectedItem">
					          		<td>${user.getUsername()}</td>
					          		<td><input type="checkbox" name="owners" value="${user.getId()}" checked/></td>
					          	</tr>
					          	<!--  DISPLAY OTHER OWNERS (LOGGED IN USER REMOVED FROM LIST)-->
					          	<c:forEach var="owner" items="${project.getOwners()}">
					          	<tr id="${owner.getId()}" class="multiSelectBoxItem selectedItem">
					          		<td>${owner.getUsername()}</td>
					          		<td><input type="checkbox" name="owners" value="${owner.getId()}" checked/></td>
					          	</tr>
								</c:forEach>
								<!--  DISPLAY PROJECT USERS (OWNERS REMOVED FROM LIST)-->
								<c:set var="userNum" value="1"/>
								<c:forEach var="projectUser" items="${project.getUsers()}">
								<tr id="${projectUser.getId()}" class="multiSelectBoxItem selectedItem">
					          		<td>${projectUser.getUsername()}</td>
					          		<td><input type="checkbox" name="owners" value="${projectUser.getId()}"/></td>
					          	</tr>
								<c:set var="userNum" value="${userNum+1}"/>
								</c:forEach>
								<!--  DISPLAY ALL OTHER USERS -->
					          	<c:forEach var="otherUser" items="${otherUsers}">
								<tr id="${otherUser.getId()}" class="multiSelectBoxItem <c:if test="${userNum ge 3}">hiddenItem</c:if>">
					          		<td>${otherUser.getUsername()}</td>
					          		<td><input type="checkbox" name="owners" value="${otherUser.getId()}"/></td>
					          	</tr>
								<c:set var="userNum" value="${userNum+1}"/>
								</c:forEach>
								<c:if test="${userNum ge 3}">
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
						<input type="submit" class="btn submit-btn" value="Update">
						<button class="btn btn-secondary" type="button" onclick="window.location.href='<c:url value="/project/${project.getId()}"/>'">Cancel</button>
						<button type="button" class="btn delete-btn" data-toggle="modal" data-target="#deleteModal">Delete Project</button>
			                <div class="modal fade" id="deleteModal" role="dialog">
			                    <div class="modal-dialog">
			                        <div class="modal-content">
			                            <div class="modal-header">
			                                <p class="modal-title" style="font-size: large; font-wieght: bold;">Delete Project</p>
			                                <button type="button" class="close" data-dismiss="modal">&times;</button>
			                            </div>
			                            <div class="modal-body">
			                                Are you sure you want to delete this project? All related boards, cycles, tasks, and task histories will be deleted.
			                            </div>
			                            <div class="modal-footer">
			                                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			                                <button type="button" class="btn delete-btn" onclick="window.location='/coop/project/delete/${project.getId()}';">Delete Project</button>
			                            </div>
			                        </div>
			                    </div>
			                </div>
					</td>
				</tr>
			</table>
		</form>
		<!-- multiSelectBox.js should be referenced before page js -->
		<script src="<c:url value='/resources/js/multiSelectBox.js'/>"></script>
		<script src="<c:url value='/resources/js/project.js'/>"></script>
	</jsp:body>
</t:layout>

