<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:cardlessLayout>
	<jsp:attribute name="title">
		${project.getName()}
	</jsp:attribute>
	<jsp:attribute name="pageButtons">
		<c:if test="${isProjectOwner}">
 			<button class="btn update-btn" type="button" onclick="window.location.href='/coop/project/update/${project.getId()}'">Update Project</button>
		</c:if>
        <c:if test="${isMember}">
		    <button type="button" class="btn other-btn" data-toggle="modal" data-target="#leaveModal">Leave Project</button>
	    </c:if>
    </jsp:attribute>
	<jsp:body>
		<div class="row" style="margin-top: 2%;">
			<div class="col-sm-3">
				<div class="card" style="margin-top:0%;">
					<div class="card-header">
						Project
					</div>
					<div class="card-body">
						<ul id="projectData" class="list-group">
							<li class="list-group-item small-pad"><a href="${project.repositoryProject.repositoryProjectUrl}" target="_blank">View Repository Project</a></li>
							<c:choose>
								<c:when test="${empty project.slackWorkspace}">
									<li class="list-group-item small-pad">
										<a href="${slackRegistrationLink}"><img alt="Add to Slack" height="40" width="139" src="https://platform.slack-edge.com/img/add_to_slack.png" srcset="https://platform.slack-edge.com/img/add_to_slack.png 1x, https://platform.slack-edge.com/img/add_to_slack@2x.png 2x"></a>
									</li>
								</c:when>
								<c:otherwise>
									<li class="list-group-item small-pad">
										<a href="https://app.slack.com/client/${project.slackWorkspace.slackWorkspaceId}" target="_blank">View Slack Workspace</a>
									</li>
								</c:otherwise>
							</c:choose>
						</ul>
						<div class="card board-card">
							<div class="card-body">
		    						<div class="card-body-icon">
		    							<i class="fa fa-fw fa-list-alt"></i>
		    						</div>
		       					<div class="mr-5">Project Board</div>
		       					</div>
		    					<a class="card-footer text-white small" href="/coop/board/${project.getBoard().getBoardId()}">
		    						<span class="float-left">View Details</span>
		    						<span class="float-right"><i class="fa fa-angle-right"></i></span>
		    					</a>
		   				</div>
					</div>
				</div>
			</div>
			<div class="col-sm-9">
				<div class="card" style="margin-top:0%;">
					<div class="card-header">
						Project Cycles
					</div>
					<div class="card-body">
						<div class="row">
							<c:set var="cycleCounter" scope = "page" value = "0"/>
	    						<c:forEach var="cycle" items="${cycles}">
	    						<c:set var="cycleCounter" value = "${cycleCounter+1}" scope="page"/>
	    						<div class="col-sm-4">
							     <div class="card cycle-card">
						            	<div class="card-body">
						              		<div class="card-body-icon">
						                		<i class="fa fa-fw fa-dashboard"></i>
						              		</div>
						              		<div class="mr-5">Cycle <c:out value="${cycleCounter}"/></div>
						            	</div>
						            	<a class="card-footer text-white small" href="/coop/cycle/<c:out value="${cycle.getId()}"/>">
						              		<span class="float-left">View Details</span>
						              		<span class="float-right">
						                		<i class="fa fa-angle-right"></i>
						              		</span>
						              		
						            	</a>
						          </div>
					          </div>
					     </c:forEach>
					     <c:if test="${cycleCounter eq 0}">
				          	<div class="col-sm-12">None</div>
				          </c:if>
				          </div>
				          <c:if test="${isProjectOwner}">
		   				<div class="card-btns">
    							<button class="btn other-btn" type="button" onclick="window.location.href='/coop/cycle/add?project=${project.getId()}'">Create Cycle</button>
						</div>
						</c:if>
					</div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-12">
				<div class="card">
					<div class="card-header">
						Project Users
					</div>
					<div class="card-body">
						<div class="row">
							<c:forEach items="${users}" var="projectUser">
					   		<div class="col-sm-3">
					   			<div class="card user-card">
					       			<div class="card-body">
					       				<div class="card-body-icon">
					           				<i class="fa fa-fw fa-user-circle-o"></i>
					       				</div>
						              		<div class="mr-5">
						              			${projectUser.getUsername()}
						              			<c:forEach var="owner" items="${owners}">
		 									<c:if test="${owner eq projectUser}">
		   										[Owner]
		 									</c:if>
											</c:forEach>
						              		</div>
						           	</div>
						            	<a class="card-footer text-white small" href="/coop/user/<c:out value="${projectUser.getId()}"/>">
						        			<span class="float-left">View Details</span>
						              		<span class="float-right">
						                		<i class="fa fa-angle-right"></i>
						           		</span>
						           	</a>
						        </div>
						    	</div>
							</c:forEach>
						</div>
					</div>
				</div>
			</div>
		</div>
        <c:if test="${isMember}">
		<div class="row">
			<div class="col-sm-12">
				<div class="card">
					<div class="card-header">
						Project Work
						<select id="workTypeSelect" name="workType" onchange="initializeWorkStats(this.value, 'project', ${project.getId()})">
							<option value="role">View work by role</option>
							<option value="tag">View work by tag</option>
							<option value="collaborators">View collaborators by role</option>
						</select>
					</div>
					<div class="card-body">
						<ul class="list-inline border rounded">
							<li id="workPercentileItem" class="list-inline-item"><span class="inline-item-label">Work percentile:</span> ${workPercentile}</li>
							<li id="collaboratorPercentileItem" class="list-inline-item"><span class="inline-item-label">Collaborator percentile:</span> ${collaboratorPercentile}</li>
						</ul>
						<div class="row roleCharts">
							<div class="col-sm-6"><canvas id="allRolesCanvas-project${project.getId()}"></canvas></div>
							<div class="col-sm-6"><canvas id="ownerCanvas-project${project.getId()}"></canvas></div>
						</div>
						<div class="row roleCharts">
							<div class="col-sm-6"><canvas id="helperCanvas-project${project.getId()}"></canvas></div>
							<div class="col-sm-6"><canvas id="reviewerCanvas-project${project.getId()}"></canvas></div>
						</div>
						<div class="row tagCharts">
							<div class="col-sm-6"><canvas id="allTagsCanvas-project${project.getId()}"></canvas></div>
							<div class="col-sm-6"><canvas id="researchCanvas-project${project.getId()}"></canvas></div>
						</div>
						<div class="row tagCharts">
							<div class="col-sm-6"><canvas id="featureCanvas-project${project.getId()}"></canvas></div>
							<div class="col-sm-6"><canvas id="testCanvas-project${project.getId()}"></canvas></div>
						</div>
						<div class="row tagCharts">
							<div class="col-sm-6"><canvas id="bugFixCanvas-project${project.getId()}"></canvas></div>
							<div class="col-sm-6"><canvas id="refactorCanvas-project${project.getId()}"></canvas></div>
						</div>
						<div class="row tagCharts">
							<div class="col-sm-6"><canvas id="otherCanvas-project${project.getId()}"></canvas></div>
							<div class="col-sm-6"></div>
						</div>
						<div class="row collaboratorCharts">
							<div class="col-sm-6"><canvas id="allRolesCollaboratorCanvas-project${project.getId()}"></canvas></div>
							<div class="col-sm-6"><canvas id="ownerCollaboratorCanvas-project${project.getId()}"></canvas></div>
						</div>
						<div class="row collaboratorCharts">
							<div class="col-sm-6"><canvas id="helperCollaboratorCanvas-project${project.getId()}"></canvas></div>
							<div class="col-sm-6"><canvas id="reviewerCollaboratorCanvas-project${project.getId()}"></canvas></div>
						</div>
					</div>
				</div>
			</div>
		</div>
        </c:if>
		<div class="modal fade content" id="leaveModal" role="dialog">
			<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">
						<p class="modal-title" style="font-size: large; font-wieght: bold;">Leave Project</p>
						<button type="button" class="close" data-dismiss="modal">&times;</button>
					</div>
					<div class="modal-body">Are you sure you want to leave this project?</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
						<button type=button class="btn delete-btn" onclick="window.location.href='/coop/project/leave/${project.getId()}'">Leave Project</button>
					</div>
				</div>
			</div>
		</div>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.2.0/chart.min.js" integrity="sha512-VMsZqo0ar06BMtg0tPsdgRADvl0kDHpTbugCBBrL55KmucH6hP9zWdLIWY//OTfMnzz6xWQRxQqsUFefwHuHyg==" crossorigin="anonymous"></script>
		<script src="<c:url value='/resources/js/work.js'/>"></script>
		<script>initializeWorkStats('role', 'project', ${project.getId()})</script>
	</jsp:body>
</t:cardlessLayout>