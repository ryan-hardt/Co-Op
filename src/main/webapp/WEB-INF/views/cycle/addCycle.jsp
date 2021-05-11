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
		<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.css" integrity="sha512-aOG0c6nPNzGk+5zjwyJaoRUgCdOrfSDhmMID2u4+OIslr0GjpLKo7Xm0Ao3xmpM4T8AmIouRkqwj1nrdVsLKEQ==" crossorigin="anonymous" />
		<script src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js" integrity="sha512-uto9mlQzrs59VwILcLiRYeLKPPbS/bT71da/OEBYEwcdNUk8jYIy+D176RYoop1Da+f9mvkYrmj5MCLZWEtQuA==" crossorigin="anonymous"></script>	<script src="<c:url value='/resources/js/multiSelectBox.js'/>"></script>
	</jsp:body>
</t:layout>