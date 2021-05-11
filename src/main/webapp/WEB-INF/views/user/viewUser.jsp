<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:cardlessLayout>
	<jsp:attribute name="title">
		${user}
	</jsp:attribute>
	<jsp:attribute name="pageButtons">
		<c:if test="${updatable}">
		<form class="page-btn-form" action="/coop/user/delete/${user.getId()}" method="post">
			<a href="update/${user.getId()}" class="btn update-btn">Update User</a>
			<c:if test="${userTask.size() == 0}">
				<button type="button" class="btn delete-btn" data-toggle="modal" data-target="#deleteModal">Delete User</button>
				<input type="submit" id="deleteUser" style="display: none;">
			</c:if>
		</form>
		<c:if test="${user.getIsActive() == 0}">
		<form class="page-btn-form" action="/coop/user/${user.getId()}/changeState/1" method="post">
			<input id="activateButton" class="btn other-btn" type="submit" value="Activate User"/>
		</form>
		</c:if>
		<c:if test="${user.getIsActive() == 1}">
		<form class="page-btn-form" action="/coop/user/${user.getId()}/changeState/0" method="post">
			<input id="deactivateButton" class="btn delete-btn" type="submit" value="Deactivate User"/>
		</form>
		</c:if>
		</c:if>
	</jsp:attribute>
	<jsp:body>
		<div class="row">
			<div class="col-sm-6">
				<div class="card">
					<div class="card-header">
						Your Projects
					</div>
					<div class="card-body">
						<div class="row">
							<c:forEach items="${userProject}" var="project">
                                <div class="col-sm-6">
							        <div class="card project-card">
						            	<div class="card-body">
						              		<div class="card-body-icon">
						                		<i class="fa fa-fw fa-gears"></i>
						              		</div>
						              		<div class="mr-5">${project.getName()}</div>
						            	</div>
						            	<a class="card-footer text-white small" href="/coop/project/${project.getId()}">
						              		<span class="float-left">View Details</span>
						              		<span class="float-right">
						                		<i class="fa fa-angle-right"></i>
						              		</span>
						              		
						            	</a>
						            </div>
					            </div>
					     	</c:forEach>
					     	<c:if test="${empty userProject}">
				          		<div class="col-sm-12">None</div>
				          	</c:if>
				          </div>
				          <div class="card-btns">
	    						<a href="/coop/project/add" class="btn other-btn">Add Project</a>
						</div>
					</div>
				</div>
			</div>
			<div class="col-sm-6">
				<div class="card">
					<div class="card-header">
						Other Projects
					</div>
					<div class="card-body">
						<div class="row">
							<c:forEach items="${otherProjects}" var="project">
                                <div class="col-sm-6">
									<div class="card project-card">
										<div class="card-body">
											<div class="card-body-icon">
												<i class="fa fa-fw fa-gears"></i>
											</div>
											<div class="mr-5">${project.getName()}</div>
										</div>
										<a class="card-footer text-white small" href="/coop/project/${project.getId()}">
											<span class="float-left">View Details</span>
											<span class="float-right">
						                		<i class="fa fa-angle-right"></i>
						              		</span>

										</a>
									</div>
								</div>
							</c:forEach>
							<c:if test="${empty otherProjects}">
								<div class="col-sm-12">None</div>
							</c:if>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-12">
				<div class="card">
					<div class="card-header">
						Tasks
					</div>
					<div class="card-body">
						<div class="row">
							<c:forEach items="${userTasks}" var="task">
					   		<div class="col-sm-3">
					   			<div class="card task-card-${task.getTag()}">
					       			<div class="card-body">
					       				<div class="card-body-icon">
					           				<i class="fa fa-fw fa-check-circle-o"></i>
					       				</div>
						              		<div class="mr-5">${task}</div>
						           	</div>
						            	<a class="card-footer text-white small" href="/coop/board/${task.getBoard().getBoardId()}">
						        			<span class="float-left">View Board</span>
						              		<span class="float-right">
						                		<i class="fa fa-angle-right"></i>
						           		</span>
						           	</a>
						        </div>
						    	</div>
							</c:forEach>
							<c:if test="${empty userTasks}">
					          	<div class="col-sm-12">None</div>
					          </c:if>
						</div>
					</div>
				</div>
			</div>
		</div>
		<c:if test="${updatable}">
		<div class="row">
			<div class="col-sm-12">
				<div class="card">
					<div class="card-header">
						Work
						<select id="workTypeSelect" name="workType" onchange="updateCharts(this.value)">
							<option value="role">View work by role</option>
							<option value="tag">View work by tag</option>
							<option value="collaborators">View collaborators by role</option>
						</select>
					</div>
					<div class="card-body">
						<ul class="list-inline border rounded" style="margin-top: 1%;">
							<li id="workPercentileItem" class="list-inline-item"><span class="inline-item-label">Work percentile:</span> ${workPercentile}</li>
							<li id="collaboratorPercentileItem" class="list-inline-item"><span class="inline-item-label">Collaborator percentile:</span> ${collaboratorPercentile}</li>
						</ul>
						<div id="allProjects">
							<div class="row roleCharts">
								<div class="col-sm-6"><canvas id="allRolesCanvas"></canvas></div>
								<div class="col-sm-6"><canvas id="ownerCanvas"></canvas></div>
							</div>
							<div class="row roleCharts">
								<div class="col-sm-6"><canvas id="helperCanvas"></canvas></div>
								<div class="col-sm-6"><canvas id="reviewerCanvas"></canvas></div>
							</div>
							<div class="row tagCharts">
								<div class="col-sm-6"><canvas id="allTagsCanvas"></canvas></div>
								<div class="col-sm-6"><canvas id="researchCanvas"></canvas></div>
							</div>
							<div class="row tagCharts">
								<div class="col-sm-6"><canvas id="featureCanvas"></canvas></div>
								<div class="col-sm-6"><canvas id="testCanvas"></canvas></div>
							</div>
							<div class="row tagCharts">
								<div class="col-sm-6"><canvas id="bugFixCanvas"></canvas></div>
								<div class="col-sm-6"><canvas id="refactorCanvas"></canvas></div>
							</div>
							<div class="row tagCharts">
								<div class="col-sm-6"><canvas id="otherCanvas"></canvas></div>
								<div class="col-sm-6"></div>
							</div>
							<div class="row collaboratorCharts">
								<div class="col-sm-6"><canvas id="allRolesCollaboratorCanvas"></canvas></div>
								<div class="col-sm-6"><canvas id="ownerCollaboratorCanvas"></canvas></div>
							</div>
							<div class="row collaboratorCharts">
								<div class="col-sm-6"><canvas id="helperCollaboratorCanvas"></canvas></div>
								<div class="col-sm-6"><canvas id="reviewerCollaboratorCanvas"></canvas></div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		</c:if>
		<div class="modal fade content" id="deleteModal" role="dialog">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<p class="modal-title" style="font-size: large; font-wieght: bold;">Delete User</p>
						<button type="button" class="close" data-dismiss="modal">&times;</button>
					</div>
					<div class="modal-body">Are you sure you want to delete this User?</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
						<button type=button class="btn delete-btn" onclick="$('#deleteUser').click();">Delete User</button>
					</div>
				</div>
			</div>
		</div>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.2.0/chart.min.js" integrity="sha512-VMsZqo0ar06BMtg0tPsdgRADvl0kDHpTbugCBBrL55KmucH6hP9zWdLIWY//OTfMnzz6xWQRxQqsUFefwHuHyg==" crossorigin="anonymous"></script>
		<script src="<c:url value='/resources/js/work.js'/>"></script>
		<script src="<c:url value='/resources/js/user.js'/>"></script>
		<script>loadCharts();</script>
	</jsp:body>
</t:cardlessLayout>