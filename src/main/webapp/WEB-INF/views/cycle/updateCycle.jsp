<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:layout title="Update Cycle" error="${error}" success="${success}">
	<jsp:body>
		<form id="updateCycleForm" method="post">
			<div class="col-xl-3 col-sm-6 mb-3">
				<div class="form-group">
					<label for="startdate">Start Date</label> <input type="datetime"
						class="form-control" id="startdate" name="startdate"
						value="${startdate}" />
				</div>
			</div>
			<div class="col-xl-3 col-sm-6 mb-3">
				<div class="form-group">
					<label for="enddate">End Date</label> <input type="datetime"
						class="form-control" id="enddate" name="enddate"
						value="${enddate}" />
				</div>
			</div>
			<div class="col-xl-4 col-sm-6 mb-3">
				<button type="submit" class="btn submit-btn">Update Cycle</button>
				<button type="button" class="btn delete-btn" data-toggle="modal"
					data-target="#deleteModal">Delete Cycle</button>
				<div class="modal fade" id="deleteModal" role="dialog">
					<div class="modal-dialog">
						<div class="modal-content">
							<div class="modal-header">
								<p class="modal-title"
									style="font-size: large; font-wieght: bold;">Delete Cycle</p>
								<button type="button" class="close" data-dismiss="modal">&times;</button>
							</div>
							<div class="modal-body">
								Are you sure you want to delete this cycle? All connected tasks and task histories will be deleted.
							</div>
							<div class="modal-footer">
								<button type="button" class="btn btn-default"
									data-dismiss="modal">Close</button>
								<button type=button class="btn delete-btn"
									onclick="window.location='/coop/cycle/delete/${cycleid}';">Delete Cycle</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</form>
		<script src="<c:url value='/resources/js/cycle.js'/>"></script>
		<link rel="stylesheet"
				href='<c:url value="/resources/jquery-ui/jquery-ui.min.css"/>'
				type="text/css" />
		<script src="<c:url value='/resources/jquery-ui/jquery-ui.min.js'/>"></script>
	</jsp:body>
</t:layout>
