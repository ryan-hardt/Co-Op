<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags"%>
<t:layout>
	<jsp:attribute name="title">
		Add New Repository
	</jsp:attribute>
	<jsp:body>
		<form name="addRepositoryHostForm" method="post" onSubmit="return validateRepository();">
			<table>
				<tr>
					<td>
						<div class="form-group">
							<label>Repository host type:</label>
							<select id="repositoryHostType" name="repositoryHostType" class="form-control">
								<option value="">Choose a repository host type...</option>
								<option value="github">GitHub</option>
								<option value="gitlab">GitLab</option>
							</select>
						</div>
					</td>
				</tr>
				<tr id="hostNameRow">
					<td>
						<div class="form-group">
							<!-- changes to Repository user name for GitHub -->
							<label id="hostNameLabel">Repository host name:</label>
							<input type="text" class="form-control" name="repositoryHostName" maxlength="50">
						</div>
					</td>
				</tr>
				<!-- removed for GitHub (value generated) -->
				<tr id="hostUrlRow">
					<td>
						<div class="form-group">
							<label>Repository host URL:</label>
							<input type="text" class="form-control" name="repositoryHostUrl">
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<div class="form-group">
							<label>Access token:</label>
							<input type="password" class="form-control" name="repositoryHostAccessToken">
						</div>
					</td>
				</tr>
				<!-- removed for GitHub -->
				<tr id="namespaceRow">
					<td>
						<div class="form-group">
							<label>Namespace:</label>
							<input type="text" class="form-control" name="repositoryHostNamespace">
						</div>
					</td>
				</tr>
				<tr>
					<td>
						<input type="submit" class="btn submit-btn" value="Add repository host">
						<button class="btn btn-secondary" type="button" onclick="window.location.href='<c:url value="/project/add"/>'">Cancel</button>
					</td>
				</tr>
			</table>
		</form>
		<!-- multiSelectBox.js should be referenced before page js -->
		<script src="<c:url value='/resources/js/repositoryHost.js'/>"></script>
	</jsp:body>
</t:layout>