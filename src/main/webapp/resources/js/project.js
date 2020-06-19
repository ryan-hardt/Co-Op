$(function() {
	//populate projects when repository is selected
	$('#repositoryHostId').change(function () {
		const repositoryHostId = $("#repositoryHostId").val();
		const url = "/coop/repository/queryRepositoryProjects/"+ repositoryHostId;
		jQuery.ajax(
			url, {
				async: false,
				method: "POST",
				success: (data,status) => {
					$('#repositoryProjectUrl').html('<option value="">Choose a repository project...</option>');
					for(let i=0; i<data.length; i++) {
						var projectHTML = "<option value='" + data[i].repositoryProjectUrl + "'>"+data[i].name+"</option>'";
						$('#repositoryProjectUrl').append(projectHTML);
					}
				},
				error: () => {
					console.error("error when getting repository projects for repository host id " + repositoryHostId);
				}
			});
	});
});

function removeSlackWorkspace(projectId) {
	const url = "/coop/project/slack/remove/"+ projectId;
	jQuery.ajax(
		url, {
			async: false,
			method: "POST",
			success: (data) => {
				var successDiv = document.getElementById("success");
				if(data === "success") {
					successDiv.innerHTML = "Your slack workspace has been removed.";
					$("#slackWorkspaceRow").hide();
				} else {
					successDiv.innerHTML = "Your slack workspace could not be removed.";
				}
				successDiv.style["display"] = "block";
			},
			error: () => {
				console.error("error when removing slack workspace for project " + projectId);
			}
		});
}

function addProjectValidate() {
	var errorDiv = document.getElementById("errors");

	//must have name
	var s = document.addProjectForm.projName.value;
	if (s == "") {
		errorDiv.innerHTML = "Your project must have a name.";
		errorDiv.style["display"] = "block";
		return false;
	}
	if (s.length > 30) {
		errorDiv.innerHTML = "Your project name can be no more than 30 characters long.";
		errorDiv.style["display"] = "block";
		return false;
	}

	//must have gitLabWebUrl
	var repositoryProjectUrl = document.addProjectForm.repositoryProjectUrl.value;
	if (repositoryProjectUrl == "") {
		errorDiv.innerHTML = "Your project must have an associated repository project.";
		errorDiv.style["display"] = "block";
		return false;
	}
	//must have at least one user
	document.addProjectForm.users.value = getSelectedMultiSelectBoxIds();
	if (document.addProjectForm.users.value == "") {
		errorDiv.innerHTML = "Your project must have at least one user.";
		errorDiv.style["display"] = "block";
		return false;
	}

	//must have at least one owner
	var allCheckboxes = document.getElementsByName("owners");
	var owners = [];
	for (var i = 0; i < allCheckboxes.length; i++) {
		if (allCheckboxes[i].checked) {
			owners.push(allCheckboxes[i].value);
		}
	}
	if (owners.length == 0) {
		errorDiv.innerHTML = "Your project must have at least one owner.";
		errorDiv.style["display"] = "block";
		return false;
	}

	return true;
}

