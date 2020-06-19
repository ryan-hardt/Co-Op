$(function() {
	var errorDiv = document.getElementById("errors");
	
	//if add form exists (add page)
	if(('#addUserForm').length) {
		$('#addUserForm').submit(function() {
			if ($('#firstName').val() == '' || $('#lastName').val() == '' || $('#username').val() == '' || $('#password').val() == '') {
				errorDiv.innerHTML = "Please fill out all fields";
				errorDiv.style["display"] = "block";
				return false;
			} else if ($('#password').val() != $('#verifyPassword').val()) {
				errorDiv.innerHTML = "Passwords do not match";
				errorDiv.style["display"] = "block";
				return false;
			}
			return true; // return true to continue submission
		});
	}
	//if update form exists (update page)
	if(('#updateUserForm').length) {
		$('#updateUserForm').submit(function() {
			if ($('#firstName').val() == '' || $('#lastName').val() == '' || $('#username').val() == '' || $('#password').val() == '') {
				errorDiv.innerHTML = "Please fill out all fields";
				errorDiv.style["display"] = "block";
				return false;
			} else if ($('#password').val() != $('#verifyPassword').val()) {
				errorDiv.innerHTML = "Passwords do not match";
				errorDiv.style["display"] = "block";
				return false;
			}
			return true; // return true to continue submission
		});
	}
});

function loadCharts() {
	//load stats for all projects (in work.js)
	updateCharts('role');
	$(".nav-link").click(function() {
		$("#workTabContent").height($("#workTabContent").height());
	});
}

function updateCharts(type) {
	//load stats for all projects (in work.js)
	$("#workTabContent").height('auto');
	initializeWorkStats(type, 'all');	//work.js
}

function checkDuplicate(){
	var username = document.getElementById('username').value;
	var xmlhttp = new XMLHttpRequest();
	xmlhttp.open("POST","/coop/user/checkduplicate?uname="+username,false);
	var status = 0;
	
	xmlhttp.send();	
	console.log(xmlhttp.status)
	if (xmlhttp.status != 200){
		var msg = document.getElementById('message');
		msg.innerHTML = "\""+username+'\" has been chosen.';
		msg.style.display ="inline";
		return false;
	}
}