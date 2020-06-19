function validate() {
	var x = document.forms["teamForm"]["teamName"].value;
	if (x.replace(/\s+/g, '') == "") {
		// Hides the "deleted team" message
		if ($("#deletedTeam").is(":visible")) {
			$("#deletedTeam").hide();
		}
		var elem = document.getElementById('errorOnAdd');
		elem.style.display = 'block';
		return false;
	}
}
