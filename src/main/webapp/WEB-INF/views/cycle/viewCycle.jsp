<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
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
		<script src="<c:url value='/resources/js/chart-js/Chart.bundle.js'/>"></script>
		<script src="<c:url value='/resources/js/work.js'/>"></script>
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
                    <div class="card-header">
                        Cycle Work
                        <select id="workTypeSelect" name="workType" onchange="initializeWorkStats(this.value, 'cycle', ${cycle.getId()})">
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
                            <div class="col-sm-6"><canvas id="allRolesCanvas-cycle${cycle.getId()}"></canvas></div>
                            <div class="col-sm-6"><canvas id="ownerCanvas-cycle${cycle.getId()}"></canvas></div>
                        </div>
                        <div class="row roleCharts">
                            <div class="col-sm-6"><canvas id="helperCanvas-cycle${cycle.getId()}"></canvas></div>
                            <div class="col-sm-6"><canvas id="reviewerCanvas-cycle${cycle.getId()}"></canvas></div>
                        </div>
                        <div class="row tagCharts">
                            <div class="col-sm-6"><canvas id="allTagsCanvas-cycle${cycle.getId()}"></canvas></div>
                            <div class="col-sm-6"><canvas id="researchCanvas-cycle${cycle.getId()}"></canvas></div>
                        </div>
                        <div class="row tagCharts">
                            <div class="col-sm-6"><canvas id="featureCanvas-cycle${cycle.getId()}"></canvas></div>
                            <div class="col-sm-6"><canvas id="testCanvas-cycle${cycle.getId()}"></canvas></div>
                        </div>
                        <div class="row tagCharts">
                            <div class="col-sm-6"><canvas id="bugFixCanvas-cycle${cycle.getId()}"></canvas></div>
                            <div class="col-sm-6"><canvas id="refactorCanvas-cycle${cycle.getId()}"></canvas></div>
                        </div>
                        <div class="row tagCharts">
                            <div class="col-sm-6"><canvas id="otherCanvas-cycle${cycle.getId()}"></canvas></div>
                            <div class="col-sm-6"></div>
                        </div>
                        <div class="row collaboratorCharts">
                            <div class="col-sm-6"><canvas id="allRolesCollaboratorCanvas-cycle${cycle.getId()}"></canvas></div>
                            <div class="col-sm-6"><canvas id="ownerCollaboratorCanvas-cycle${cycle.getId()}"></canvas></div>
                        </div>
                        <div class="row collaboratorCharts">
                            <div class="col-sm-6"><canvas id="helperCollaboratorCanvas-cycle${cycle.getId()}"></canvas></div>
                            <div class="col-sm-6"><canvas id="reviewerCollaboratorCanvas-cycle${cycle.getId()}"></canvas></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        </c:if>
        <script src='<c:url value="/resources/js/chart-js/Chart.bundle.js" />'></script>
		<script>initializeCycleChart(${cycle.getId()}, ${cycle.getStartDate().getTime()}, ${cycle.getEndDate().getTime()})</script>
		<script>initializeWorkStats('role', 'cycle', ${cycle.getId()})</script>
	</jsp:body>
</t:cardlessLayout>