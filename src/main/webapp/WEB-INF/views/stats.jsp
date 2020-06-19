<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:cardlessLayout>
	<jsp:attribute name="title">
		Work Statistics
	</jsp:attribute>
	<jsp:body>
		<div id="work-charts" class="col-sm-12">
			<ul class="nav nav-tabs" id="myTab" role="tablist">
				<li class="nav-item">
					<a class="nav-link active chart-tabs" id="allProjects-tab" data-toggle="tab" href="#allProjects" role="tab" aria-controls="allProjects" aria-selected="true">All Projects</a>
				</li>
				<c:forEach items="${allProjects}" var="project">
				<li class="nav-item">
					<a class="nav-link chart-tabs" id="project${project.getId()}-tab" data-toggle="tab" href="#project${project.getId()}" role="tab" aria-controls="project${project.getId()}" aria-selected="false" onclick="initializeWorkStats(${project.getId()})">${project.getName()}</a>
				</li>
				</c:forEach>
			</ul>
			<div class="tab-content" id="myTabContent">
				<div class="tab-pane fade show active" id="allProjects" role="tabpanel" aria-labelledby="allProjects-tab">
					<div class="row">
						<div class="col-sm-6"><canvas id="allCanvas"></canvas></div>
						<div class="col-sm-6"><canvas id="ownerCanvas"></canvas></div>
					</div>
					<div class="row">
						<div class="col-sm-6"><canvas id="helperCanvas"></canvas></div>
						<div class="col-sm-6"><canvas id="reviewerCanvas"></canvas></div>
					</div>
				</div>
				<c:forEach items="${allProjects}" var="project">
				<div class="tab-pane fade" id="project${project.getId()}" role="tabpanel" aria-labelledby="project${project.getId()}-tab">
					<div class="row">
						<div class="col-sm-6"><canvas id="allCanvasProject${project.getId()}"></canvas></div>
						<div class="col-sm-6"><canvas id="ownerCanvasProject${project.getId()}"></canvas></div>
					</div>
					<div class="row">
						<div class="col-sm-6"><canvas id="helperCanvasProject${project.getId()}"></canvas></div>
						<div class="col-sm-6"><canvas id="reviewerCanvasProject${project.getId()}"></canvas></div>
					</div>
				</div>
				</c:forEach>
			</div>
		</div>
		<script src='<c:url value="/resources/js/chart-js/Chart.bundle.js" />'></script>
		<script src="<c:url value='/resources/js/work.js'/>"></script>
		<script>initializeWorkStats()</script>
	</jsp:body>
</t:cardlessLayout>
