<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Add Cycle" error="${error}" success="${success}">
	<jsp:body>
		<form id="addCycleForm" name="addCycleForm" action="/coop/cycle/add" method="POST">
			<input type="hidden" name="projectId" value="${project.getId()}">
			<div class="col-xl-3 col-sm-6 mb-3">
				<div class="form-group">
					<label for="startdate">Start Date</label> <input type="datetime" class="form-control" id="startdate" name="startdate" value="" placeholder="MM/DD/YYYY" />
				</div>
			</div>
			<div class="col-xl-3 col-sm-6 mb-3">
				<div class="form-group">
					<label for="enddate">End Date</label> <input type="datetime" class="form-control" id="enddate" name="enddate" value="" placeholder="MM/DD/YYYY" />
				</div>
			</div>
			<div class="col-xl-3 col-sm-6 mb-3">
				<input type="submit" class="btn submit-btn" value="Create Cycle"></input>
			</div>
		</form>
		<script src="<c:url value='/resources/js/cycle.js'/>"></script>
		<link rel="stylesheet" href='<c:url value="/resources/jquery-ui/jquery-ui.min.css"/>' type="text/css" />
		<script src="<c:url value='/resources/jquery-ui/jquery-ui.min.js'/>"></script>
	</jsp:body>
</t:layout>