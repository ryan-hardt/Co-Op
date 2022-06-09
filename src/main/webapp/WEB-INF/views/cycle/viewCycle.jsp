<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<t:cardlessLayout>
	<jsp:attribute name="title">
		${cycle.getProject()} Cycle ${cycleNumber}
	</jsp:attribute>
	<jsp:attribute name="pageButtons">
		<c:if test="${isProjectOwner}">
			<button type="button" class="btn update-btn" onclick="window.location='/coop/cycle/update/${cycle.getId()}';">Update Cycle</button>
		</c:if>
		<button type="button" class="btn cancel-btn" onclick="window.location='/coop/project/${cycle.getProject().getId()}';">Back to Project</button>
	</jsp:attribute>
	<jsp:body>
		<div class="row" style="margin-top:2%;">
			<div class="col-sm-3">
				<div class="card" style="margin-top:0%">
					<div class="card-header">
						Cycle Info
					</div>
					<div class="card-body">
						<ul id="cycleData" class="list-group">
							<li class="list-group-item small-pad"><span class="item-label">Start:</span>${startDateStr}</li>
							<li class="list-group-item small-pad"><span class="item-label">End:</span>${endDateStr}</li>
						</ul>
						<div class="card board-card">
							<div class="card-body">
		    						<div class="card-body-icon">
		    							<i class="fa fa-fw fa-list-alt"></i>
		    						</div>
		       					<div class="mr-5">Cycle ${cycleNumber} Board</div>
		       					</div>
		    					<a class="card-footer text-white small" href="/coop/board/${cycle.getBoard().getBoardId()}">
		    						<span class="float-left">View</span>
		    						<span class="float-right"><i class="fa fa-angle-right"></i></span>
		    					</a>
		   				</div>
                        <div id="branchField" class="form-group" style="margin-top:2%">
							<form name="cycleTeamBranchForm" id="cycleTeamBranchForm" method="post" action="/coop/cycle/updateTeamBranch/${cycle.getId()}">
								<label for="teamBranch">Team Branch</label> <a href="#" data-toggle="popover" data-content="Select the branch into which your team has merged their cycle contributions."><i class="fa fa-question-circle" aria-hidden="true"></i></a>
								<select class="form-control" id="teamBranch" name="teamBranch" <c:if test="${!cycle.getBoard().isActive() || !isMember}">disabled</c:if>>
									<option id="emptyBranchOption" value="">Choose a repository branch</option>
									<c:forEach var="repositoryProjectBranch" items="${repositoryProjectBranches}">
										<option value="${repositoryProjectBranch}" <c:if test="${repositoryProjectBranch eq cycle.cycleTeamBranchName}">selected</c:if>>${repositoryProjectBranch}</option>
									</c:forEach>
								</select>
								<input type="submit" class="btn update-btn" value="Update Team Branch" style="margin-top:1%"/>
							</form>
                        </div>
					</div>
					
				</div>
			</div>
			<div class="col-sm-9">
				<div class="card" style="margin-top:0%">
					<div class="card-header">
						Work Timeline
					</div>
					<div class="card-body">
						<div style="height:350px;">
							<canvas id="workCanvas"></canvas>
						</div>
					</div>
				</div>
			</div>
		</div>
		<c:if test="${isMember}">
		<div class="row">
			<div class="col-sm-12">
				<div class="card">
					<div class="card-header">Cycle Work</div>
					<div class="card-body">
						<div class="row">
						<!-- loop through members -->
						<c:forEach items="${cycleStatsMap}" var="userStats">
						<div class="col-sm-4">
							<div class="card user-card">
								<div class="card-body">
									<div class="card-body-icon">
										<i class="fa fa-fw fa-user-circle-o"></i>
									</div>
									<div class="mr-5">
										<h3>${userStats.key}</h3>
										<c:if test="${userStats.value.isEmpty()}">
											<br/>
											<h5>No work reported</h5>
										</c:if>
										<c:if test="${not userStats.value.isEmpty()}">
										<div>
											<canvas id="user${userStats.key.id}Canvas-Role"></canvas>
										</div>
										<div>
											<canvas id="user${userStats.key.id}Canvas-Tag"></canvas>
										</div>
										</c:if>
										<br/>
										<h4>Commits</h4>
										<c:if test="${userStats.value.allCommits.size() eq 0}">
										<br/>
										<h5>No commits found</h5>
										</c:if>
										<c:if test="${userStats.value.allCommits.size() gt 0}">
										<c:forEach items="${userStats.value.allCommits}" var="commit">
										<hr/>
										<c:if test="${userStats.value.mergedCommits.contains(commit)}">[merged] </c:if><a href="${commitBaseUrl}/${commit.commitId}" target="_blank">${commit.commitMessage}</a><br/>
										Added: ${commit.numLinesAdded} Deleted: ${commit.numLinesDeleted}<br/>
										<fmt:formatDate type="both" timeStyle="short" value="${commit.committedDate}"/><br/>
										</c:forEach>
										</c:if>
									</div>
								</div>
							</div>
						</div>
						</c:forEach>
						</div>
					</div>
				</div>
			</div>
		</div>
        </c:if>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.2.0/chart.min.js" integrity="sha512-VMsZqo0ar06BMtg0tPsdgRADvl0kDHpTbugCBBrL55KmucH6hP9zWdLIWY//OTfMnzz6xWQRxQqsUFefwHuHyg==" crossorigin="anonymous"></script>
		<script src="<c:url value='/resources/js/work.js'/>"></script>
		<script src="<c:url value='/resources/js/cycle.js'/>"></script>
		<script>initializeCycleChart(${cycle.getId()}, ${cycle.getStartDate().getTime()}, ${cycle.getEndDate().getTime()})</script>
		<script>initializeWorkStats('cycle', ${cycle.getId()})</script>
	</jsp:body>
</t:cardlessLayout>