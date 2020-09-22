let board = null;
let currentDraggedCard = null;
let currentHoveredCard = null;
let isUserAssignedToTask = false;
let newTaskField = null;
let isBoardActive = false;
let taskStatuses = ["Not Started", "In Progress", "Needs Help", "Completed"];
let taskTags = ["Research", "Feature Implementation", "Unit Test", "Bug Fix", "Refactor", "Other"];
let hoverCounter = 0;

$(function() {
	$("#fmtCompletionDateEst").datepicker({
		dateFormat: "mm/dd/yy"
	});
	
	//hide content on page load
	$('#task-history').hide();
	$('#addWorkForm').hide();
	
	//assign nav button functionality
	$('#task-details-btn').click(function() {
		displayTaskDetails();
	});

	$('#task-impact-btn').click(function() {
		displayTaskImpact();
	});
	
	$('#task-users-btn').click(function() {
		displayTaskUsers();
	});
	
	$('#task-notes-btn').click(function() {
		displayTaskNotes();
	});
	
	$('#task-history-btn').click(function() {
		displayTaskHistory();
	});
	
	$('#task-work-btn').click(function() {
		displayTaskWork();
	});
	
	//assign action button functionality
	$('#add-work-btn').click(function() {
		if(isBoardActive) {
			$('#addWorkForm').show();
			$('#add-work-btn').hide();
		}
	});

	$("#toggle-impact-form").click(function() {
		if($("#impact-form-container").is(":visible")) {
			$("#impact-form-container").hide();
		} else {
			//if a file tree has not yet been retrieved
			if($("#fileTreeList").length == 0) {
				getRepositoryTree($("#repositoryProjectId").val(), $("#impactBranchName").val());
			}
			$("#impact-form-container").show();
		}
	});
	
	//assign form submission button functionality
	$('#submit-task-details-btn').click(function () {
		let error = false;
		if(document.taskDetailsUpdateForm.description.value == '') {
			$('#taskFormErrors').html("Please enter a description.");
			error = true;
		}
		//cycle board
		if(document.taskDetailsUpdateForm.isCycleBoard.value == 'true' && document.taskDetailsUpdateForm.status.value != 'Not Started') {
			if(document.taskDetailsUpdateForm.tag.value == '') {
				$('#taskFormErrors').html("Please select a tag.");
				error = true;
			} else if(document.taskDetailsUpdateForm.fmtCompletionDateEst.value == '') {
				$('#taskFormErrors').html("Please enter an estimated completion date.");
				error = true;
			} else if(document.taskDetailsUpdateForm.timeEstimate.value == 0) {
				$('#taskFormErrors').html("Please enter a time estimate.");
				error = true;
			} else if(!$.isNumeric(document.taskDetailsUpdateForm.timeEstimate.value)) {
				$('#taskFormErrors').html("Please enter a valid time estimate.");
				error = true;
			}
			if(isCodingTask(document.taskDetailsUpdateForm.tag.value)) {
				//get selected commits if present
				document.taskDetailsUpdateForm.commits.value = getSelectedMultiSelectBoxIds();

				if(document.taskDetailsUpdateForm.branch.value == '') {
					$('#taskFormErrors').html("Please select a branch.");
					error = true;
				} else if(document.taskDetailsUpdateForm.status.value == 'Completed' && document.taskDetailsUpdateForm.commits.value == '') {
					$('#taskFormErrors').html("Please select one or more commits.");
					error = true;
				}
			} else {
				document.taskDetailsUpdateForm.branch.value = '';
				document.taskDetailsUpdateForm.commits.value = '';
			}
		}
		if(error) {
			$('#taskFormErrors').show();
		}
		//submit form if all values present
		else {
			const url = "updateTaskDetails/"+$("#taskId").val();
			jQuery.ajax(
				url, {
					async: false,
					method: "POST",
					data: $("#taskDetailsUpdateForm").serialize(),
					success: (data,status) => {
						displaySuccess();
						refreshBoard();
					},
					error: () => {
						console.log("error when updating task details");
					}
				});
		}
	});
	
	$('#deleteTask').click(function () {
		const url = "deleteTask/"+$("#taskId").val();
		jQuery.ajax(
			url, {
				async: false,
				method: "POST",
				data: $("#taskDetailsUpdateForm").serialize(),
				success: (data,status) => {
					displaySuccess();
					refreshBoard();
				},
				error: () => {
					console.log("error when deleting task");
				}
			});
	});

	$('#submit-impact-btn').click(function () {
		let impactedFiles = new Array();
		$("[name=impactedFiles]").each(function() {
			if($(this).is(':checked')) {
				impactedFiles.push($(this).val());
			}
		});
		const url = "addTaskImpactedFiles/"+$("#taskId").val();
		jQuery.ajax(
			url, {
				async: false,
				method: "POST",
				data: {branch: $("#impactBranchName").val(), impactedFilePaths: JSON.stringify(impactedFiles)},
				success: (data) => {
					$("#impact-form-container").hide();
					removeAddedFilesFromList(data);
					refreshBoard();
					populateTaskImpact(data);
				},
				error: () => {
					console.log("error when updating task impacted files");
				}
			});

	});
	
	$('#submit-task-users-btn').click(function () {
		let error = true;
		//cycle board
		if(document.taskUsersUpdateForm.isCycleBoard.value == 'true') {
			$("[name^=users-]").each(function() {
				if($(this).is(':checked') && $(this).attr("id").endsWith("owner")) {
					error = false;
				}
			});
			if(error) {
				$('#taskFormErrors').html("Please select a task owner.");
				$('#taskFormErrors').show();
			}
			//submit form if all values present
			else {
				const url = "updateTaskUsers/"+$("#taskId").val();
				jQuery.ajax(
					url, {
						async: false,
						method: "POST",
						data: $("#taskUsersUpdateForm").serialize(),
						success: (data,status) => {
							displaySuccess();
							refreshBoard();
						},
						error: () => {
							console.log("error when updating task users");
						}
					});
			}
		}
	});
	
	$('#submit-work-btn').click(function () {
		const url = "addWork/"+$("#taskId").val();
		jQuery.ajax(
			url, {
				async: false,
				method: "POST",
				data: $("#addWorkForm").serialize(),
				success: (data,status) => {
					$('#addWorkForm').hide();
					$('#add-work-btn').show();
					refreshBoard();
					populateTaskWork(data);
				},
				error: () => {
					console.log("error when adding task work");
				}
			});
	});
	
	$('#submit-note-btn').click(function() {
		const url = "addNote/" + $("#taskId").val();
		let text = $("#noteText").val();
		if(text != '' && text.trim() != '') {
			jQuery.ajax(
				url, {
					async: false,
					method: "POST",
					data: {noteText: text},
					success: (data,status) => {
						refreshBoard();
						$("#noteText").val("");
						populateTaskNotes(data);
					},
					error: () => {
						console.log("error when adding task note");
					}
				});
		}
	});
	
	$('#submit-commit-btn').click(function() {
		updateTaskStatus($("#commitTaskId").val(), $("#commitNewStatus").val(), $("#commitNewPriority").val());
	});
	
	//modify form fields
	$("[type='number']").keypress(function (evt) {
	    evt.preventDefault();
	});
	
	$("#branch").change(function() {
		getBranchCommits($(this).val());
	});

	$("#impactBranchName").change(function() {
		getRepositoryTree($("#repositoryProjectId").val(), $(this).val());
	});

	
	var taskTagOptions = "<option id='emptyTagOption' value=''>Choose a tag</option>";
	for(let i=0; i<taskTags.length; i++) {
		taskTagOptions += "<option value='"+taskTags[i]+"'>"+taskTags[i]+"</option>";
	}
	$('#tag').html(taskTagOptions);
	
	$('#tag').change(function() {
		if(isCodingTask($('#tag').find(":selected").val())) {
			$('#branchField').show();
			$('#commitsField').show();
		} else {
			$('#branchField').hide();
			$('#commitsField').hide();
		}
	});
});

function refreshBoard() {
	document.board.clear();
	document.board.queryTasks();
	document.board.createDom();
}

function generateContent(boardId, isCycle, isActive) {
	isBoardActive = isActive;
	if(isCycle) {
		document.board = initCycleBoard(boardId);
	} else {
		document.board = initProductBoard(boardId);
	}
	refreshBoard();
}

function initBoardColumn(name, div) {
	const column = {
			name: name,
			div: div,
			cards: [],
			createDom: () => {
				const renderedCards = document.createDocumentFragment();
				for (let i = 0; i < column.cards.length; i++) {
					const card = column.cards[i];
					renderedCards.appendChild(card.div);
				}
				column.div.appendChild(renderedCards);
			},
			clear: () => {
				while (column.div.firstChild) {
					column.div.removeChild(column.div.firstChild);
				}
				currentDraggedCard = null;
				column.cards = [];
			}
	}
	column.div.setAttribute("ondragover", "event.preventDefault()");
	column.div.setAttribute("ondrop", "drop(event)");
	column.div.column = column;

	return column;
}

/* task detail popup */
function loadTaskDetail(card, isCycleBoard) {
    populateTaskFormValues(card, isCycleBoard);
    populateTaskHistory(card);
    populateTaskWork(card);
    populateTaskImpact(card);
    displayTaskDetails();
    $('#taskModal').modal('show');
}

function populateTaskFormValues(card, isCycleBoard) {
	$('#branchField').hide();
	$('#commitsField').hide();
	
	$('#taskId').val(card.taskId);
	$('#taskStatus').val(card.status);
	$('#description').val(card.description);
	populateTaskNotes(card);
	selectTag(card);
	selectBranch(card);
	if(card.repositoryProjectBranch != '') {
		getBranchCommits(card.repositoryProjectBranch);
	}
	selectCommits(card);
	if(isCycleBoard) {
		if(isCodingTask(card.tag)) {
			$('#branchField').show();
			$('#commitsField').show();
		}
		$('#timeEstimate').val(card.timeEstimate);
		$('#fmtCompletionDateEst').val(card.fmtCompletionDateEst);
		populateUserRoles(card);
	}
}

function populateTaskCommits(data) {
	$("#commitTable").empty();
	var i;
	for (i=0; i<data.length; i++) {
		const commit = data[i];
		if(i < 3) {
			$('#commitTable').append("<tr id='"+commit.commitId+"' class='multiSelectBoxItem'><td style='width:100%;min-width:100%;'>" + commit.commitId + "<br/>" + commit.commitMessage + "</td></tr>");
		} else {
			$('#commitTable').append("<tr id='"+commit.commitId+"' class='multiSelectBoxItem hiddenItem'><td style='width:100%;min-width:100%;'>" + commit.commitId + "<br/>" + commit.commitMessage + "</td></tr>");
		}
	}
	if(i >= 3) {
		$('#commitTable').append("<tr><td id='exp-col-btn' style='width:100%;min-width:100%;'>&#x25BC;</td></tr>");
	}
}

function populateTaskNotes(card) {
	$("#note-list").empty();
	if(card.notes != undefined && card.notes.length > 0) {
		$("#note-list").append("<ul class='list-group'>");
		for(let i=0; i<card.notes.length; i++) {
			$("#note-list").append('<li class="list-group-item">'+card.notes[i].text + '<div class="list-subtext">posted by '+ card.notes[i].user.firstName + ' ' + card.notes[i].user.lastName + ' on ' + card.notes[i].formattedDate + '</div></li>');
		}
		$("#note-list").append("</ul><br>");
	} else {
		$("#note-list").html("<div>No notes present for this task.</div>");
	}
}

function populateTaskHistory(card) {
	$("#task-history").empty();
	if(card.taskHistories != undefined && card.taskHistories.length > 0) {
		$("#task-history").append("<ul class='list-group'>");
		for(let i=0; i<card.taskHistories.length; i++) {
			let taskHistory = card.taskHistories[i];
			for(let j=0; j<taskHistory.changedValueList.length; j++) {
				let taskChange = taskHistory.changedValueList[j];
				var changeDesc = '';
				//if changed field value is a list
				if(taskChange.oldValue != null && taskChange.oldValue.startsWith('[')) {
					taskChange.oldValue = taskChange.oldValue.substring(1, taskChange.oldValue.length-1);
					taskChange.newValue = taskChange.newValue.substring(1, taskChange.newValue.length-1);
					var oldValues = taskChange.oldValue.split(',');
					var newValues = taskChange.newValue.split(',');
					for(let oldInd = 0; oldInd < oldValues.length; oldInd++) {;
						if(oldValues[oldInd] != '' && newValues.indexOf(oldValues[oldInd]) == -1) {
							if(changeDesc != '') {
								changeDesc += ', ';
							}
							changeDesc += '"' + oldValues[oldInd].trim() + '" was removed from <strong>' + taskChange.changedField + '</strong>';
						}
					}
					for(let newInd = 0; newInd < newValues.length; newInd++) {
						if(newValues[newInd] != '' && oldValues.indexOf(newValues[newInd]) == -1) {
							if(changeDesc != '') {
								changeDesc += ', ';
							}
							changeDesc += '"' + newValues[newInd].trim() + '" was added to <strong>' + taskChange.changedField + '</strong>';
						}
					}
				} 
				//if changed field value is not a list
				else {
					changeDesc = '<strong>' + taskChange.changedField;
					changeDesc += (taskChange.oldValue == null || taskChange.oldValue == '')? '</strong> was set to "' + taskChange.newValue + '"' : '</strong> was changed from "' + taskChange.oldValue + '" to "' + taskChange.newValue + '"';
				}
				$("#task-history").append('<li class="list-group-item">' + changeDesc + ' on ' + taskHistory.formattedDate + ' by ' + taskHistory.changedByUser.firstName + ' ' + taskHistory.changedByUser.lastName + '</li>');
			}
		}
		$("#task-history").append("</ul><br>");
	} else {
		$("#task-history").html("<div>This task has not been updated.</div>");
	}	
}

function populateTaskWork(card) {
	let userId = $("#userId").attr("value");
	let userIsAssigned = isUserAssigned(userId, card);
	if(userIsAssigned) {
		$("#add-work-btn").show();
	} else {
		$("#add-work-btn").hide();
	}
	$('taskId').val(card.taskId);
	$('#task-work-list').empty();
	if(card.work != undefined && card.work.length > 0) {
		$('#task-work-list').append("<ul class='list-group'>");
		for(let i=0; i<card.work.length; i++) {
			let taskWork = card.work[i];
			addWorkItem(taskWork.workId, taskWork.formattedDate, taskWork.user.firstName + " " + taskWork.user.lastName, taskWork.numMinutes, taskWork.description, userId, taskWork.user.id);
		}
		$('#task-work-list').append("</ul><br>");
	} else {
		$('#task-work-list').append("<div>No work reported for this task.</div>");
	}
}

function populateTaskImpact(card) {
	$('#task-impact-list').empty();
	if(card.impactedFiles != undefined && card.impactedFiles.length > 0) {
		$("#impactBranchName option[value='"+card.impactedFiles[0].branch+"']").prop('selected', true);
		$('#task-impact-list').append("<ul class='list-group'>");
		for(let i=0; i<card.impactedFiles.length; i++) {
			let impactedFile = card.impactedFiles[i];
			addImpactItem(impactedFile.impactedProjectFileId, impactedFile.path);
		}
		$('#task-impact-list').append("</ul><br>");
	} else {
		$('#task-impact-list').append("<div>No impacted files reported for this task.</div>");
	}
}

function populateTaskImpactItemUsers(impactedFileId, users) {
	$("#"+impactedFileId).append("<ul id='"+impactedFileId+"-users'></ul>");
	for(let i=0; i<users["current"].length; i++) {
		$("#"+impactedFileId+"-users").append("<li class='small-text'>"+users['current'][i]+" <span class='other-color'><i class='fa fa-comment-o fa-1'></i></span></li>");
	}
	for(let i=0; i<users["previous"].length; i++) {
		$("#"+impactedFileId+"-users").append("<li class='small-text'>"+users['previous'][i]+"</li>");
	}
}

function populateFileTree(treeMapObj) {
	//loop assumes the first key is the project root
	let projectRoot = Object.keys(treeMapObj)[0];
	let fileTreeHTML = "<ul id='fileTreeList'>";
	fileTreeHTML += writeFileTreeDir(projectRoot, treeMapObj);
	fileTreeHTML += "</ul>";
	$("#repositoryFileTree").append(fileTreeHTML);
}

function writeFileTreeDir(path, treeMapObj) {
	let fileTreeHTML = "<li><span class='caret'>"+getFolderName(path)+"</span>";
	fileTreeHTML += "<ul id='"+path+"' class='nested'>";

	let dirContents = treeMapObj[path];
	for(let i=0; i<dirContents.length; i++) {
		if(isDir(dirContents[i])) {
			fileTreeHTML += writeFileTreeDir(dirContents[i], treeMapObj);
		} else {
			fileTreeHTML += "<li><input type='checkbox' name='impactedFiles' value='"+path+"/"+dirContents[i]+"'> "+dirContents[i]+"</input></li>";
		}
	}
	fileTreeHTML += "</ul>";
	fileTreeHTML += "</li>";
	return fileTreeHTML;
}

function getFolderName(filePath) {
	let parentPathInd = filePath.lastIndexOf("/");
	if(parentPathInd > -1) {
		return filePath.substr(parentPathInd+1);
	} else {
		return filePath;
	}
}

function injectTreeFunctionality() {
	let toggler = document.getElementsByClassName("caret");
	for (let i = 0; i < toggler.length; i++) {
		toggler[i].addEventListener("click", function() {
			this.parentElement.querySelector(".nested").classList.toggle("active");
			this.classList.toggle("caret-down");
		});
	}
}

function isDir(dirItem) {
	return dirItem.indexOf("/") > -1;
}

function removeAddedFilesFromList(task) {
	if(task.impactedFiles != undefined && task.impactedFiles.length > 0) {
		for (let i = 0; i < task.impactedFiles.length; i++) {
			$("input[value='"+task.impactedFiles[i].path+"']").parent().remove();
		}
	}
}

function addWorkItem(id, date, userName, minutes, description, userId, workUserId) {
	var workItemHTML = "<li id='" + id + "' class='list-group-item'>";
	if(userId == workUserId && isBoardActive) {
		workItemHTML += '<button id="delete-work-item" class="btn delete-btn list-btn" onclick="deleteWorkItem('+id+')">Delete</button>';
	}
	workItemHTML += date + ": " + userName + "<br>" + minutes + " min";
	if(description != null && description != '') {
		 workItemHTML += ": " + description;
	}
	workItemHTML += "</li>";
	$('#task-work-list > ul').append(workItemHTML);
}

function deleteWorkItem(id) {
	const url = "deleteWork/"+id;
	jQuery.ajax(
		url, {
			async: false,
			method: "POST",
			success: (data,status) => {
				refreshBoard();
				populateTaskWork(data);
			},
			error: () => {
				console.log("error when deleting work item");
			}
		});
}

function addImpactItem(impactedProjectFileId, path) {
	var impactedFileHTML = "<li id='" + impactedProjectFileId + "' class='list-group-item'>";
	if(isBoardActive) {
		impactedFileHTML += "<span class='delete-color'><i id='delete-impact-item' class='fa fa-window-close fa-1' onclick='deleteImpactItem("+impactedProjectFileId+", \""+path+"\")'></i> </span>";
	}
	impactedFileHTML += "<span class='other-color'><i id='view-impact-item-users' class='fa fa-users fa-1' onclick='toggleImpactItemUsers("+impactedProjectFileId+")'></i> </span>";
	impactedFileHTML += "<span class='small-text'>"+path.substring(path.lastIndexOf('/')+1)+"</span>";
	impactedFileHTML += "</li>";
	$('#task-impact-list > ul').append(impactedFileHTML);
}

function deleteImpactItem(impactedFileId, path) {
	const url = "deleteImpactItem/"+$("#taskId").val()+"/"+impactedFileId;
	jQuery.ajax(
		url, {
			async: false,
			method: "POST",
			success: (data) => {
				refreshBoard();
				populateTaskImpact(data);
				addFileToRepositoryTree(path);
			},
			error: () => {
				console.log("error when deleting impact item");
			}
		});
}

function toggleImpactItemUsers(impactedFileId) {
	//if users list has already been loaded
	if($("#"+impactedFileId+"-users").length) {
		if($("#"+impactedFileId+"-users").is(":visible")) {
			$("#"+impactedFileId+"-users").hide();
		} else {
			$("#"+impactedFileId+"-users").show();
		}
	} else {
		const url = "showImpactItemUsers/" + $("#taskId").val() + "/" + impactedFileId;
		jQuery.ajax(
			url, {
				async: false,
				method: "POST",
				success: (data) => {
					populateTaskImpactItemUsers(impactedFileId, data);
				},
				error: () => {
					console.log("error when loading impact item users");
				}
			});
	}
}

function addFileToRepositoryTree(path) {
	let lastSlashInd = path.lastIndexOf("/");
	if(lastSlashInd > -1) {
		let dirPath = path.substring(0, lastSlashInd);
		let listElement = document.getElementById(dirPath);
		if(listElement != null) {
			let fileHTML = "<li><input type='checkbox' name='impactedFiles' value='" + path + "'> " + path.substr(lastSlashInd + 1) + "</input></li>";
			listElement.innerHTML = listElement.innerHTML + fileHTML;
		}
	}
}

function getBranchCommits(branch) {
	const projectId = $("#projectId").val();
	const url = "commits/"+projectId+"/"+branch;
	jQuery.ajax(
		url, {
			async: false,
			method: "POST",
			success: (data,status) => {
				populateTaskCommits(data);
			},
			error: () => {
				console.log("error when obtaining branch commits");
			}
		});
}

function getRepositoryTree(repositoryProjectId, branchName) {
	const url = "/coop/repository/queryRepositoryProjectFiles/"+repositoryProjectId+"/"+branchName+"/"+$("#taskId").val();
	$("#filesLoading").show();
	jQuery.ajax(
		url, {
			async: true,
			method: "POST",
			success: (data) => {
				populateFileTree(data);
				$("#filesLoading").hide();
				injectTreeFunctionality();
			},
			error: () => {
				$("#filesLoading").hide();
				console.log("error when obtaining project files from the repository");
			}
		});
}

function displayTaskDetails() {
	$("#task-impact").hide();
	$("#task-impact-btn").removeClass("active-nav-btn");
	$("#task-users").hide();
	$("#task-users-btn").removeClass("active-nav-btn");
	$("#task-notes").hide();
	$("#task-notes-btn").removeClass("active-nav-btn");
	$("#task-work").hide();
	$("#task-work-btn").removeClass("active-nav-btn");
	$("#task-history").hide();
	$("#task-history-btn").removeClass("active-nav-btn");
	$('#submit-task-users-btn').hide();
	$('#taskFormErrors').hide();
	$('#taskFormUpdated').hide();
	
	$("#task-details").show();
	$("#task-details-btn").addClass("active-nav-btn");
	$('#submit-task-details-btn').show();
	$('#task-delete-btn').show();
}

function displayTaskImpact() {
	$("#task-details").hide();
	$("#task-details-btn").removeClass("active-nav-btn");
	$("#task-users").hide();
	$("#task-users-btn").removeClass("active-nav-btn");
	$("#task-notes").hide();
	$("#task-notes-btn").removeClass("active-nav-btn");
	$("#task-work").hide();
	$("#task-work-btn").removeClass("active-nav-btn");
	$("#task-history").hide();
	$("#task-history-btn").removeClass("active-nav-btn");
	$('#submit-task-details-btn').hide();
	$('#task-delete-btn').hide();
	$('#submit-task-users-btn').hide();
	$('#taskFormErrors').hide();
	$('#taskFormUpdated').hide();
	//TODO: ADD PERMISSIONS
	$("#impact-form-container").hide();
	$("#task-impact").show();
	$("#task-impact-btn").addClass("active-nav-btn");
}

function displayTaskUsers() {
	$("#task-details").hide();
	$("#task-details-btn").removeClass("active-nav-btn");
	$("#task-impact").hide();
	$("#task-impact-btn").removeClass("active-nav-btn");
	$("#task-notes").hide();
	$("#task-notes-btn").removeClass("active-nav-btn");
	$("#task-work").hide();
	$("#task-work-btn").removeClass("active-nav-btn");
	$("#task-history").hide();
	$("#task-history-btn").removeClass("active-nav-btn");
	$('#submit-task-details-btn').hide();
	$('#task-delete-btn').hide();
	$('#taskFormErrors').hide();
	$('#taskFormUpdated').hide();
	
	$("#task-users").show();
	$("#task-users-btn").addClass("active-nav-btn");
	$('#submit-task-users-btn').show();
}

function displayTaskNotes() {
	$("#task-details").hide();
	$("#task-details-btn").removeClass("active-nav-btn");
	$("#task-impact").hide();
	$("#task-impact-btn").removeClass("active-nav-btn");
	$("#task-users").hide();
	$("#task-users-btn").removeClass("active-nav-btn");
	$("#task-work").hide();
	$("#task-work-btn").removeClass("active-nav-btn");
	$("#task-history").hide();
	$("#task-history-btn").removeClass("active-nav-btn");
	$('#submit-task-details-btn').hide();
	$('#task-delete-btn').hide();
	$('#submit-task-users-btn').hide();
	$('#taskFormErrors').hide();
	$('#taskFormUpdated').hide();
	
	$("#task-notes").show();
	$("#task-notes-btn").addClass("active-nav-btn");
}

function displayTaskWork() {
	$("#task-details").hide();
	$("#task-details-btn").removeClass("active-nav-btn");
	$("#task-impact").hide();
	$("#task-impact-btn").removeClass("active-nav-btn");
	$("#task-users").hide();
	$("#task-users-btn").removeClass("active-nav-btn");
	$("#task-notes").hide();
	$("#task-notes-btn").removeClass("active-nav-btn");
	$("#task-history").hide();
	$("#task-history-btn").removeClass("active-nav-btn");
	$('#submit-task-details-btn').hide();
	$('#task-delete-btn').hide();
	$('#submit-task-users-btn').hide();
	$('#taskFormErrors').hide();
	$('#taskFormUpdated').hide();

	if(isUserAssignedToTask) {
		$('#add-work-btn').show();
	}
	$('#addWorkForm').hide();
	$("#task-work").show();
	$("#task-work-btn").addClass("active-nav-btn");
}

function displayTaskHistory() {
	$("#task-details").hide();
	$("#task-details-btn").removeClass("active-nav-btn");
	$("#task-impact").hide();
	$("#task-impact-btn").removeClass("active-nav-btn");
	$("#task-users").hide();
	$("#task-users-btn").removeClass("active-nav-btn");
	$("#task-notes").hide();
	$("#task-notes-btn").removeClass("active-nav-btn");
	$("#task-work").hide();
	$("#task-work-btn").removeClass("active-nav-btn");
	$('#submit-task-details-btn').hide();
	$('#task-delete-btn').hide();
	$('#submit-task-users-btn').hide();
	$('#taskFormErrors').hide();
	$('#taskFormUpdated').hide();

	$("#task-history").show();
	$("#task-history-btn").addClass("active-nav-btn");
}

function selectTag(card) {
	if(card.tag == undefined) {
		$('#emptyTagOption').prop("selected", true);
	} else {
		$('#tag > option').each(function() {
			if($(this).val() == card.tag) {
				$(this).prop("selected", true);
			}
		});
	}
}

function selectBranch(card) {
	if(card.repositoryProjectBranch == undefined || card.repositoryProjectBranch == '') {
		$('#emptyBranchOption').prop("selected", true);
	} else {
		$('#branch > option').each(function() {
			if($(this).val() == card.repositoryProjectBranch) {
				$(this).prop("selected", true);
			}
		});
	}
}

function selectCommits(card) {
	clearSelectedIds();
	for (let i = 0; i < card.repositoryCommits.length; i++) {
		selectItem($(".multiSelectBox").find("#"+card.repositoryCommits[i]));
	}
	let userId = $("#userId").attr("value");
	if(!isBoardActive || !isUserAssigned(userId, card)) {
		preventUpdates();
	}
}

function displaySuccess() {
	$('#taskFormUpdated').html("Task update successful!");
	$('#taskFormUpdated').show();
}

/*************************** PRODUCT BACKLOG ***************************/
function initProductBoard(id) {
	board = {
			remainingTasks: initBoardColumn("Remaining Tasks", document.getElementById("remainingTasks")),
			id: id,
			queryTasks: () => {
				const url = "queryTasks/" + board.id;
				jQuery.ajax(
					url, {
						async: false,
						method: "POST",
						success: (data,status) => {
							for (let i=0; i<data.length; i++) {
								const card = initProductBoardCard(data[i]);
								board.remainingTasks.cards.push(card);
							}
						},
						error: () => {
							console.log("error when loading product board");
						}
					});
			},
			createDom: () => {
				board.remainingTasks.cards.forEach((card) => {
					board.remainingTasks.div.appendChild(card.div);
				});
			},
			clear: () => {
				board.remainingTasks.clear();
			}
	};
	return board;
}

function addNewProductBoardCard() {
	let c = initProductBoardCard();
	board.remainingTasks.cards.push(c);
	board.remainingTasks.div.appendChild(c.div);
	if(newTaskField != null) {
		newTaskField.focus();
	}
}

function initProductBoardCard(card) {
	if(card == undefined) {
		card = {
			taskId: "",
			description: "",
			status: "Not Started",
			priority: 0,
			taskHistories: []
		}
	}
	
	const jqCardDiv = jQuery("<div />", {
		"id": card.taskId,
		"class": "card text-white product-task-card " + card.tag
	});
	
	card.div = jqCardDiv[0];
	card.div.card = card;
	
	/* configure card object for existing task*/
	if(card.taskId != '') {
		/* card text display div */
		const jqTitle = jQuery("<div />");
		
		jqTitle[0].onclick = (ev) => {
			loadTaskDetail(card, false);
		};
		
		jqTitle[0].innerText = card.description;
		jqCardDiv.append(jqTitle);
	}
	/* configure card object for new task */
	else {
		/* card text input field */
		const jqCardTextInput = jQuery("<input>", {
			"class": "form-control col-xs-6 task-card-input"
		});
		jqCardTextInput[0].setAttribute("type", "text");
		
		/* allow return key to finish editing */
		jqCardTextInput.keyup(function(e) {
			if(e.which == 13) {
				jqCardTextInput.trigger( "blur" );
			}
		});
		
		/* when description text field is exited */
		jqCardTextInput[0].onblur = (ev) => {
			//if description is present
			if(ev.target.value != '') {
				//create task
				const url = "addTask/" + board.id + "?description=" + ev.target.value;
				jQuery.ajax(
						url, {
							async: false,
							method: "POST",
							success: (data, status) => {
								refreshBoard();
							},
							error: (a, b, c) => {
							}
						});
			}
			//if no description is present
			else {
				//delete task card
				ev.target.parentElement.remove();
			}
		};
		
		jqCardDiv.append(jqCardTextInput[0]);
		newTaskField = jqCardTextInput[0];
	}
	return card;
}

/*************************** CYCLE BACKLOG ***************************/
function initCycleBoard(id) {
	board = {
			id: id,
			statuses: initStatuses(),
			queryTasks: () => {
				const url = "queryTasks/" + board.id;
				jQuery.ajax(
						url, {
							async: false,
							method: "POST",
							success: (data, status) => {
								for (let i = 0; i < data.length; i++) {
									addCardToStatus(board, data[i]);
								}
							},
							error: () => {
								console.log("error");
							}
						});
			},
			createDom: () => {
				for(i=0; i<board.statuses.length; i++) {
					board.statuses[i].createDom();
				}
			},
			clear: () => {
				for(i=0; i<board.statuses.length; i++) {
					board.statuses[i].clear();
				}
			}
	};
	
	return board;
}

function initStatuses() {
	let statuses = [];
	for(i=0; i<taskStatuses.length; i++) {
		statuses.push(initBoardColumn(taskStatuses[i], document.getElementById(taskStatuses[i])));
	}
	return statuses;
}

function addCardToStatus(board, cardData) {
	const card = initCycleBoardCard(cardData);
	board.statuses[taskStatuses.indexOf(card.status)].cards.push(card);
}

function addNewCycleBoardCard() {
	let c = initCycleBoardCard();
	board.statuses[0].cards.push(c);
	board.statuses[0].div.appendChild(c.div);
	if(newTaskField != null) {
		newTaskField.focus();
	}
}

function initCycleBoardCard(card) {
	if(card == undefined) {
		card = {
			taskId: "",
			description: "",
			status: "Not Started",
			tag: "",
			priority: 0
		}
	}
	const jqCardDiv = jQuery("<div />", {
		"id": card.taskId,
		"class": "card text-white task-card " + card.tag,
		"ondragstart": "handleBoardCardDragStart(event)",
		"ondragenter": "handleBoardCardDragEnter(event)",
		"ondragleave": "handleBoardCardDragLeave(event)"
	});
	card.div = jqCardDiv[0];
	card.div.card = card;

	/* configure card object for existing task*/
	if(card.taskId != '') {
		/* card text display div */
		const jqTitle = jQuery("<span />");
		
		jqTitle[0].onclick = (ev) => {
			loadTaskDetail(card, true);
		};
		jqTitle[0].innerText = card.description;
		
		//add needs helper/reviewer icon
		if(cardNeedsHelperOrReviewer(card)) {
			jqTitle.prepend("<i class='fa fa-user-times assigned-icon'></i>");
		}
		//add role association icon
		let userId = $("#userId").attr("value");
		if(isUserOwner(userId, card)) {
			jqTitle.prepend("<i class='fa fa-star assigned-icon'></i>");
		} else if(isUserReviewer(userId, card)) {
			jqTitle.prepend("<i class='fa fa fa-star-half-full assigned-icon'></i>");
		} else if(isUserHelper(userId, card)) {
			jqTitle.prepend("<i class='fa fa-star-o assigned-icon'></i>");
		}
		jqCardDiv.append(jqTitle);

		card.updateStatus = (newStatus, newPriority) => {
			const url = "updateStatus/" + card.taskId + "?status=" + newStatus + "&priority=" + newPriority;
			jQuery.ajax(
				url, {
					async: false,
					method: "POST",
					success: (data, status) => {
					},
					error: (xhr, ajaxOptions, thrownError) => {
						displayError(xhr.responseText);
					}
			});
		}
		card.div.setAttribute("draggable", "true");
	}
	/* configure card object for new task */
	else {
		/* card text input field */
		const jqCardTextInput = jQuery("<input>", {
			"class": "form-control col-xs-6 task-card-input"
		});
		jqCardTextInput[0].setAttribute("type", "text");
		
		/* allow return key to finish editing */
		jqCardTextInput.keyup(function(e) {
			if(e.which == 13) {
				jqCardTextInput.trigger( "blur" );
			}
		});
		
		/* when description text field is exited */
		jqCardTextInput[0].onblur = (ev) => {
			//if description is present
			if(ev.target.value != '') {
				ev.target.setAttribute("draggable", "true");
				//create task
				const url = "addTask/" + board.id + "?description=" + ev.target.value;
				jQuery.ajax(
						url, {
							async: false,
							method: "POST",
							success: (data, status) => {
								refreshBoard();
							},
							error: (a, b, c) => {
							}
						});
			}
			//if no description is present
			else {
				//delete task card
				ev.target.parentElement.remove();
			}
		};
		
		jqCardDiv.append(jqCardTextInput[0]);
		newTaskField = jqCardTextInput[0];
		card.div.setAttribute("draggable", "false");
	}
	return card;
}

function updateTaskStatus(taskId, newStatus, newPriority) {
	const url = "updateStatus/" + taskId + "?status=" + newStatus + "&priority=" + newPriority;
	jQuery.ajax(
		url, {
			async: false,
			method: "POST",
			success: (data, status) => {
			},
			error: (xhr, ajaxOptions, thrownError) => {
				displayError(xhr.responseText);
			}
		});
}

function cardNeedsHelperOrReviewer(card) {
	return (card.status == taskStatuses[2] && card.helpers.length == 0) || (card.status == taskStatuses[3] && card.reviewers.length == 0);
}

function isUserAssigned(userId, card) {
	isUserAssignedToTask = isUserOwner(userId, card) || isUserHelper(userId, card) || isUserReviewer(userId, card);
	return isUserAssignedToTask;
}

function isUserOwner(userId, card) {
	if(card.owners != undefined) {
		for(let i=0; i<card.owners.length; i++) {
			if(card.owners[i].id == userId) {
				return true;
			}
		}
	}
	return false;
}

function isUserHelper(userId, card) {
	if(card.helpers != undefined) {
		for(let i=0; i<card.helpers.length; i++) {
			if(card.helpers[i].id == userId) {
				return true;
			}
		}
	}
	return false;
}

function isUserReviewer(userId, card) {
	if(card.reviewers != undefined) {
		for(let i=0; i<card.reviewers.length; i++) {
			if(card.reviewers[i].id == userId) {
				return true;
			}
		}
	}
	return false;
}

function isCodingTask(tag) {
	return tag == 'Feature Implementation' || tag == 'Unit Test' || tag == 'Bug Fix' || tag == 'Refactor';
}

function displayError(text) {
	$("#popupErrorText").text(text);
	$("#popupError").modal("show");
}

/* task detail popup */
function populateUserRoles(card) {
	$(".task-user-row").each(function() {
		let userId = $(this).attr("id");
		let roleChecked = false;
		if(isUserOwner(userId, card)) {
			$("#"+ userId + "-owner").prop("checked", true);
			roleChecked = true;
		} else {
			$("#"+ userId + "-owner").prop("checked", false);
		}
		if(isUserHelper(userId, card)) {
			$("#"+ userId + "-helper").prop("checked", true);
			roleChecked = true;
		} else {
			$("#"+ userId + "-helper").prop("checked", false);
		}
		if(isUserReviewer(userId, card)) {
			$("#"+ userId + "-reviewer").prop("checked", true);
			roleChecked = true;
		} else {
			$("#"+ userId + "-reviewer").prop("checked", false);
		}
		if(roleChecked == false) {
			$("#"+ userId + "-none").prop("checked", true);
		} else {
			$("#"+ userId + "-none").prop("checked", false);
		}
	});
}

/* drag and drop */
function handleBoardCardDragStart(ev) {
	currentDraggedCard = ev.target;
	
	//this line is for firefox to work correctly
	ev.dataTransfer.setData("text", ev.target.id);
	
	// Add this element's id to the drag payload so the drop handler will know which element to add to its tree
	ev.dataTransfer.effectAllowed = "move";
}

function handleBoardCardDragEnter(ev) {
	ev.preventDefault();
	let containingElement = ev.target;
	hoverCounter++;
	
	//determine whether card was dropped on another card or in a container
	while(containingElement.className.indexOf('task-card')==-1) {
		containingElement = containingElement.parentElement;
	}
	if(containingElement.id != currentDraggedCard.id) {
		currentHoveredCard = containingElement;
	}
}

function handleBoardCardDragLeave(ev) {
	ev.preventDefault();
	let containingElement = ev.target;
	hoverCounter--;
	
	//determine whether card was dropped on another card or in a container
	while(containingElement.className.indexOf('task-card')==-1) {
		containingElement = containingElement.parentElement;
	}
	if(hoverCounter == 0 && containingElement.id != currentDraggedCard.id) {
		currentHoveredCard = null;
	}
}

function drop(ev) {
	ev.preventDefault();
	let containingElement = ev.target;
	
	//determine whether card was dropped on another card or in a container
	while(containingElement.className.indexOf('task-card')==-1 && containingElement.className.indexOf('column-body')==-1) {
		containingElement = containingElement.parentElement;
	}
	//if card was dropped on another card
	if(containingElement.className.indexOf('task-card')!=-1) {
		let elementRect = containingElement.getBoundingClientRect();
		let dropY = ev.clientY;
		let midCardY = elementRect.top + ((elementRect.bottom - elementRect.top)/2);
		//dropped card should be placed above this card
		if(dropY < midCardY) {
			//update status and priority
			let priority = containingElement.card.priority;
			currentDraggedCard.card.updateStatus(containingElement.card.status, priority);
		} 
		//dropped card should be placed below this card
		else {
			let parent = containingElement;
			while(parent.className.indexOf('column-body') == -1) {
				parent = parent.parentElement;
			}
			let priority = containingElement.card.priority;
			let newPriority = ((priority+1)<parent.column.cards.length)?priority+1:parent.column.cards.length;
			//update status and priority
			currentDraggedCard.card.updateStatus(containingElement.card.status, newPriority);
		}
	} 
	//if card was dropped on a column
	else {
		let column = containingElement.column;
		//if column is empty
		if(column.cards.length == 0) {
			currentDraggedCard.card.updateStatus(column.name, 1);
		}
		//if column has cards
		else {
			let dropY = ev.clientY;
			let priorBottomY = 0;
			let dropped = false;
			for(let i=0; i<!dropped && column.cards.length; i++) {
				let elementRect = column.cards[i].div.getBoundingClientRect();
				let cardTopY = elementRect.top;
				let cardBottomY = elementRect.bottom;
				//if card should appear above currently evaluated card
				if(dropY >= priorBottomY && dropY <= cardTopY) {
					//update status and priority
					let priority = column.cards[i].priority;
					currentDraggedCard.card.updateStatus(column.name, priority);
					dropped = true;
				}
				priorBottomY = cardBottomY;
			}
			if(!dropped) {
				currentDraggedCard.card.updateStatus(column.name, column.cards.length+1);
			}
		}
	}
	
	document.board.clear();
	refreshBoard();

	return true;
}

function drag(ev) {
	ev.preventDefault();
	// Only allow move-style drag and drop.
	ev.dataTransfer.dropEffect = "move";
}