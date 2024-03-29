
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:horizontalScrollLayout>
	<jsp:attribute name="title">
		<c:choose> 
			<c:when test="${board.getCycle()!=null}">
				${board.getCycle().getProject().getName()} Cycle <c:out value="${CycleNum}"/>
			</c:when>
			<c:otherwise>
				${board.getProject().getName()} Product Board
			</c:otherwise>
		</c:choose>
	</jsp:attribute>
	<jsp:attribute name="pageButtons">
		<c:if test="${board.getCycle()!=null}">
			<button type="button" class="btn cancel-btn" onclick="window.location='/coop/cycle/${cycleId}';">Back to Cycle</button>
		</c:if>
		<button type="button" class="btn cancel-btn" onclick="window.location='/coop/project/${projectId}';">Back to Project</button>
        <select id="userFilter" class="btn select-btn" onchange="filterTasks()">
            <option value="all">All users</option>
            <c:forEach items="${projectUsers}" var="projectUser">
                <option value="${projectUser.id}">${projectUser.firstName} ${projectUser.lastName}</option>
            </c:forEach>
        </select>
		<select id="roleFilter" class="btn select-btn" onchange="filterTasks()">
			<option value="all">All roles</option>
			<c:forEach items="${taskRoles}" var="taskRole">
                <option value="${taskRole}">${taskRole}</option>
            </c:forEach>
		</select>
	</jsp:attribute>
	<jsp:body>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.css" integrity="sha512-aOG0c6nPNzGk+5zjwyJaoRUgCdOrfSDhmMID2u4+OIslr0GjpLKo7Xm0Ao3xmpM4T8AmIouRkqwj1nrdVsLKEQ==" crossorigin="anonymous" />
	<link rel="stylesheet" href='<c:url value="/resources/css/board.css"/>' type="text/css"/>
	<link rel="stylesheet" href='<c:url value="/resources/css/multiSelectBox.css"/>' type="text/css"/>
	
	<div id="board-container" class="container-fluid">
	<!-- Product Board -->
	<c:if test="${board.getCycle()==null}">
		<div class="row text-center">
			<div class="card product-board-container"> <!-- col-sm-2  -->
				<div class="card-header column-header">
					Remaining Tasks
					<form class="float-right" method="post" name="newTask">
						<span class='submit-color'><i class="fa fa-plus-square add-btn" data-toggle="tooltip" data-placement="top" title="Add Task" onclick="addNewProductBoardCard();" style="margin-right:10px;"></i></span>
					</form>
				</div>
				<div style="min-height:40vh;" id="remainingTasks"></div>
			</div> 
		</div>
	</c:if>
	
	<!-- Cycle Board -->
	<c:if test="${board.getCycle()!=null}">
		<input type="hidden" name="userId" id="userId" value="${user.getId()}">
		<div class="row text-center">
		<c:forEach items="${statuses}" var="status">
		<div class="col-sm-2 task-column-container">
			<div class="card">
				<div class="card-header column-header">
					${status}
					<c:if test="${status eq notStartedStatus}">
					<form class="float-right" method="post" name="newTask">
						<span class="submit-color"><i class="fa fa-plus-square add-btn" data-toggle="tooltip" data-placement="top" title="Add Task" onclick="addNewCycleBoardCard();"></i></span>
					</form>
					</c:if>
				</div>
				<div class="card-body column-body" id="${status}"></div>
			</div>
		</div>
		</c:forEach>
		</div>
	</c:if>
	</div>
	<div class="modal fade content" id="taskModal" role="dialog">
		<div class="modal-dialog wide-modal">
			<div class="modal-content">
				<div class="modal-header">
					<p class="modal-title mr-5" style="font-size: large; font-wieght: bold; vertical-align:middle">Task Info</p>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body">
					<div class="btn-group menu-btn-group" style="justify-content: center">
						<button id="task-details-btn" type="button" class="btn nav-btn">Details</button>
						<button id="task-impact-btn" type="button" class="btn nav-btn">Impact</button>
						<button id="task-users-btn" type="button" class="btn nav-btn">Users</button>
						<button id="task-notes-btn" type="button" class="btn nav-btn">Notes</button>
						<button id="task-work-btn" type="button" class="btn nav-btn">Work</button>
						<button id="task-history-btn" type="button" class="btn nav-btn">History</button>
					</div>
					<hr>
					<div id="taskFormUpdated" class="alert alert-success" style="display:none"></div>
					<div id="taskFormErrors" class="alert alert-danger" style="display:none"></div>
					<div id="task-details">
						<input type="hidden" id="taskId" name="taskId">
						<input type="hidden" id="projectId" name="projectId" value="${projectId}">
						<form id="taskDetailsUpdateForm" name="taskDetailsUpdateForm" method="post" onsubmit="return false;">
							<input type="hidden" id="taskStatus" name="status">
							<input type="hidden" id="isCycleBoard" name="isCycleBoard" value="${board.getCycle()!=null}">
							<div class="form-group">
								<input type="text" class="form-control" id="description" name="description" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>>
							</div>
							<div class="form-group">
								<label for="tag">Tag</label>
								<select class="form-control" id="tag" name="tag" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>>
								</select>
							</div>
							<c:choose>
							<c:when test="${board.getCycle()!=null}">
							<div class="form-group">
								<label for="newStatus">Status</label>
								<select class="form-control" id="newStatus" name="newStatus" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>>
									<c:forEach items="${statuses}" var="status">
										<option value="${status}">${status}</option>
									</c:forEach>
								</select>
							</div>
							<div id="branchField" class="form-group">
								<label for="branch">Branch</label>
								<select class="form-control" id="branch" name="branch" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>>
									<option id="emptyBranchOption" value="">Choose a repository branch</option>
									<c:forEach var="repositoryProjectBranch" items="${repositoryProjectBranches}">
									<option value="${repositoryProjectBranch}">${repositoryProjectBranch}</option>
									</c:forEach>
								</select>
							</div>
							<div class="form-group">
								<label for="fmtCompletionDateEst">Estimated Completion Date</label> <a href="#" data-toggle="popover" data-content="This is intended to provide reviewers with an estimate on when they can review a task."><i class="fa fa-question-circle" aria-hidden="true"></i></a>
								<input type="datetime" class="form-control" id="fmtCompletionDateEst" name="fmtCompletionDateEst" value="" placeholder="MM/DD/YYYY" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>>
							</div>
							<div class="form-group">
								<label for="timeEstimate">Time Estimate (minutes)</label>
								<input type="number" class="form-control" id="timeEstimate" name="timeEstimate" min="0" value="0" step="15" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>>
							</div>
							</c:when>
							<c:otherwise>
							<div class="form-group">
								<label for="boardId">Move</label>
								<select class="form-control" id="boardId" name="boardId">
									<option value="">Choose a cycle</option>
									<c:forEach items="${cycles}" var="cycle">
										<option value="${cycle.getBoard().getBoardId()}">${cycle}</option>
									</c:forEach>
								</select>
							</div>
							</c:otherwise>
							</c:choose>
						</form>
					</div>
					<div id="task-impact">
						<div id="task-impact-list">
						</div>
						<button id="toggle-impact-form" class="btn other-btn">Add Impacted Files</button> <a href="#" data-toggle="popover" data-content="Impacted files must be selected for all coding tasks after selecting a branch on the Details tab."><i class="fa fa-question-circle" aria-hidden="true"></i></a>
						<div id="impact-form-container">
							<form id="addImpactForm" name="addImpactForm" method="post" onsubmit="return false;">
								<input type="hidden" id="repositoryProjectId" name="repositoryProjectId" value="${repositoryProjectId}">
								<div id="filesLoading">
									<i class="fa fa-circle-o-notch fa-spin fa-1x fa-fw"></i>
									<span class="sr-only">Loading...</span>
								</div>
								<div id="repositoryFileTree">
									<!-- populated by board.js -->
								</div>
								<button id="submit-impact-btn" class="btn submit-btn">Add Selected Files</button>
							</form>
						</div>
					</div>
					<div id="task-users">
						<form id="taskUsersUpdateForm" name="taskUsersUpdateForm" method="post" onsubmit="return false;">
							<input type="hidden" id="isCycleBoard" name="isCycleBoard" value="${board.getCycle()!=null}">
							<table>
								<tr class="radioBtnOpts">
									<th></th>
									<th>Owner</th>
									<th>Helper</th>
									<th>Reviewer</th>
									<th>None</th>
						          </tr>
								<c:forEach items="${projectUsers}" var="projectUser">
					          	<tr id="${projectUser.getId()}" class="task-user-row">
					          		<td>${projectUser}</td>
					          		<td><input type="radio" name="users-${projectUser.getId()}" id="${projectUser.getId()}-owner" value="Owner" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>/></td>
					          		<td><input type="radio" name="users-${projectUser.getId()}" id="${projectUser.getId()}-helper" value="Helper" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>/></td>
					          		<td><input type="radio" name="users-${projectUser.getId()}" id="${projectUser.getId()}-reviewer" value="Reviewer" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>/></td>
					          		<td><input type="radio" name="users-${projectUser.getId()}" id="${projectUser.getId()}-none" value="None" <c:if test="${!board.isActive() || !isMember}">disabled</c:if>/></td>
					          	</tr>
								</c:forEach>
							</table>
						</form>
					</div>
					<div id="task-notes">
						<c:if test="${board.isActive() && isMember}">
						<div class="form-row" style="margin-bottom:3%;">
							<div class="col-sm-10">
								<input type="text" class="form-control" id="noteText" name="noteText">
							</div>
							<div class="col-sm-2">
								<button id="submit-note-btn" class="btn submit-btn">Add</button>
							</div>
						</div>
						</c:if>
						<div id="note-list">
						</div>
					</div>
					<div id="task-work">
						<div id="work-form-container">
							<c:if test="${board.isActive() && isMember}">
							<form id="addWorkForm" name="addWorkForm" method="post" onsubmit="return false;">
								<div class="form-group">
									<label for="description">Description</label>
									<input type="text" class="form-control" id="description" name="description">
								</div>
								<div class="form-group">
									<label for="minutes">Minutes</label>
									<input type="number" class="form-control" id="minutes" name="minutes" min="0" value="15" step="15">
								</div>
								<button id="submit-work-btn" class="btn submit-btn">Submit</button>
							</form>
							<button id="add-work-btn" class="btn other-btn">Add</button> <a href="#" id="addWorkPopover" data-toggle="popover" data-content="A user must be assigned to a role to report work on a task."><i class="fa fa-question-circle" aria-hidden="true"></i></a>
							</c:if>
						</div>
						<div id="task-work-list">
						</div>
					</div>
					<div id="task-history">
					</div>
				</div>
				<div class="modal-footer">
				    <button class="btn cancel-btn" data-dismiss="modal">Close</button>
					<c:if test="${board.isActive() && isMember}">
					<button id="submit-task-details-btn" class="btn submit-btn" type="submit">Update</button>
					<button id="submit-task-users-btn" class="btn submit-btn" type="submit">Update</button>
					<button id="task-delete-btn" class="btn delete-btn" type="submit" data-toggle="modal" data-target="#deleteModal">Delete</button>
					</c:if>
					<c:if test="${!board.isActive() and CycleNum lt cycles.size()}">
						<button id="next-cycle-btn" class="btn submit-btn" type="submit">Copy to Next Cycle</button>
					</c:if>
					<input type="submit" id="deleteTask" style="display: none;">
				</div>
			</div>
		</div>
	</div> 
	<div class="modal fade content" id="deleteModal" role="dialog">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<p class="modal-title" style="font-size: large; font-wieght: bold;">Delete Task</p>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body">Are you sure you want to delete this task? This will also delete all work associated with this task!</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					<button type="button" class="btn delete-btn" onclick="$('#deleteTask').click();" data-dismiss="modal">Delete Task</button>
				</div>
			</div>
		</div>
	</div>				
	<div id="popupError" class="modal fade content" role="alert">
          <div class="modal-dialog">
          	<div id="popupError" class="modal-body alert alert-danger">
				<a href="#" class="close" data-dismiss="modal" aria-label="close">&times;</a>
				<div id="popupErrorText"></div>
			</div>
		</div>
     </div>
	<!-- multiSelectBox.js should be referenced before page js -->
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js" integrity="sha512-uto9mlQzrs59VwILcLiRYeLKPPbS/bT71da/OEBYEwcdNUk8jYIy+D176RYoop1Da+f9mvkYrmj5MCLZWEtQuA==" crossorigin="anonymous"></script>
	<script src="<c:url value='/resources/js/multiSelectBox.js'/>"></script>
	<script src='<c:url value="/resources/js/board.js"/>'></script>
	<script>generateContent(${board.getBoardId()}, ${board.getCycle()!=null}, ${board.isActive()})</script>
	</jsp:body>
</t:horizontalScrollLayout>