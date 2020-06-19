/**
 * Validation for create and update Cycle forms
 */

$(document).ready(function() {
	$("#startdate").datepicker({
		dateFormat: "mm/dd/yy"
	});
	$("#enddate").datepicker({
		dateFormat: "mm/dd/yy"
	});
});

document.addEventListener("DOMContentLoaded", () => {
	document.getElementsByTagName("form")[0].addEventListener("submit", (event) => {
		var startDate = document.getElementById("startdate").value;
		var endDate = document.getElementById("enddate").value;
		var errorDiv = document.getElementById("errors");
		
		var errors = [];

		if(!startDate) {
			errors.push("Please set a start date");
		}
		if(!endDate) {
			errors.push("Please set an end date");
		}
		var dateExpr = /\d{2}\/\d{2}\/\d{4}/;
		if(!dateExpr.test(startDate) ||
				!dateExpr.test(endDate) ||
				isNaN(Date.parse(startDate)) ||
				isNaN(Date.parse(endDate))) {
			errors.push("Please enter valid start and end dates in the format MM/DD/YYYY");
		}
		if(new Date(startDate) > new Date(endDate)) {
			errors.push("Make sure the start date is later than the end date");
		}

		if(errors.length != 0) {
			event.preventDefault();
			errorDiv.innerHTML = "<ul>";
			for(error of errors) {
				errorDiv.innerHTML += "<li>" + error + "</li>";
			}
			errorDiv.innerHTML += "</ul>";
			errorDiv.style["display"] = "block";
		}
	});
});